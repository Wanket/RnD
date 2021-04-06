package datatypes

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val datetime: Long,
    val author: String,
    val text: String
)

@Serializable
data class MessageToSend(
    val text: String
)
