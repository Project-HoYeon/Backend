package dev.hoyeon.plugins

import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        route("/chat") {
            webSocket("/global") {
                setupIncoming()
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        outgoing.send(Frame.Text("YOU SAID: $text"))
                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                }
            }
            webSocket("/private") {
                setupIncoming()
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        outgoing.send(Frame.Text("YOU SAID: $text"))
                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                }
            }
        }
    }
}

private suspend fun WebSocketSession.setupIncoming() {
    incoming
        .receiveAsFlow()
        .buffer(Channel.UNLIMITED)
        .collect { frame ->
            when(frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    //EventHandler.fireEventWithScope(Events.Gateway.MessageReceive(conn, text))
                    try {
                        //val command: GatewayCommand = jsonSerializer.decodeFromString(text)
                        //EventHandler.fireEventWithScope(Events.Gateway.Command(conn, command))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else -> {}
            }
        }
}