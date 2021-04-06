package api

import datatypes.User
import db.tables.User as UserTable
import db.Connection
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.select
import datatypes.Result
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.sessions.*
import security.PasswordHasher

fun Route.authorization() = post(SharedConstants.AUTHORIZATION) {
    call.receiveOrBadRequest<User> {
        if (it.name.length > 150) {
            call.respond(HttpStatusCode.BadRequest)

            return@post
        }

        val user = Connection.loggedTransaction { UserTable.select { UserTable.userName eq it.name }.firstOrNull() }

        if (user == null ||
            !user[UserTable.password].contentEquals(PasswordHasher.getHash(user[UserTable.salt], it.password))
        ) {
            call.respond(Result(StatusCode.InvalidUsernameOrPassword))

            return@post
        }

        call.sessions.set(SharedConstants.COOKIE_SESSION_NAME, UserIdPrincipal(user[UserTable.userName]))

        call.respond(Result(StatusCode.Success))
    }
}
