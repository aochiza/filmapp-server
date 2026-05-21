package com.filmapp.routes

import com.filmapp.dto.ErrorResponse
import com.filmapp.repositories.FilmRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.watchLaterRoutes(filmRepository: FilmRepository) {
    authenticate("auth-jwt") {
        route("/watch-later") {

            get {
                val userId = call.getUserId()
                val films = filmRepository.getWatchLater(userId)
                call.respond(films)
            }

            post("/{filmId}") {
                val userId = call.getUserId()
                val filmId = call.parameters["filmId"]?.toIntOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid ID")
                    )

                filmRepository.addToWatchLater(userId, filmId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{filmId}") {
                val userId = call.getUserId()
                val filmId = call.parameters["filmId"]?.toIntOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid ID")
                    )

                filmRepository.removeFromWatchLater(userId, filmId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}