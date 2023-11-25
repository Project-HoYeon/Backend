package dev.hoyeon.db.services

import dev.hoyeon.objects.StudentID
import dev.hoyeon.objects.User
import dev.hoyeon.objects.UserID

interface UserRepository {

    suspend fun <T> dbQuery(block: suspend () -> T): T

    suspend fun create(user: User): UserID

    suspend fun read(id: UserID): User?

    suspend fun read(studentID: StudentID): User?

    suspend fun studentIDExists(studentID: StudentID): Boolean

    suspend fun checkAuth(studentID: StudentID, password: String): Boolean

    suspend fun update(id: UserID, user: User)

    suspend fun delete(id: UserID)
}