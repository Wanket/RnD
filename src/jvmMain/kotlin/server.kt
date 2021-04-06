import api.api
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(CallLogging) {
            level = Level.DEBUG
        }

        install(ContentNegotiation) {
            json()
        }

        routing {
            resource("/", "/html/index.html")

            static("/static") {
                resources()
            }

            api()
        }
    }.start(wait = true)
}
