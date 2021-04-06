package db

import db.tables.Post
import db.tables.User
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.util.*

object Connection {
    private val tables = arrayOf(
        User,
        Post
    )

    init {
        Properties().apply {
            load(Thread.currentThread().contextClassLoader.getResourceAsStream("db.properties"))

            Database.connect(
                getProperty("db.url"),
                getProperty("db.driver"),
                getProperty("db.user"),
                getProperty("db.password")
            )
        }

        GlobalScope.launch {
            loggedTransaction { SchemaUtils.create(*tables) }
        }
    }

    suspend inline fun <reified T> loggedTransaction(
        transactionIsolation: Int = null.transactionManager.defaultIsolationLevel,
        crossinline statement: Transaction.() -> T
    ) = withContext(Dispatchers.IO) {
        transaction(transactionIsolation, null.transactionManager.defaultRepetitionAttempts) {
            addLogger(Slf4jSqlDebugLogger)

            statement()
        }
    }
}
