package dev.hoyeon.plugins

import dev.hoyeon.objects.ChatUser
import dev.hoyeon.socket.ChatHandler
import dev.hoyeon.socket.ChatRoomManager
import dev.hoyeon.socket.Connection
import dev.hoyeon.socket.PacketHandler
import dev.hoyeon.socket.chat.ChatRoom
import dev.hoyeon.socket.packet.Packet
import dev.hoyeon.utils.getKoinInstance
import io.github.cdimascio.dotenv.Dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*
import kotlin.jvm.optionals.getOrNull

private val json = Json {
    ignoreUnknownKeys = true
}

private val logger = KotlinLogging.logger {  }
private val globalChannelID = ChatRoomManager.createRoom("Global").id

fun Application.configureSockets() {
    val dotenv = getKoinInstance<Dotenv>()

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false

        contentConverter = KotlinxWebsocketSerializationConverter(Json {
            encodeDefaults = false
        })
    }
    routing {
        route(dotenv["BASE_ROUTE_PATH", "/"]) {
            authenticate("jwt-socket") {
                route("/chat") {
                    get {
                        call.respond(ChatHandler.getChatLog())
                    }
                    webSocket("/global") {
                        setupIncoming(isGlobal = true)
                    }
                    webSocket("/private/{id}") {
                        setupIncoming(isGlobal = false)
                    }
                }
            }
        }
    }
}

private suspend fun WebSocketServerSession.setupIncoming(isGlobal: Boolean) {
    val room = if (isGlobal)
        ChatRoomManager.getRoomById(globalChannelID).get()
    else {
        val roomID = call.parameters["id"]!!.let(UUID::fromString)
        val room = ChatRoomManager.getRoomById(roomID).getOrNull()
        if (room == null) {
            close(reason = CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid room id"))
            return
        }
        room
    }
    val conn = Connection(this).also {
        it.roomId = room.id
    }
    room.members += ChatRoom.ConnectionUser(
        connection = conn,
        user       = ChatUser.from(conn)
    )

    logger.debug { "Socket connected as id #${conn.id}" }
    incoming
        .receiveAsFlow()
        .buffer(Channel.UNLIMITED)
        .onCompletion {
            PacketHandler.fireEvent(Packet.DisconnectEvent())
            conn.purge()
            logger.debug { "Socket connection #${conn.id} disconnected" }
        }
        .collect { frame ->
            when(frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    try {
                        val packet: Packet = json.decodeFromString(text)
                        packet.connection = conn
                        PacketHandler.fireEvent(packet)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else -> {}
            }
        }
}