package dev.hoyeon.db.services.internal

import dev.hoyeon.cypher.SHA256
import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.objects.StudentID
import dev.hoyeon.objects.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepositoryImpl(private val database: Database): UserRepository {
    object Users : Table("users") {
        val id = uuid("id")
        val name = varchar("name", length = 50)
        val studentID = integer("stdID")
        val pwHash = varchar("pwHash", 64)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    override suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun create(user: User): UUID = dbQuery {
        Users.insert {
            it[id] = user.id
            it[name] = user.name
            it[studentID] = user.studentID
            it[pwHash] = user.pwHash
        }[Users.id]
    }

    override suspend fun read(id: UUID): User? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { User(it[Users.id], it[Users.studentID], it[Users.name], it[Users.pwHash]) }
                .singleOrNull()
        }
    }

    override suspend fun read(studentID: StudentID): User? {
        return dbQuery {
            Users.select { Users.studentID eq studentID }
                .map { User(it[Users.id], it[Users.studentID], it[Users.name], it[Users.pwHash]) }
                .singleOrNull()
        }
    }

    override suspend fun studentIDExists(studentID: StudentID): Boolean {
        return dbQuery {
            Users.select { Users.studentID eq studentID }
                .singleOrNull()
        } != null
    }

    override suspend fun checkAuth(studentID: StudentID, password: String): Boolean {
        val query = dbQuery {
            Users.select { Users.studentID eq studentID }
                .singleOrNull()
        } ?: return false

        val hash = SHA256.encrypt(password)
        return query[Users.pwHash] == hash
    }

    override suspend fun update(id: UUID, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[studentID] = user.studentID
            }
        }
    }

    override suspend fun delete(id: UUID) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}
