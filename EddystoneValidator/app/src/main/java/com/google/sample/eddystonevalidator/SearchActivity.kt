package com.google.sample.eddystonevalidator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.sample.eddystonevalidator.service.model.ItemsItem
import com.google.sample.eddystonevalidator.service.model.SearchResponse
import com.google.sample.eddystonevalidator.service.service.RetrofitClient
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.viewholder_song.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rutgers.edu.bonfire.extensions.toast
import rutgers.edu.bonfire.service.service.SpotifyService
import java.util.concurrent.TimeUnit

class SearchActivity : Activity() {
    private lateinit var songAdapter:SongAdapter
    private lateinit var service: SpotifyService
    companion object {
        val TAG="SearchActivity"
        val EXTRA_SONG_ID="SearchActivity.EXTRA_SONG_ID"
        val EXTRA_SONG_NAME="SearchActivity.EXTRA_SONG_NAME"

        const val RESULT_SONG_CHOSEN=1
        fun getIntent(c:Context):Intent{
            val intent=Intent(c,SearchActivity::class.java)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        service=RetrofitClient.getMySpotifyRetrofit().create(SpotifyService::class.java)
        recyclerview_search_songs.layoutManager=LinearLayoutManager(this)

        songAdapter=SongAdapter(ArrayList())
        recyclerview_search_songs.adapter=songAdapter

        RxTextView.textChanges(edittext_search)
                .filter { it.length>3 }
                .debounce(300,TimeUnit.MILLISECONDS)
                .map { it.toString() }
                .subscribe {
                    service.search(it).enqueue(object :Callback<SearchResponse>{
                        override fun onFailure(call: Call<SearchResponse>?, t: Throwable?) {
                            Log.d(TAG,t!!.message)
                        }
                        override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                            if(response.isSuccessful){
                                val tracks=response.body()?.tracks?.items
                                songAdapter.updateSongs(ArrayList(tracks))
                            }else{
                                Log.d(TAG,"Failed to fetch")
                                toast("Failed to fetch")
                            }
                            //
                        }
                    })
                }
    }

    private inner class SongAdapter(private var songs:ArrayList<ItemsItem>):RecyclerView.Adapter<PartyViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):PartyViewHolder {
            return PartyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.viewholder_search_song,parent,false))
        }

        override fun getItemCount(): Int = songs.size

        override fun onBindViewHolder(holder: PartyViewHolder, position: Int) {
            val item=songs[position]

            holder.itemView.textview_song_name.text=item.name
            var artists=""
            item.artists?.forEach { artists+=it.name+", " }
            artists=artists.substring(0,artists.length-2)
            holder.itemView.textview_song_suggestor.text=artists
            holder.itemView.setOnClickListener {
                Log.d(TAG,"Chosen song uri: "+item.uri )
                intent.putExtra(EXTRA_SONG_ID,item.uri)
                intent.putExtra(EXTRA_SONG_NAME,item.name)
                setResult(RESULT_SONG_CHOSEN,intent)
                finish()
            }
        }

        fun updateSongs(items:ArrayList<ItemsItem>){
            songs=items
            notifyDataSetChanged()
        }
    }

    private inner class PartyViewHolder(v: View):RecyclerView.ViewHolder(v){


    }


}
