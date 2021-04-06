package api

import datatypes.Result
import datatypes.User
import db.Connection
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import security.PasswordHasher
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import kotlin.random.Random
import db.tables.User as UserTable

fun Route.registration() = post(SharedConstants.REGISTRATION) {
    call.receiveOrBadRequest<User> { user ->
        if (user.name.length > 150) {
            call.respond(HttpStatusCode.BadRequest)

            return@post
        }

        Connection.loggedTransaction(TRANSACTION_SERIALIZABLE) {
            if (UserTable.slice().select { UserTable.userName eq user.name }.firstOrNull() != null) {
                return@loggedTransaction StatusCode.UserAlreadyExist
            }

            val salt = Random.nextBytes(32)

            UserTable.insert {
                it[userName] = user.name
                it[this.salt] = salt
                it[password] = PasswordHasher.getHash(salt, user.password)
            }

            return@loggedTransaction StatusCode.Success
        }.let {
            if (it == StatusCode.Success) {
                call.sessions.set(SharedConstants.COOKIE_SESSION_NAME, UserIdPrincipal(user.name))
            }

            call.respond(Result(it))
        }
    }
}
