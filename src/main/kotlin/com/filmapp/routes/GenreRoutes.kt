package com.filmapp.routes

import com.filmapp.dto.ErrorResponse
import com.filmapp.dto.GenreRequest
import com.filmapp.repositories.GenreRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.genreRoutes(genreRepository: GenreRepository) {
    route("/genres") {

        get {
            call.respond(genreRepository.getAll())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val genre = genreRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Genre not found"))

            call.respond(genre)
        }

        authenticate("auth-jwt") {
            post {
                val request = call.receive<GenreRequest>()
                val genre = genreRepository.create(request)
                call.respond(HttpStatusCode.Created, genre)
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

                val request = call.receive<GenreRequest>()
                val genre = genreRepository.update(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Genre not found"))

                call.respond(genre)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

                if (genreRepository.delete(id)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Genre not found"))
                }
            }
        }
    }
}