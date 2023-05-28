package data.remote.repository

import data.remote.api.WeatherApi
import data.remote.api.ReverseGeocodingApi
import data.remote.models.CurrentWeather
import data.remote.models.ReversedCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val reverseGeoCodingApi: ReverseGeocodingApi,
) {

    suspend fun getCurrentWeather(apiKey: String, countryName: String, airQualityData: String): CurrentWeather {
        return withContext(Dispatchers.IO){
            weatherApi.getCurrentWeather(
                '"' + countryName + '"',
                airQualityData)
        }.await()
    }

    suspend fun getReversedGeocodingCountryName(latitude: String, longitude: String, format: String): ReversedCountry {
        return withContext(Dispatchers.IO){
            reverseGeoCodingApi.getCountryNameByCoordinates(latitude, longitude, format)
        }.await()
    }

}
