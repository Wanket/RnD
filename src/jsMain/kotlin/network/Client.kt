package network

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*

val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }

    install(WebSockets)

    defaultRequest {
        port = 8080
        contentType(ContentType.Application.Json)
    }
}
