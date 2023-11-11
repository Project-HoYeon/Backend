package dev.hoyeon.routes

import dev.hoyeon.HOST_API_URL
import dev.hoyeon.auth.EmailSender
import dev.hoyeon.auth.EmailValidateSession
import dev.hoyeon.cypher.JwtTokenGenerator
import dev.hoyeon.cypher.SHA256
import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.objects.StudentID
import dev.hoyeon.objects.User
import dev.hoyeon.utils.getKoinInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.BufferedReader
import java.io.InputStream
import java.net.URLEncoder
import java.util.*

fun Route.handleAuth() {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            val studentID = request.id.toIntOrNull()
            if (studentID == null) {
                call.respond(HttpStatusCode.Unauthorized, hashMapOf(
                    "message" to "ID_PW_MISMATCH"
                ))
                return@post
            }

            val userRepo = getKoinInstance<UserRepository>()
            val isValid = userRepo.checkAuth(studentID, request.password)
            if (!isValid) {
                call.respond(HttpStatusCode.Unauthorized, hashMapOf(
                    "message" to "ID_PW_MISMATCH"
                ))
                return@post
            }
            // TODO: JWT Token generation
            val user = userRepo.read(studentID)!!
            val token = JwtTokenGenerator.generateToken(user)
            call.respond(HttpStatusCode.OK, hashMapOf(
                "token" to token
            ))
        }

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val studentID = request.id.toIntOrNull()
            if (studentID == null) {
                call.respond(HttpStatusCode.BadRequest, hashMapOf(
                    "message" to "INVALID_ID_FORMAT"
                ))
                return@post
            }

            val userRepo = getKoinInstance<UserRepository>()
            val idExists = userRepo.studentIDExists(studentID)
            if (idExists) {
                call.respond(HttpStatusCode.Conflict, hashMapOf(
                    "message" to "STD_ID_CONFLICT"
                ))
                return@post
            }

            if (!EmailValidateSession.isIDValidated(studentID)) {
                if (EmailValidateSession.hasSessionOfID(studentID))
                    call.respond(HttpStatusCode.Unauthorized, hashMapOf(
                        "message" to "EMAIL_AUTH_NOT_COMPLETED"
                    ))
                else {
                    sendValidateMail(studentID)
                    call.respond(HttpStatusCode.Accepted, hashMapOf(
                        "message" to "AUTH_MAIL_SENT"
                    ))
                }
                return@post
            }
            EmailValidateSession.removeSession(studentID)

            val uid = UUID.randomUUID()
            val pwHash = SHA256.encrypt(request.password)
            User(
                id = uid,
                studentID = studentID,
                name = request.name,
                pwHash = pwHash,
            ).also {
                userRepo.create(it)
            }
            call.respond(HttpStatusCode.OK, uid.toString())
        }

        get("/validate") {
            val id = call.request.queryParameters["id"]?.let(UUID::fromString)
            if (id == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            // TODO: EmailValidate
            if (!EmailValidateSession.isSessionValid(id)) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            EmailValidateSession.validateSession(id)
            call.respond(HttpStatusCode.OK, ":)")
        }
    }
}

private fun sendValidateMail(studentID: StudentID) {
    val mailContent = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("static/auth_mail_content.html")!!
        .let(InputStream::reader)
        .let(::BufferedReader)
        .readText()

    val uuid = EmailValidateSession.createSession(studentID).toString()
    val url  = "$HOST_API_URL/auth/validate?id=${URLEncoder.encode(uuid, Charsets.UTF_8)}"
    val mailData = EmailSender.EmailData(
        title = "<호연> 이메일 인증을 완료해 주세요.",
        content = mailContent.replace("%TARGET_ADDRESS%", url),
        type = "html"
    )

    val receiverAddr = "$studentID@vision.hoseo.edu"
    EmailSender.sendMail(receiverAddr, mailData)
}

@Serializable
private class LoginRequest(
    val id: String,
    val password: String
)

@Serializable
private class RegisterRequest(
    val id: String,
    val name: String,
    val password: String,
)