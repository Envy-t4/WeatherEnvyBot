package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.*
import data.remote.API_KEY
import data.remote.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val BOT_ANSWER_TIMEOUT = 30
private const val BOT_TOKEN = ""
private const val GIF_WAITING_URL = "https://media.giphy.com/media/Q0MrhO9BUSxKR8RdZC/giphy.gif"

class WeatherBot(private val weatherRepository: WeatherRepository) {

    private lateinit var country: String
    private var _chatId: ChatId? = null
    private val chatId by lazy { requireNotNull(_chatId) }

    fun createBot(): Bot {
        return bot {
            timeout = BOT_ANSWER_TIMEOUT
            token = BOT_TOKEN
            logLevel = LogLevel.All()

            dispatch {
                setUpCommands()
                setUpCallbacks()
            }
        }
    }

    private fun Dispatcher.setUpCallbacks() {
        callbackQuery(callbackData = "getMyLocation") {
            bot.sendMessage(chatId = chatId, text = "Send me your location")
            location {
                CoroutineScope(Dispatchers.IO).launch {
                    country = weatherRepository.getReversedGeocodingCountryName(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString(), format = "json"
                    ).address.country

                    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = "Yes, correct",
                                callbackData = "yes_label"
                            )
                        )
                    )
                    bot.sendMessage(
                        chatId = chatId,
                        text = "Your city is $country?",
                        replyMarkup = inlineKeyboardMarkup
                    )
                }
            }
        }

        callbackQuery(callbackData = "enterManually") {
            bot.sendMessage(chatId = chatId, text = "Enter your city")
            message(Filter.Text) {
                country = message.text.toString()

                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Yes, correct",
                            callbackData = "yes_label"
                        )
                    )
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = "Your city is $country?\n If not, please enter your city again",
                    replyMarkup = inlineKeyboardMarkup
                )
            }
        }

        callbackQuery(callbackData = "yes_label") {
            bot.apply {
                sendAnimation(chatId = chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
                sendMessage(chatId = chatId, text = "Searching for weather...")
                sendChatAction(chatId = chatId, action = com.github.kotlintelegrambot.entities.ChatAction.TYPING)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    API_KEY,
                    country,
                    "no"
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = """
                        ☁️ Cloud: ${currentWeather.current.cloud} %
                        🌡️ Temperature: ${currentWeather.current.temp_c} C
                        😨 Feels like: ${currentWeather.current.feelslike_c} C
                        ✅ Condition: ${currentWeather.current.condition.text}
                        💧 Humidity: ${currentWeather.current.humidity} %
                        💨 Wind speed: ${currentWeather.current.wind_kph} km/h
                        🧭 Wind direction: ${currentWeather.current.wind_dir}
                        🎈 Pressure: ${currentWeather.current.pressure_mb} mb
                        🌧️ Precipitation: ${currentWeather.current.precip_mm} mm
                        🌗 Is it day: ${if (currentWeather.current.is_day == 1) "Yes" else "No"}
                        🪟 Visibility: ${currentWeather.current.vis_km} km
                        🌆 City: ${currentWeather.location.name}
                        🏴󠁥󠁳󠁰󠁶󠁿 Country: ${currentWeather.location.country}
                        ⏱️ Last updated: ${currentWeather.current.last_updated}
                    """.trimIndent()
                )

                bot.sendMessage(chatId = chatId, text = "You can request another city by typing /weather")

                country = ""

                //|UV index: ${currentWeather.current.uv}

            }
        }
    }


    private fun Dispatcher.setUpCommands() {
        command("start") {
            _chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "Hello, ${message.from?.firstName}! I'm a weather bot. " +
                        "I can show you the weather in any city you want. " +
                        "Just type /weather and the name of the city you want to know the weather in."
            )
        }

        command("weather") {
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Define my city (For mobile devices)",
                        callbackData = "getMyLocation"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Enter city manually",
                        callbackData = "enterManually"
                    )
                )
            )
            bot.sendMessage(
                chatId = chatId,
                text = "How do you want to define your city?",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
}




