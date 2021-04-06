package api

import api.poll.poll
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Routing.api() {
    route(SharedConstants.API_PREFIX) {
        application.install(Sessions) {
            cookie<UserIdPrincipal>(SharedConstants.COOKIE_SESSION_NAME, SessionStorageMemory())
        }

        application.install(Authentication) {
            session<UserIdPrincipal> {
                challenge { call.respond(HttpStatusCode.Unauthorized) }
                validate { it }
            }
        }

        authorization()
        registration()

        authenticate {
            sendMessage()

            poll()

            ping()
        }
    }
}
