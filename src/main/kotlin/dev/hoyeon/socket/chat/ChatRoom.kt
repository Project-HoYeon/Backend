package dev.hoyeon.socket.chat

import dev.hoyeon.objects.ChatUser
import dev.hoyeon.socket.ConnID
import dev.hoyeon.socket.Connection
import dev.hoyeon.socket.packet.Packet
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.websocket.*
import java.util.UUID

private val logger = KotlinLogging.logger {}

data class ChatRoom(
    val id: UUID,
    val name: String,
    val members: MutableList<ConnectionUser>
) {

    suspend fun broadcast(packet: Packet, selfID: ConnID? = null) {
        for (member in members) {
            if (member.connection.id == selfID)
                continue
            try {
                member.connection.session.sendSerialized(packet)
            } catch (e: Exception) {
                logger.error(e) { "Failed to broadcast packet" }
            }
        }
    }

    data class ConnectionUser(
        val connection: Connection,
        val user      : ChatUser,
    ) {

        override fun equals(other: Any?): Boolean {
            return other is ConnectionUser && other.connection.id == connection.id
        }

        override fun hashCode(): Int {
            return connection.id.hashCode()
        }
    }
}