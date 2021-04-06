package components

import api.StatusCode
import datatypes.Message
import datatypes.MessageToSend
import datatypes.Result
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.css.WordBreak
import kotlinx.css.wordBreak
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import network.client
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import styled.css
import styled.styledTbody

external interface MessagesState : RState {
    var messages: MutableList<Message>

    var messageToSend: String
}

external interface MessagesProps : RProps {
    var onLoginRequired: () -> Unit
}

@JsExport
class MessagesComponent : RComponent<MessagesProps, MessagesState>() {
    init {
        state.messages = mutableListOf()
    }

    override fun RBuilder.render() {
        div(classes = "min-vh-100 bg-dark d-flex align-items-center justify-content-center") {
            div(classes = "container p-3") {
                div(classes = "bg-light p-3 mb-3 rounded-3 row overflow-auto") {
                    table(classes = "table table-striped table-bordered") {
                        thead {
                            tr {
                                th { +"Дата" }
                                th { +"Автор" }
                                th { +"Текст" }
                            }
                        }

                        styledTbody {
                            css { wordBreak = WordBreak.breakAll }

                            for (message in state.messages) {
                                tr {
                                    td { +Instant.fromEpochSeconds(message.datetime).toLocalDateTime(TimeZone.currentSystemDefault()).toString() }
                                    td { +message.author }
                                    td { +message.text }
                                }
                            }
                        }
                    }
                }

                div("row bg-light p-3 rounded-3") {
                    div("col") {
                        input(classes = "form-control") {
                            attrs {
                                onChangeFunction = { setState { messageToSend = (it.target as HTMLInputElement).value } }
                            }
                        }
                    }

                    div("col-auto") {
                        button(classes = "btn btn-primary float-end") {
                            +"Отправить"
                            attrs {
                                onClickFunction = { onSendClicked() }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onSendClicked() = state.apply {
        if (messageToSend === undefined || messageToSend.isBlank()) {
            return@apply
        }

        GlobalScope.launch {
            client.post<Result> {
                url("${SharedConstants.API_PREFIX}/${SharedConstants.SEND_MESSAGE}")
                body = MessageToSend(messageToSend)
            }.let {
                if (it.status != StatusCode.Success) {
                    throw IllegalStateException("Unexpected status code")
                }
            }
        }
    }

    override fun componentDidMount() {
        webSocketJob = GlobalScope.launch {
            client.ws("${SharedConstants.API_PREFIX}/${SharedConstants.MESSAGE_SOCKET}") {
                for (frame in incoming) {
                    if (frame !is Frame.Text) {
                        throw IllegalStateException("Unexpected frame type")
                    }

                    Json.decodeFromString<Message>(frame.readText()).let { setState { messages.add(it) } }
                }
            }
        }
    }

    override fun componentWillUnmount() {
        GlobalScope.launch { webSocketJob.cancel() }
    }

    private lateinit var webSocketJob: Job
}


fun RBuilder.messages(propsBuilder: MessagesProps.() -> Unit) {
    child(MessagesComponent::class) { attrs(propsBuilder) }
}
