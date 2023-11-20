package dev.hoyeon.db.services

import dev.hoyeon.objects.Post
import dev.hoyeon.objects.PostID
import java.util.UUID

interface PostRepository {

    suspend fun <T> dbQuery(block: suspend () -> T): T

    suspend fun create(
        authorID    : UUID,
        authorName  : String,
        title       : String,
        content     : String,
        isAnonymous : Boolean,
        writtenAt   : Long,
    ): PostID

    suspend fun read(postID: PostID): Post?

    suspend fun readAll(from: Long, limit: Int, contentLength: Int): List<Post>

    suspend fun delete(postID: PostID)
}