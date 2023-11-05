package dev.hoyeon.objects

import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@Serializable
data class Post(
    val id      : Int,
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID,
    val title   : String,
    val content : String,
): KoinComponent {

    private val userRepo: UserRepository by inject()

    suspend fun getAuthor(): User? =
        userRepo.read(authorId)
}