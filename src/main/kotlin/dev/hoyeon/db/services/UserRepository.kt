package dev.hoyeon.db.services

import dev.hoyeon.objects.StudentID
import dev.hoyeon.objects.User
import java.util.UUID

interface UserRepository {

    suspend fun <T> dbQuery(block: suspend () -> T): T

    suspend fun create(user: User): UUID

    suspend fun read(id: UUID): User?

    suspend fun studentIDExists(stdID: StudentID): Boolean

    suspend fun checkAuth(stdID: StudentID, password: String): Boolean

    suspend fun update(id: UUID, user: User)

    suspend fun delete(id: UUID)
}