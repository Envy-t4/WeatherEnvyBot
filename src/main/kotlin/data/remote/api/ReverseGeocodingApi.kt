package data.remote.api

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface ReverseGeocodingApi {

    @GET("reverse")
    fun getCurrentWeather(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("format") formatData: String,
    ): Deferred<>
}