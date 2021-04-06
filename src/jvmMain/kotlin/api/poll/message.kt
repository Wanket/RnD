package api.poll

import SharedConstants
import datatypes.Message
import db.Connection
import db.tables.Post
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.selectAll
import state.MessageSubscribersQueue

fun Route.messageSocket() = webSocket(SharedConstants.MESSAGE_SOCKET) {
    Connection.loggedTransaction {
        Post.selectAll().map { Message(it[Post.datetime].epochSecond, it[Post.author], it[Post.message]) }
    }.forEach {
        send(Json.encodeToString(it))
    }

    MessageSubscribersQueue.subscribe(this).use {
        for (frame in incoming) {
            return@webSocket
        }
    }
}
