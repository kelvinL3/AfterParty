package com.google.sample.eddystonevalidator.service.service

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rutgers.edu.bonfire.service.service.SpotifyService

/**
 * Created by hemanth on 4/22/18.
 */


class RetrofitClient{
    companion object {
        var spotifyRetrofit:Retrofit?=null
        fun getMySpotifyRetrofit():Retrofit{
            val gson= GsonBuilder().setLenient().create()

            if(spotifyRetrofit==null){
                spotifyRetrofit = Retrofit.Builder().baseUrl(SpotifyService.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build()
            }
            return spotifyRetrofit!!
        }
    }

}