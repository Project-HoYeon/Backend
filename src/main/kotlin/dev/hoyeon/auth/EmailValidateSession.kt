package dev.hoyeon.auth

import dev.hoyeon.objects.StudentID
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.UUID
import java.util.concurrent.TimeUnit

object EmailValidateSession {

    private val studentIDMap: MutableMap<StudentID, UUID> = hashMapOf()
    private val sessions: MutableMap<UUID, Session> = ExpiringMap.builder()
        .expirationListener<UUID, Session> { _, value ->
            studentIDMap -= value.studentID
        }
        .maxSize(256)
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(5, TimeUnit.MINUTES)
        .build()

    fun createSession(studentID: StudentID): UUID {
        val nid = UUID.randomUUID()
        sessions[nid] = Session(studentID)
        studentIDMap[studentID] = nid

        return nid
    }

    fun isSessionValid(id: UUID): Boolean =
        id in sessions

    fun validateSession(id: UUID): Boolean {
        if (!isSessionValid(id))
            return false

        sessions[id]!!.isValidated = true
        (sessions as ExpiringMap).resetExpiration(id)
        return true
    }

    fun hasSessionOfID(studentID: StudentID): Boolean =
        studentID in studentIDMap

    fun isIDValidated(studentID: StudentID): Boolean {
        return studentIDMap[studentID]?.let { uuid ->
            sessions[uuid]?.isValidated
        } == true
    }

    fun removeSession(studentID: StudentID) {
        if (!hasSessionOfID(studentID))
            return
        val uuid = studentIDMap[studentID]!!
        sessions -= uuid
        studentIDMap -= studentID
    }

    private data class Session(
        val studentID   : StudentID,
        var isValidated : Boolean = false,
    )
}