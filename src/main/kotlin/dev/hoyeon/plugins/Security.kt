package dev.hoyeon.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.hoyeon.utils.getKoinInstance
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

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
                if (
                    payload.audience.contains(jwtAudience) &&
                    payload.getClaim("username").asString().isNotBlank() &&
                    payload.getClaim("userID").asString().isNotEmpty() &&
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

    data class MySession(val count: Int = 0)
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    routing {
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}
