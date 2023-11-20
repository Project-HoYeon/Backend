package dev.hoyeon.socket

import io.ktor.server.websocket.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

typealias ConnID = Int

data class Connection(val session: WebSocketServerSession) {

    val id: ConnID = lastId.getAndIncrement()
    var roomId: UUID? = null
    lateinit var userId: UUID

    fun getUserName(): String =
        usernames[id]!!

    fun purge() {
        roomId?.let(ChatRoomManager::getRoomById)?.ifPresent { room ->
            room.members.removeIf { it.connection.id == id }
        }
        usernames -= id
    }

    init {
        val username = nameGenerator.getRandomNickname()
        usernames[id] = username
    }

    companion object {
        val lastId = AtomicInteger(0)

        private val usernames: MutableMap<ConnID, String> = hashMapOf()
        private val nameGenerator = NicknameGenerator()
    }
}
