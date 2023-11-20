package dev.hoyeon.plugins

import dev.hoyeon.routes.handleAuth
import dev.hoyeon.routes.handlePost
import dev.hoyeon.utils.getKoinInstance
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    val dotenv = getKoinInstance<Dotenv>()

    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        //route("/hoyeon/api/v1") {
        //}

        route(dotenv["BASE_ROUTE_PATH", "/"]) {
            handleAuth()
            handlePost()
            get("/") {
                call.respondText("Hello World!")
            }
            get<Articles> { article ->
                // Get all articles ...
                call.respond("List of articles sorted starting from ${article.sort}")
            }
        }
    }
}

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
