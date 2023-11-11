package dev.hoyeon.cypher

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.hoyeon.objects.User
import io.github.cdimascio.dotenv.Dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.security.SecureRandom
import java.util.*

private val logger = KotlinLogging.logger {  }

object JwtTokenGenerator: KoinComponent {

    private const val EXPIRES_AFTER = 1000 * 60 * 60 * 3

    private val dotenv: Dotenv by inject()
    private val secureRandom = SecureRandom()

    fun generateToken(user: User): TokenInfo {
        val audience = dotenv["JWT_AUDIENCE"]
        val domain   = dotenv["JWT_DOMAIN"]
        val secret   = dotenv["JWT_SECRET"]

        val fingerprint = ByteArray(50)
            .apply(secureRandom::nextBytes)
            .let(HexFormat.of()::formatHex)
        val fgpHash = SHA256.encrypt(fingerprint)

        logger.debug { "Generating token with fingerprint: $fingerprint" }

        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(domain)
            .withClaim("studentID", user.studentID)
            .withClaim("userID", user.id.toString())
            .withClaim("username", user.name)
            .withClaim("fgp", fgpHash)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRES_AFTER))
            .sign(Algorithm.HMAC256(secret))

        return TokenInfo(token, fingerprint)
    }

    data class TokenInfo(
        val token       : String,
        val fingerprint : String,
    )
}