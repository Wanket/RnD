package datatypes

import api.StatusCode
import kotlinx.serialization.Serializable

@Serializable
class Result(
    val status: StatusCode
)
