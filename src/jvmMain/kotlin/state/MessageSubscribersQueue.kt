package state

import datatypes.Message
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.util.*

object MessageSubscribersQueue {
    class Subscriber(private val session: DefaultWebSocketSession) : Closeable {
        override fun close() {
            subscribers.remove(session)
        }
    }

    fun subscribe(session: DefaultWebSocketSession): Subscriber {
        subscribers.add(session)

        return Subscriber(session)
    }

    suspend fun sendToSubscribers(message: Message) = subscribers.forEach { it.send(Json.encodeToString(message)) }

    private val subscribers = Collections.synchronizedSet<DefaultWebSocketSession>(LinkedHashSet())
}
