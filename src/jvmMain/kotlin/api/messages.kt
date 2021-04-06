package api

import SharedConstants
import datatypes.Message
import datatypes.MessageToSend
import datatypes.Result
import db.Connection
import db.tables.Post
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.insert
import state.MessageSubscribersQueue

fun Route.sendMessage() = post(SharedConstants.SEND_MESSAGE) {
    call.receiveOrBadRequest<MessageToSend> { message ->
        if (message.text.length > 280) {
            call.respond(HttpStatusCode.BadRequest)

            return@post
        }

        Connection.loggedTransaction {
            Post.insert {
                it[author] = call.sessions.get<UserIdPrincipal>()!!.name
                it[Post.message] = message.text
            }
        }.let {
            launch {
                MessageSubscribersQueue.sendToSubscribers(
                    Message(
                        it[Post.datetime].epochSecond,
                        it[Post.author],
                        it[Post.message]
                    )
                )
            }
        }

        call.respond(Result(StatusCode.Success))
    }
}
