package dev.hoyeon.plugins

import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.objects.User
import dev.hoyeon.utils.getKoinInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureDatabases() {

    val userRepo = getKoinInstance<UserRepository>()

    routing {
        // Create user
        post("/users") {
            val user = call.receive<User>()
            val id = userRepo.create(user)
            call.respond(HttpStatusCode.Created, id.toString())
        }
        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]
                ?.let(UUID::fromString)
                ?: throw IllegalArgumentException("Invalid ID")

            val user = userRepo.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]
                ?.let(UUID::fromString)
                ?: throw IllegalArgumentException("Invalid ID")

            val user = call.receive<User>()
            userRepo.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]
                ?.let(UUID::fromString)
                ?: throw IllegalArgumentException("Invalid ID")

            userRepo.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
