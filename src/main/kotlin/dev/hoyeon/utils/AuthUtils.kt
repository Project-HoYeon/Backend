package dev.hoyeon.utils

import com.auth0.jwt.interfaces.Payload
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.jwtPayload(): Payload =
    principal<JWTPrincipal>()!!.payload