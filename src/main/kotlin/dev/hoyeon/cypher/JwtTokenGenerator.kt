package dev.hoyeon.cypher

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.hoyeon.objects.User
import io.github.cdimascio.dotenv.Dotenv
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

object JwtTokenGenerator: KoinComponent {

    private const val EXPIRES_AFTER = 1000 * 60 * 60 * 3

    private val dotenv: Dotenv by inject()

    fun generateToken(user: User): String {
        val audience = dotenv["JWT_AUDIENCE"]
        val domain   = dotenv["JWT_DOMAIN"]
        val secret   = dotenv["JWT_SECRET"]

        return JWT.create()
            .withAudience(audience)
            .withIssuer(domain)
            .withClaim("studentID", user.studentID)
            .withClaim("userID", user.id.toString())
            .withClaim("username", user.name)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRES_AFTER))
            .sign(Algorithm.HMAC256(secret))
    }
}