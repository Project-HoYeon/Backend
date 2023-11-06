package dev.hoyeon.objects

import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

typealias PostID = Int

@Serializable
sealed interface Post {
    val id          : PostID
    val title       : String
    val content     : String
    val isAnonymous : Boolean
    val writtenAt   : Long
}

@Serializable
data class DefaultPost(
    override val id          : PostID,
    @Serializable(with = UUIDSerializer::class)
    val authorId    : UUID,
    override val title       : String,
    override val content     : String,
    override val isAnonymous : Boolean,
    override val writtenAt   : Long,
): KoinComponent, Post {

    private val userRepo: UserRepository by inject()

    suspend fun getAuthor(): User? =
        userRepo.read(authorId)

    fun toAnonymousPost(): AnonymousPost =
        AnonymousPost(id, title, content, isAnonymous, writtenAt)
}

@Serializable
data class AnonymousPost(
    override val id          : PostID,
    override val title       : String,
    override val content     : String,
    override val isAnonymous : Boolean,
    override val writtenAt   : Long,
): KoinComponent, Post