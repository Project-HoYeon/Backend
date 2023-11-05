package dev.hoyeon.db.services

import dev.hoyeon.objects.Post
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

interface PostRepository {

    suspend fun <T> dbQuery(block: suspend () -> T): T

    suspend fun create(post: Post)
}