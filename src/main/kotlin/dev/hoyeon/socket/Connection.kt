package dev.hoyeon.socket

import dev.hoyeon.objects.UserID
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

typealias ConnID = Int

data class Connection(
    val userId: UserID,
    val session: WebSocketServerSession
) {

    val id: ConnID
    var roomId: UUID? = null

    fun getUserName(): String =
        usernames[id]!!

    suspend fun purge(reason: CloseReason = CloseReason(CloseReason.Codes.NORMAL, "BYE")) {
        roomId?.let(ChatRoomManager::getRoomById)?.ifPresent { room ->
            room.members.removeIf { it.connection.id == id }
        }
        val nUserName = getUserName()
        reconnMap[userId] = id to nUserName
        usernames -= id
        if (session.isActive)
            session.close(reason)
    }

    init {
        val username: String
        if (userId in reconnMap) {
            val reconnInfo = reconnMap[userId]!!
            username = reconnInfo.second
            id = reconnInfo.first

            reconnMap -= userId
        } else {
            username = nameGenerator.getRandomNickname()
            id = lastId.getAndIncrement()
        }
        usernames[id] = username
    }

    companion object {
        val lastId = AtomicInteger(0)

        private val usernames: MutableMap<ConnID, String> = hashMapOf()
        private val reconnMap: MutableMap<UserID, Pair<ConnID, String>> =
            ExpiringMap
                .builder()
                .maxSize(128)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(15, TimeUnit.MINUTES)
                .build()
        private val nameGenerator = NicknameGenerator()
    }
}
