package dev.hoyeon.db.services.internal

import dev.hoyeon.db.services.PostRepository
import dev.hoyeon.objects.DefaultPost
import dev.hoyeon.objects.Post
import dev.hoyeon.objects.PostID
import dev.hoyeon.utils.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PostRepositoryImpl(private val database: Database): PostRepository {
    object Posts: Table("posts") {
        val postID = integer("postId").autoIncrement()
        val authorID = uuid("authorId")
        val title = text("title")
        val content = largeText("content")
        val isAnonymous = bool("anon")
        val writtenAt = long("timestamp")

        override val primaryKey = PrimaryKey(postID)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Posts)
        }
    }

    override suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun create(
        authorID    : UUID,
        authorName  : String,
        title       : String,
        content     : String,
        isAnonymous : Boolean,
        writtenAt   : Long,
    ): PostID = dbQuery {
        Posts.insert {
            it[this.authorID]    = authorID
            it[this.title]       = title
            it[this.content]     = content
            it[this.isAnonymous] = isAnonymous
            it[this.writtenAt]   = writtenAt
        }[Posts.postID]
    }

    override suspend fun read(postID: PostID): Post? {
        return dbQuery {
            Posts.select { Posts.postID eq postID }
                .map { it.toPost() }
                .singleOrNull()
        }
    }

    override suspend fun readAll(from: Long, limit: Int, contentLength: Int): List<Post> {
        return dbQuery {
            Posts.selectAll()
                .limit(limit, offset = from)
                .map { it.toPost(contentLength) }
        }
    }

    override suspend fun delete(postID: PostID) {
        dbQuery {
            Posts.deleteWhere { Posts.postID.eq(postID) }
        }
    }

    private fun ResultRow.toPost(contentLength: Int = -1): Post {
        var content = this[Posts.content]
        if (contentLength != -1 && content.length > contentLength)
            content = content.substring(0, contentLength) + "..."

        return DefaultPost(
            id          = this[Posts.postID],
            authorId    = this[Posts.authorID],
            title       = this[Posts.title],
            content     = content,
            writtenAt   = this[Posts.writtenAt],
            isAnonymous = this[Posts.isAnonymous]
        )
    }
}