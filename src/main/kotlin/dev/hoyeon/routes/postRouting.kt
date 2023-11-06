package dev.hoyeon.routes

import dev.hoyeon.db.services.PostRepository
import dev.hoyeon.objects.DefaultPost
import dev.hoyeon.utils.currentTimeMillis
import dev.hoyeon.utils.getKoinInstance
import dev.hoyeon.utils.jwtPayload
import dev.hoyeon.utils.toUUID
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.handlePost() = authenticate("jwt") {
    route("/posts") {

        get {
            val params = call.request.queryParameters

            val from = params["from"]?.toLongOrNull()
            if (from == null || from < 0) {
                call.respond(HttpStatusCode.BadRequest, "Illegal format on parameter 'from': $from");
                return@get
            }

            val limit = params["limit"]?.toIntOrNull()
            if (limit == null || limit < 1) {
                call.respond(HttpStatusCode.BadRequest, "Illegal format on parameter 'limit': $limit");
                return@get
            }

            val repo = getPostRepository()
            val posts = repo.readAll(from, limit)
                .map { post ->
                    if (post.isAnonymous)
                        (post as DefaultPost).toAnonymousPost()
                    else
                        post
                }
            call.respond(posts)
        }

        get("/{id}") {
            val postID = call.parameters["id"]?.toIntOrNull()
            if (postID == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val repo = getPostRepository()
            val post = repo.read(postID)?.let {
                if (it.isAnonymous)
                    (it as DefaultPost).toAnonymousPost()
                else
                    it
            }
            if (post == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(post)
        }

        post("/write") {
            val params = call.receive<PostWriteRequest>()
            val payload = call.jwtPayload()
            val userID = payload.getClaim("userID")
                .asString()
                .toUUID()
            val username = payload.getClaim("username")
                .asString()

            val repo = getPostRepository()
            val postID = repo.create(
                authorID = userID,
                authorName = username,
                title = params.title,
                content = params.content,
                isAnonymous = params.isAnonymous,
                writtenAt = currentTimeMillis()
            )

            call.respond(postID)
        }
    }
}

@Serializable
private data class PostWriteRequest(
    val title       : String,
    val content     : String,
    val isAnonymous : Boolean,
)

private var postRepository: PostRepository? = null
private fun getPostRepository(): PostRepository =
    postRepository
        ?: getKoinInstance<PostRepository>()
            .also { postRepository = it }