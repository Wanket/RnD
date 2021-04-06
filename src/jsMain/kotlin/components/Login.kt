package components

import SharedConstants
import api.StatusCode
import datatypes.Result
import datatypes.User
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.role
import network.client
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.button
import react.dom.div
import react.dom.input
import react.dom.label
import react.dom.p
import styled.styledDiv

external interface LoginState : RState {
    var isLogin: Boolean

    var userName: String
    var password: String

    var error: String
}

external interface LoginProps : RProps {
    var onLoginSuccess: () -> Unit
}

@JsExport
class LoginComponent : RComponent<LoginProps, LoginState>() {
    init {
        state.apply {
            isLogin = true
            error = ""
        }
    }

    override fun RBuilder.render() {
        div(classes = "min-vh-100 bg-dark d-flex align-items-center justify-content-center") {
            div(classes = "bg-light p-3 rounded-3") {

                if (state.error.isNotEmpty()) {
                    div(classes = "alert alert-danger") {
                        attrs {
                            role = "alert"
                        }
                        +state.error
                    }
                }

                div(classes = "mb-3") {
                    label(classes = "form-label") { +"Логин" }
                    input(classes = "form-control") {
                        attrs {
                            maxLength = "150"
                            onChangeFunction = { setState { userName = (it.target as HTMLInputElement).value } }
                        }
                    }
                }

                div(classes = "mb-3") {
                    label(classes = "form-label") { +"Пароль" }
                    input(classes = "form-control", type = InputType.password) {
                        attrs {
                            onChangeFunction = { setState { password = (it.target as HTMLInputElement).value } }
                        }
                    }
                }

                div(classes = "row") {
                    div(classes = "col") {
                        button(classes = "btn btn btn-link") {
                            +if (state.isLogin) "Регистрация" else "Вход"
                            attrs {
                                onClickFunction = { setState { isLogin = !isLogin } }
                            }
                        }
                    }

                    div(classes = "col") {
                        div(classes = "float-end align-self-center") {
                            button(classes = "btn btn-primary") {
                                +if (state.isLogin) "Войти" else "Зарегестрироваться"
                                attrs {
                                    onClickFunction = { onLoginClicked() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onLoginClicked() = state.apply {
        if (userName === undefined || userName.isBlank() || password === undefined || password.isBlank()) {
            error = "Не все требуемые данные введены"

            return@apply
        }

        GlobalScope.launch {
            client.post<Result> {
                url("${SharedConstants.API_PREFIX}/${if (isLogin) SharedConstants.AUTHORIZATION else SharedConstants.REGISTRATION}")
                body = User(userName, password)
            }.let {
                when (it.status) {
                    StatusCode.InvalidUsernameOrPassword -> setState { error = "Неверное имя пользователя или пароль" }
                    StatusCode.UserAlreadyExist -> setState { error = "Пользователь уже существует" }
                    StatusCode.Success -> props.onLoginSuccess()
                    else -> throw IllegalStateException("Unexpected status code")
                }
            }
        }
    }
}

fun RBuilder.login(propsBuilder: LoginProps.() -> Unit) {
    child(LoginComponent::class) { attrs(propsBuilder) }
}
