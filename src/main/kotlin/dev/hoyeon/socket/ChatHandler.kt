package dev.hoyeon.socket

import dev.hoyeon.objects.ChatUser
import dev.hoyeon.socket.chat.Chat
import dev.hoyeon.socket.chat.ChatRoom
import dev.hoyeon.socket.packet.Packet
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {  }

class ChatHandler {

    suspend fun handlePacket(packet: Packet) {
        val connection = packet.connection
        when(packet) {
            is Packet.HelloPacket -> {
                packet.connection.userId = packet.userID
                logger.debug { "Bound user id ${packet.userID} with conn id #${packet.connection.id}" }
            }
            is Packet.ChatSentPacket -> {
                val roomId = connection.roomId ?: return
                val room = ChatRoomManager.getRoomById(roomId).get()

                val nChat = Chat.ReceivingChat(
                    sender = ChatUser.from(connection),
                    content = (packet.chat as Chat.SentChat).content
                )
                val nPacket = Packet.ChatReceivePacket(nChat)

                chatLog.addLast(nChat)

                room.broadcast(nPacket, connection.id)
            }
            else -> {}
        }
    }

    init {
        PacketHandler.apply {
            on(this, ::handlePacket)
        }
    }

    companion object {
        private val chatLog: ArrayDeque<Chat> = ArrayDeque(50)

        fun getChatLog(): List<Chat> =
            chatLog
    }
}