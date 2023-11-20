package dev.hoyeon.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.hoyeon.cypher.SHA256
import dev.hoyeon.utils.getKoinInstance
import dev.hoyeon.utils.isDevelopment
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*


fun Application.configureSecurity() {
    val dotenv: Dotenv = getKoinInstance()

    val jwtAudience = dotenv["JWT_AUDIENCE"]
    val jwtDomain   = dotenv["JWT_DOMAIN"]
    val jwtRealm    = dotenv["JWT_REALM"]
    val jwtSecret   = dotenv["JWT_SECRET"]
    authentication {
        jwt("jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                val payload = credential.payload

                val fingerprint = request.cookies[dotenv["FGP_TOKEN_NAME", "_Token-Fgp"]]
                    ?.let(SHA256::encrypt)

                if (
                    payload.audience.contains(jwtAudience) &&
                    payload.getClaim("username").asString().isNotBlank() &&
                    payload.getClaim("userID").asString().isNotBlank() &&
                    payload.getClaim("studentID") != null &&
                    (isDevelopment || payload.getClaim("fgp").asString().equals(fingerprint))
                )
                    JWTPrincipal(credential.payload)
                else
                    null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token invalid or expired")
            }
        }
        jwt("jwt-socket") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            authHeader { call ->
                val token = call.request.queryParameters["token"]
                    ?: return@authHeader null

                try {
                    parseAuthorizationHeader("Bearer $token")
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            validate { credential ->
                val payload = credential.payload

                if (
                    payload.audience.contains(jwtAudience) &&
                    payload.getClaim("username").asString().isNotBlank() &&
                    payload.getClaim("userID").asString().isNotBlank() &&
                    payload.getClaim("studentID") != null
                )
                    JWTPrincipal(credential.payload)
                else
                    null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token invalid or expired")
            }
        }
    }
}
