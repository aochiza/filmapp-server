package com.filmapp.routes

import com.filmapp.dto.ErrorResponse
import com.filmapp.dto.FilmRequest
import com.filmapp.dto.FilmsListResponse
import com.filmapp.repositories.FilmRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun ApplicationCall.getUserId(): Int {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("userId")?.asInt()
        ?: throw IllegalArgumentException("User not authenticated")
}

fun Route.filmRoutes(filmRepository: FilmRepository) {

    route("/films") {

        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["search"]
            val genreId = call.request.queryParameters["genreId"]?.toIntOrNull()

            val (films, total) = filmRepository.getAll(page, pageSize, search, genreId)
            call.respond(FilmsListResponse(films, total.toInt(), page, pageSize))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val film = filmRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Film not found"))

            call.respond(film)
        }

        authenticate("auth-jwt") {

            post {
                val request = call.receive<FilmRequest>()
                val film = filmRepository.create(request)
                call.respond(HttpStatusCode.Created, film)
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

                val request = call.receive<FilmRequest>()
                val film = filmRepository.update(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Film not found"))

                call.respond(film)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

                if (filmRepository.delete(id)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Film not found"))
                }
            }

            get("/favorites") {
                val userId = call.getUserId()
                call.respond(filmRepository.getFavorites(userId))
            }

            post("/{id}/favorite") {
                val userId = call.getUserId()
                val filmId = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

                filmRepository.addToFavorites(userId, filmId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}/favorite") {
                val userId = call.getUserId()
                val filmId = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

                filmRepository.removeFromFavorites(userId, filmId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}