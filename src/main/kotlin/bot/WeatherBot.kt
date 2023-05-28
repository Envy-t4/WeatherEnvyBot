package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import data.remote.repository.WeatherRepository


private const val BOT_ANSWER_TIMEOUT = 30
private const val BOT_TOKEN = ""

class WeatherBot(private val weatherRepository: WeatherRepository) {

    private var _chatId: ChatId? = null
    private val chatId: ChatId get() = requireNotNull(_chatId)

    fun createBot(): Bot {
        return bot {
            timeout = BOT_ANSWER_TIMEOUT
            token = BOT_TOKEN

            dispatch {
                setUpCommands()
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
    }
}


