package dev.hoyeon.socket.chat

import dev.hoyeon.objects.ChatUser
import dev.hoyeon.serializers.UUIDSerializer
import dev.hoyeon.utils.currentTimeMillis
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.util.UUID

@Serializable
@JsonClassDiscriminator("type")
sealed class Chat {

    @SerialName("ts")
    val timestamp: Long = currentTimeMillis()

    @Serializable
    @SerialName("sc")
    data class SentChat(
        val content: String,
    ): Chat()

    @Serializable
    @SerialName("rc")
    data class ReceivingChat(
        val sender: ChatUser,
        val content: String,
    ): Chat()

    @Serializable
    @SerialName("lightning")
    data class LightningChat(
        val sender: ChatUser,
        val roomID  : @Serializable(with = UUIDSerializer::class) UUID,
        val roomName: String,
    ): Chat()
}
