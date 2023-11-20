package dev.hoyeon.objects

import dev.hoyeon.socket.Connection
import kotlinx.serialization.Serializable

@Serializable
data class ChatUser(
    val id  : Int,
    val name: String,
) {
    companion object {
        fun from(connection: Connection): ChatUser =
            ChatUser(connection.id, connection.getUserName())
    }
}
