import api.StatusCode
import components.login
import components.messages
import datatypes.Result
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.client
import react.*
import react.dom.render

external interface AppState : RState {
    var isLogged: Boolean
}

@JsExport
class AppComponent : RComponent<RProps, AppState>() {
    init {
        GlobalScope.launch {
            try {
                client.get<Result>("${SharedConstants.API_PREFIX}/${SharedConstants.PING}").status == StatusCode.Success
            } catch (e: ResponseException) {
                if (e.response.status == HttpStatusCode.Unauthorized) false else throw e
            }.let {
                setState { isLogged = it }
            }
        }
    }

    override fun RBuilder.render() {
        if (state.isLogged === undefined) {
            return
        }

        if (state.isLogged) {
            messages {
                onLoginRequired = { setState { isLogged = false } }
            }
        } else {
            login {
                onLoginSuccess = { setState { isLogged = true } }
            }
        }
    }
}

fun main() {
    render(document.getElementById("root")) {
        child(AppComponent::class) {}
    }
}
