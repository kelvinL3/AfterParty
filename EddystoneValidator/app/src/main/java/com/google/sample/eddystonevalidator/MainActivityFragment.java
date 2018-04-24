// Copyright 2015 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sample.eddystonevalidator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Main UI and logic for scanning and validation of results.
 */
public class MainActivityFragment extends Fragment {

  private static final String TAG = "EddystoneValidator";
  private static final int REQUEST_ENABLE_BLUETOOTH = 1;
  private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;

  // An aggressive scan for nearby devices that reports immediately.
  private static final ScanSettings SCAN_SETTINGS =
      new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0)
          .build();

  private static final Handler handler = new Handler(Looper.getMainLooper());

  // The Eddystone Service UUID, 0xFEAA.
  private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
      ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

  private BluetoothLeScanner scanner;
  private BeaconArrayAdapter arrayAdapter;

  private List<ScanFilter> scanFilters;
  private ScanCallback scanCallback;

  private Map<String /* device address */, Beacon> deviceToBeaconMap = new HashMap<>();
  private Map<String /* device address */, Integer> deviceMissMap = new HashMap<>();

  private SharedPreferences sharedPreferences;
  private int onLostTimeoutMillis;

  private ScanResultCallback currCallback;

  interface ScanResultCallback{
      void onBeaconScan(Beacon beacon, double distance);

      void onBeaconLost(Beacon beacon);
  }
  public void setOnScanListener(ScanResultCallback scanResultCallback){
      currCallback=scanResultCallback;
  }

  public double getDistance(double rssi, double txPower){
      if (rssi == 0) {
          return -1.0; // if we cannot determine accuracy, return -1.
      }

      double ratio = ((double) rssi)/txPower;
      if (ratio <1.4) {
          return Math.pow(ratio,2)*Math.pow(10,16);
      }
      else {
          double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
          return accuracy;
      }
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
    ArrayList<Beacon> arrayList = new ArrayList<>();
    arrayAdapter = new BeaconArrayAdapter(getActivity(), R.layout.beacon_list_item, arrayList);
    scanFilters = new ArrayList<>();
    scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());
    scanCallback = new ScanCallback() {
      @Override
      public void onScanResult(int callbackType, ScanResult result) {
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord == null) {
          return;
        }
        String deviceAddress = result.getDevice().getAddress();

        double distance=getDistance(result.getRssi(),result.getScanRecord().getTxPowerLevel());
          if(distance>8.0){
              if(deviceToBeaconMap.get(deviceAddress)!=null){
                  if(deviceMissMap.get(deviceAddress)>5){
                      Beacon beacon=deviceToBeaconMap.get(deviceAddress);
                      Log.d(TAG,"Lost beacon");
                      deviceToBeaconMap.remove(deviceAddress);
                      deviceMissMap.remove(deviceAddress);
                      currCallback.onBeaconLost(beacon);
                  }else{
                      int miss=deviceMissMap.get(deviceAddress);
                      miss++;
                      deviceMissMap.put(deviceAddress,miss);
                  }

              }
              return;
          }

        Beacon beacon=null;
        if (!deviceToBeaconMap.containsKey(deviceAddress)) {
          beacon = new Beacon(deviceAddress, result.getRssi());
          deviceToBeaconMap.put(deviceAddress, beacon);
          deviceMissMap.put(deviceAddress,0);
          arrayAdapter.add(beacon);
            byte[] serviceData = scanRecord.getServiceData(EDDYSTONE_SERVICE_UUID);
            validateServiceData(deviceAddress, serviceData);
        } else {
            beacon=deviceToBeaconMap.get(deviceAddress);
            deviceMissMap.put(deviceAddress,0);
          deviceToBeaconMap.get(deviceAddress).lastSeenTimestamp = System.currentTimeMillis();
          deviceToBeaconMap.get(deviceAddress).rssi = result.getRssi();
        }
          currCallback.onBeaconScan(beacon,getDistance(beacon.rssi,result.getScanRecord().getTxPowerLevel()));
        Log.d(TAG,"Distance from room: "+distance);
      }

      @Override
      public void onScanFailed(int errorCode) {
        switch (errorCode) {
          case SCAN_FAILED_ALREADY_STARTED:
            logErrorAndShowToast("SCAN_FAILED_ALREADY_STARTED");
            break;
          case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
            logErrorAndShowToast("SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
            break;
          case SCAN_FAILED_FEATURE_UNSUPPORTED:
            logErrorAndShowToast("SCAN_FAILED_FEATURE_UNSUPPORTED");
            break;
          case SCAN_FAILED_INTERNAL_ERROR:
            logErrorAndShowToast("SCAN_FAILED_INTERNAL_ERROR");
            break;
          default:
            logErrorAndShowToast("Scan failed, unknown error code");
            break;
        }
      }
    };

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    onLostTimeoutMillis =
        sharedPreferences.getInt(SettingsActivity.ON_LOST_TIMEOUT_SECS_KEY, 5) * 1000;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);
    ListView listView = (ListView) view.findViewById(R.id.listView);
    listView.setAdapter(arrayAdapter);
    listView.setEmptyView(view.findViewById(R.id.placeholder));
    return view;
  }

  @Override
  public void onPause() {
    super.onPause();
    if (scanner != null) {
      //scanner.stopScan(scanCallback);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    handler.removeCallbacksAndMessages(null);

    int timeoutMillis =
        sharedPreferences.getInt(SettingsActivity.ON_LOST_TIMEOUT_SECS_KEY, 5) * 1000;

    if (timeoutMillis > 0) {  // 0 is special and means don't remove anything.
      onLostTimeoutMillis = timeoutMillis;
      setOnLostRunnable();
    }

    if (sharedPreferences.getBoolean(SettingsActivity.SHOW_DEBUG_INFO_KEY, false)) {
      Runnable updateTitleWithNumberSightedBeacons = new Runnable() {
        final String appName = getActivity().getString(R.string.app_name);

        @Override
        public void run() {
          getActivity().setTitle(appName + " (" + deviceToBeaconMap.size() + ")");
          handler.postDelayed(this, 1000);
        }
      };
      handler.postDelayed(updateTitleWithNumberSightedBeacons, 1000);
    } else {
      getActivity().setTitle(getActivity().getString(R.string.app_name));
    }

    if (scanner != null) {
      scanner.startScan(scanFilters, SCAN_SETTINGS, scanCallback);
    }
  }

  private void setOnLostRunnable() {
    Runnable removeLostDevices = new Runnable() {
      @Override
      public void run() {
        long time = System.currentTimeMillis();
        Iterator<Entry<String, Beacon>> itr = deviceToBeaconMap.entrySet().iterator();
        while (itr.hasNext()) {
          Beacon beacon = itr.next().getValue();
          if ((time - beacon.lastSeenTimestamp) > onLostTimeoutMillis||beacon.rssi>60) {
            itr.remove();
            deviceToBeaconMap.remove(beacon.deviceAddress);
            arrayAdapter.remove(beacon);
            currCallback.onBeaconLost(beacon);
          }
        }
        handler.postDelayed(this, onLostTimeoutMillis);
      }
    };
    handler.postDelayed(removeLostDevices, onLostTimeoutMillis);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
      if (resultCode == Activity.RESULT_OK) {
        init();
      } else {
        getActivity().finish();
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_COARSE_LOCATION: {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Log.d(TAG, "PERMISSION_REQUEST_COARSE_LOCATION granted");
        } else {
          showFinishingAlertDialog("Coarse location access is required",
              "App will close since the permission was denied");
        }
      }
    }
  }

  // Attempts to create the scanner.
  private void init() {
    // New Android M+ permission check requirement.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("This app needs coarse location access");
        builder.setMessage("Please grant coarse location access so this app can scan for beacons");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION);
          }
        });
        builder.show();
      }
    }
    BluetoothManager manager = (BluetoothManager) getActivity().getApplicationContext()
        .getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter btAdapter = manager.getAdapter();
    if (btAdapter == null) {
      showFinishingAlertDialog("Bluetooth Error", "Bluetooth not detected on device");
    } else if (!btAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    } else {
      scanner = btAdapter.getBluetoothLeScanner();
    }
  }

  // Pops an AlertDialog that quits the app on OK.
  private void showFinishingAlertDialog(String title, String message) {
    new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            getActivity().finish();
          }
        }).show();
  }

  // Checks the frame type and hands off the service data to the validation module.
  private void validateServiceData(String deviceAddress, byte[] serviceData) {
    Beacon beacon = deviceToBeaconMap.get(deviceAddress);
    if (serviceData == null) {
      String err = "Null Eddystone service data";
      beacon.frameStatus.nullServiceData = err;
      logDeviceError(deviceAddress, err);
      return;
    }
    Log.v(TAG, deviceAddress + " " + Utils.toHexString(serviceData));
    switch (serviceData[0]) {
      case Constants.UID_FRAME_TYPE:
        UidValidator.validate(deviceAddress, serviceData, beacon);
        break;
      case Constants.TLM_FRAME_TYPE:
        TlmValidator.validate(deviceAddress, serviceData, beacon);
        break;
      case Constants.URL_FRAME_TYPE:
        UrlValidator.validate(deviceAddress, serviceData, beacon);
        break;
      default:
        String err = String.format("Invalid frame type byte %02X", serviceData[0]);
        beacon.frameStatus.invalidFrameType = err;
        logDeviceError(deviceAddress, err);
        break;
    }
    arrayAdapter.notifyDataSetChanged();
  }

  private void logErrorAndShowToast(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    Log.e(TAG, message);
  }

  private void logDeviceError(String deviceAddress, String err) {
    Log.e(TAG, deviceAddress + ": " + err);
  }

}
