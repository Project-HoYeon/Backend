package dev.hoyeon.objects

import dev.hoyeon.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

typealias StudentID = Int

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id          : UUID,
    val studentID   : StudentID,
    val name        : String,
    val pwHash      : String,
)
