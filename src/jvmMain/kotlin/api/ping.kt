package api

import datatypes.Result
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.ping() = get(SharedConstants.PING) {
    call.respond(Result(StatusCode.Success))
}
