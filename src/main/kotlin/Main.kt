import bot.WeatherBot
import data.remote.RetroFitClient
import data.remote.RetrofitType
import data.remote.api.ReverseGeocodingApi
import data.remote.api.WeatherApi
import data.remote.repository.WeatherRepository

fun main() {
    val weatherRetrofit = RetroFitClient().getRetrofit(RetrofitType.WEATHER)
    val reversRetrofit = RetroFitClient().getRetrofit(RetrofitType.REVERSE_GEOCODER)
    val weatherApi = RetroFitClient().getWeatherApi(weatherRetrofit)
    val reverseApi = RetroFitClient().getReverseGeocodingApi(reversRetrofit)
    val weatherRepository = WeatherRepository(weatherApi, reverseApi)
    val weatherBot = WeatherBot(weatherRepository).createBot()
    weatherBot.startPolling()
}