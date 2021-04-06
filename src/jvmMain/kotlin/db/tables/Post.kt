package db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object Post : IntIdTable() {
    val datetime = timestamp("datetime").clientDefault { Instant.now() }
    val author = reference("author", User.userName)
    val message = varchar("message", 280)
}
