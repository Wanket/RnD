package api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*

suspend inline fun <reified T : Any>ApplicationCall.receiveOrBadRequest(onSuccess: (T) -> Unit) {
    runCatching { receive<T>() }.apply {
        onFailure { respond(HttpStatusCode.BadRequest) }
        onSuccess(onSuccess)
    }
}
