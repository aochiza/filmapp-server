package com.filmapp.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.*

suspend fun ApplicationCall.requireAdmin(): Boolean {
    val principal = principal<JWTPrincipal>()

    val role = principal
        ?.payload
        ?.getClaim("role")
        ?.asString()

    if (role != "ADMIN") {
        respond(
            HttpStatusCode.Forbidden,
            mapOf("message" to "Access denied")
        )
        return false
    }

    return true
}