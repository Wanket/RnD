package datatypes

import kotlinx.serialization.Serializable

@Serializable
class User(
    val name: String,
    val password: String
)
