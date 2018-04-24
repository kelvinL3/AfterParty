package rutgers.edu.bonfire.service.service

import com.google.sample.eddystonevalidator.service.model.SearchResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Created by hemanth on 12/11/17.
 */


interface SpotifyService{


    companion object {
        const val BASE_URL="https://api.spotify.com"
        var token="BQBTHFMqZ9RQG1HMSbaKd-STh7fZNgnNzkrBvZayT-zML0Zaty9Ocd3F5SrUUnFiTGoXNeMUKoPxKXQrimY"
    }


    //NEWS
    @GET("/v1/search")
    fun search(@Query("q") q:String, @Query("type") type:String="track",@Header("Authorization")auth:String="Bearer "+ token): Call<SearchResponse>


    /*
    @GET("/api/news")
    fun getNewsItem(@Query("id") id:String):Single<NewsResponse>

    //TOURS
    @GET("api/tours")
    fun getTours():Single<AllToursResponse>

    @GET("api/tours")
    fun getTourItem(@Query("id") id:String):Single<TourResponse>

    //LOCATIONS
    @GET("api/locations")
    fun getLocation(@Query("tourId")tourId:String,@Query("stopIndex")stopIndex:String):Single<List<TourItem>>*/

}