package com.google.sample.eddystonevalidator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.google.sample.eddystonevalidator.service.model.ItemsItem
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_room.*
import kotlinx.android.synthetic.main.viewholder_party.*
import kotlinx.android.synthetic.main.viewholder_party.view.*
import rutgers.edu.bonfire.extensions.toast
import io.socket.client.On.on
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.viewholder_song.view.*
import org.json.JSONObject




/**
 * Created by hemanth on 4/21/18.
 */
class MainActivity:Activity(){
    lateinit var blFragment: MainActivityFragment
    private var currRoom:Beacon?=null
    private val EVENT_ENTER_ROOM="ENTER_ROOM"
    private val EVENT_UPVOTE="UPVOTE_SONG"
    private val EVENT_DOWNVOTE="DOWNVOTE_SONG"
    private val EVENT_SKIP_SONG="SKIP_SONG"
    private val EVENT_PLAY_SONG="PLAY"
    private val EVENT_PAUSE="PAUSE"
    private val EVENT_SONG_INFO="SONG_INFO"
    private val EVENT_SONG_STATUS="CURRENT_STATUS"


    private val voteMap=HashMap<String,String>()
    private val requestMap=HashSet<String>()
    private var isPlaying=false


    private lateinit var socket:Socket
    private lateinit var context:Context
    companion object {
        const val TAG="MainActivity"

        const val RESULT_SEARCH=1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpSocket("http://08670788.ngrok.io")


        context=this
        blFragment=fragmentManager.findFragmentById(R.id.fragment) as MainActivityFragment
        blFragment.setOnScanListener(object :MainActivityFragment.ScanResultCallback{
            override fun onBeaconScan(beacon: Beacon, distance:Double) {
                if(currRoom==null){
                    val id=beacon.urlStatus.toString().split(".")[1]
                    textview_party.text=id
                    toast("Entered room: "+id+ "\nDistance: "+distance)

                    /*if(id=="hems03"){
                        constraint_layout_main.background=resources.getDrawable(R.drawable.blue)
                    }else{
                        constraint_layout_main.background=resources.getDrawable(R.drawable.red)
                    }*/

                    progressbar_room.visibility=View.INVISIBLE
                    currRoom=beacon

                    if(socket.connected()){
                        socket.emit(EVENT_ENTER_ROOM,id)
                    }
                }
            }
            override fun onBeaconLost(beacon: Beacon) {
                if(beacon==currRoom){
                    currRoom=null
                    textview_party.text="Not in room"
                    textview_room.text="No song played"
                    constraint_layout_main.setBackgroundColor(resources.getColor(R.color.white))
                    (recyclerview_songs.adapter as PartyAdapter).updateItems(ArrayList(), ArrayList())
                    toast("Left room: "+beacon.urlStatus)
                    progressbar_room.visibility=View.VISIBLE
                    if(socket.connected()){
                        socket.emit(EVENT_ENTER_ROOM,"")
                    }
                }
            }
        })
        button_add_song.setOnClickListener {
            startActivityForResult(SearchActivity.getIntent(context), RESULT_SEARCH)
        }

        button_right.setOnClickListener {
            if(socket.connected()){
                socket.emit(EVENT_SKIP_SONG)
            }
        }

        button_pause.setOnClickListener {
            isPlaying=!isPlaying
            if(isPlaying){
                button_pause.setImageResource(R.drawable.play)
            }else{
                button_pause.setImageResource(R.drawable.pause)
            }
            val payload=if(isPlaying) EVENT_PAUSE else EVENT_PLAY_SONG

            socket.emit(payload)

        }
        recyclerview_songs.adapter=PartyAdapter(ArrayList(), ArrayList())
        recyclerview_songs.layoutManager=LinearLayoutManager(this)

    }


    fun setUpSocket(port:String){
        socket= IO.socket(port)
        socket.on(Socket.EVENT_CONNECT,{
            Log.d(TAG,"Socket connected")
            socket.emit("HELLO","HELLO")

            if(currRoom!=null){
                val id=currRoom!!.urlStatus.toString().split(".")[1]
                socket.emit(EVENT_ENTER_ROOM,id)
            }
        }).on(EVENT_SONG_INFO,{
            val obj=it[0] as JSONObject

            if(obj.has("currentSong")){
                val status=obj.getString("currentStatus")
                val currentSong=obj.getString("currentSong")
                val resSongs=ArrayList<String>()
                val resUris=ArrayList<String>()
                val nextSongs=obj.getJSONArray("nextSongs")
                val nextUris=obj.getJSONArray("nextUris")



                for(i in 0..nextSongs.length()-1){
                    resSongs.add(nextSongs[i].toString())
                }

                for(i in 0..nextUris.length()-1){
                    resUris.add(nextUris[i].toString())
                }

                runOnUiThread {
                    textview_code.text=status
                    textview_room.text=if(currentSong=="") "No song playing" else currentSong
                    (recyclerview_songs.adapter as PartyAdapter).updateItems(resSongs,resUris)
                }
            }


        }).on(EVENT_SONG_STATUS,{
            val status=it[0].toString()

            if (status=="playing"){
                button_pause.setImageResource(R.drawable.play)
                textview_code.text="playing"
            }else{
                button_pause.setImageResource(R.drawable.pause)
                textview_code.text="paused"
            }

        })

        socket.on(Socket.EVENT_DISCONNECT,{
            Log.d(TAG,"Socket disconnected")
        })

        socket.connect()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.actionSettings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class PartyAdapter(private var songs:ArrayList<String>, private var uris:ArrayList<String>):RecyclerView.Adapter<PartyViewHolder>(){


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyViewHolder {
            return PartyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.viewholder_song,parent,false))
        }

        fun updateItems(songs:ArrayList<String>,uris:ArrayList<String>){
            this.songs=songs
            this.uris=uris
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = songs.size

        override fun onBindViewHolder(holder: PartyViewHolder, position: Int) {
            val view=holder.itemView
            val name=songs[position]
            val uri=uris[position]

            view.textview_song_name.text=name

            if(voteMap[name]!=null){
                if(voteMap[name]=="upvote"){
                    view.button_vote_up.setImageResource(R.drawable.fire_up)
                    view.button_vote_down.setImageResource(R.drawable.fire_down_empty)
                }else{
                    view.button_vote_down.setImageResource(R.drawable.fire_down)
                    view.button_vote_up.setImageResource(R.drawable.fire_up_empty)
                }
            }else{
                view.button_vote_down.setImageResource(R.drawable.fire_down_empty)
                view.button_vote_up.setImageResource(R.drawable.fire_up_empty)
            }

            view.button_vote_down.setOnClickListener {
                view.button_vote_down.setImageResource(R.drawable.fire_down)
                view.button_vote_up.setImageResource(R.drawable.fire_up_empty)
                if(socket.connected()){
                    val obj = JSONObject()
                    obj.put("uri", uri)
                    obj.put("name", name)

                    voteMap[name]="downvote"
                    socket.emit(EVENT_DOWNVOTE,obj)
                }
            }

            view.button_vote_up.setOnClickListener {
                view.button_vote_up.setImageResource(R.drawable.fire_up)
                view.button_vote_down.setImageResource(R.drawable.fire_down_empty)

                if(socket.connected()){
                    val obj = JSONObject()
                    obj.put("uri", uri)
                    obj.put("name", name)
                    voteMap[name]="upvote"
                    socket.emit(EVENT_UPVOTE,obj)
                }

            }
        }
    }

    private inner class PartyViewHolder(v:View):RecyclerView.ViewHolder(v){


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RESULT_SEARCH->{
                when(resultCode){
                    SearchActivity.RESULT_SONG_CHOSEN->{
                        val songUri=data?.getStringExtra(SearchActivity.EXTRA_SONG_ID)
                        val songName=data?.getStringExtra(SearchActivity.EXTRA_SONG_NAME)
                        if(socket.connected()){
                            val obj = JSONObject()
                            obj.put("uri", songUri)
                            obj.put("name", songName)


                            requestMap.add(songName!!)
                            socket.emit(EVENT_UPVOTE,obj)
                        }
                    }
                }
            }
        }

    }
}