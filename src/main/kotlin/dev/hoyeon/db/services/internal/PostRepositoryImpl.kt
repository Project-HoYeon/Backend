package dev.hoyeon.db.services.internal

import dev.hoyeon.db.services.PostRepository
import dev.hoyeon.objects.Post
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class PostRepositoryImpl(private val database: Database): PostRepository {
    object Posts: Table("posts") {
        val postId = integer("postId").autoIncrement()
        val authorId = uuid("authorId")
        val title = text("title")
        val content = largeText("content")

        override val primaryKey = PrimaryKey(postId)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Posts)
        }
    }

    override suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun create(post: Post) {

    }
}