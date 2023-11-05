package dev.hoyeon.auth

import io.github.cdimascio.dotenv.Dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.properties.Delegates.notNull

private val logger = KotlinLogging.logger {  }

object EmailSender: KoinComponent {

    private val env: Dotenv by inject()
    private val session: Session

    fun sendMail(dest: String, builder: EmailData.Builder.() -> Unit) {
        val data = EmailData.Builder().apply(builder).build()
        sendMail(dest, data)
    }

    fun sendMail(dest: String, data: EmailData): Boolean =
        sendMail(dest, data.title, data.content, data.type)

    fun sendMail(dest: String, title: String, content: String, subtype: String): Boolean {
        val message = MimeMessage(session)
        try {
            message.setFrom("hoyeon@mooner.dev")
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest))
            message.subject = title
            message.setText(content, "utf-8", subtype)
            Transport.send(message)
            logger.debug { "Mail sent to \${dest}" }
        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    init {
        val props = Properties()
        props.setProperty("mail.smtp.auth", "true")
        props.setProperty("mail.smtp.starttls.enable", "true")
        props.setProperty("mail.smtp.host", "smtp.gmail.com")
        props.setProperty("mail.smtp.port", "587")
        session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(env["EMAIL_ADDR"], env["EMAIL_PSWD"])
            }
        })
        //session.setDebug(true);
    }

    data class EmailData(
        val title   : String,
        val content : String,
        val type    : String,
    ) {

        class Builder {

            var title   : String by notNull()
            var content : String by notNull()
            var type    : String by notNull()

            fun build(): EmailData =
                EmailData(title, content, type)
        }
    }
}