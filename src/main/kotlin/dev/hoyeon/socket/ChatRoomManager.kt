package dev.hoyeon.socket

import dev.hoyeon.socket.chat.ChatRoom
import java.util.*

object ChatRoomManager {

    private val rooms: MutableMap<UUID, ChatRoom> = hashMapOf()

    fun getRoomById(id: UUID): Optional<ChatRoom> =
        Optional.ofNullable(rooms[id])

    fun createRoom(name: String): ChatRoom {
        val nid = UUID.randomUUID()
        return ChatRoom(nid, name, arrayListOf()).also {
            rooms[nid] = it
        }
    }
}