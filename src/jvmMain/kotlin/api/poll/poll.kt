package api.poll

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.websocket.*

fun Route.poll() {
    application.install(WebSockets)

    messageSocket()
}
