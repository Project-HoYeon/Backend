package dev.hoyeon

import dev.hoyeon.auth.EmailSender
import dev.hoyeon.auth.EmailValidateSession
import dev.hoyeon.db.services.PostRepository
import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.plugins.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.BufferedReader
import java.io.InputStream
import java.net.URLEncoder
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testMailSend() {
        startKoin()
        val studentID = 20221000
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

    private fun startKoin() {
        val dotenv = dotenv()

        /* DI Setup */
        startKoin {
            val module = module(createdAtStart = true) {
                single { dotenv }
                single { createDatabase(dotenv) }
            }
            modules(module)
        }
    }
}
