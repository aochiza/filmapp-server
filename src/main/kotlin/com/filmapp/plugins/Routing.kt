package com.filmapp.plugins

import com.filmapp.repositories.FilmRepository
import com.filmapp.repositories.GenreRepository
import com.filmapp.repositories.UserRepository
import com.filmapp.routes.authRoutes
import com.filmapp.routes.filmRoutes
import com.filmapp.routes.genreRoutes
import com.filmapp.routes.watchLaterRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll

fun Application.configureRouting() {
    println("=== ROUTING IS CALLED ===")

    // Создаём репозитории (они используют dataSource из Database.kt)
    val userRepository = UserRepository(dataSource)
    val filmRepository = FilmRepository(dataSource)
    val genreRepository = GenreRepository(dataSource)

    routing {
        // Базовые эндпоинты для проверки
        get("/health") {
            call.respondText("OK")
        }

        get("/ping") {
            call.respondText("pong")
        }

        get("/test-db") {
            try {
                val result = org.jetbrains.exposed.sql.transactions.transaction {
                    com.filmapp.models.Films.selectAll().count()
                }
                call.respondText("Success! Films in DB: $result")
            } catch (e: Exception) {
                call.respondText("DB Error: ${e.message}")
            }
        }

        // API v1 роуты
        route("/api/v1") {
            authRoutes(userRepository)
            filmRoutes(filmRepository)
            genreRoutes(genreRepository)
            watchLaterRoutes(filmRepository)
        }
    }
}