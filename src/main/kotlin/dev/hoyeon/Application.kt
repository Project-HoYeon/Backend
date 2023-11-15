package dev.hoyeon

import dev.hoyeon.db.services.PostRepository
import dev.hoyeon.db.services.UserRepository
import dev.hoyeon.db.services.internal.PostRepositoryImpl
import dev.hoyeon.db.services.internal.UserRepositoryImpl
import dev.hoyeon.plugins.*
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun main(args: Array<String>) {
    val dotenv = dotenv()

    /* DI Setup */
    startKoin {
        val module = module(createdAtStart = true) {
            single { dotenv }
            single { createDatabase(dotenv) }
            singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
            singleOf(::PostRepositoryImpl) { bind<PostRepository>() }
        }
        modules(module)
    }

    io.ktor.server.cio.EngineMain.main(args)
}

fun createDatabase(env: Dotenv): Database =
    Database.connect(
        url      = "${env["DB_HOST"]}:${env["DB_PORT"]}/hoyeon",
        user     = env["DB_USERNAME"],
        driver   = env["DB_DRIVER", "org.mariadb.jdbc.Driver"],
        password = env["DB_PASSWORD"],
    )

fun Application.module() {
    configureSecurity()
    //configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureRouting()
    configureCORS()
}
