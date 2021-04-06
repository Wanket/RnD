package db.tables

import org.jetbrains.exposed.sql.Table
import kotlin.random.Random

object User : Table() {
    val userName = varchar("user_name", 150)
    val password = binary("password", 32)
    val salt = binary("salt", 32)

    override val primaryKey: PrimaryKey = PrimaryKey(userName)
}
