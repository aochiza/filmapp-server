package com.filmapp.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.filmapp.dto.*
import com.filmapp.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.authRoutes(userRepository: UserRepository) {
    val secret = application.environment.config.property("jwt.secret").getString()
    val issuer = application.environment.config.property("jwt.issuer").getString()
    val audience = application.environment.config.property("jwt.audience").getString()
    val expirationMs = application.environment.config
        .property("jwt.expiration").getString().toLong()

    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.email.isBlank() || request.password.isBlank() || request.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("All fields are required"))
                return@post
            }

            if (userRepository.existsByEmail(request.email)) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Email already registered"))
                return@post
            }

            val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())

            val user = userRepository.create(request.email, passwordHash, request.name)

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("userId", user.id)
                .withClaim("email", user.email)
                .withClaim("role", user.role)
                .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC256(secret))

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    token = token,
                    userId = user.id,
                    name = user.name,
                    email = user.email,
                    role = user.role
                )
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = userRepository.findByEmail(request.email)
                ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
                    return@post
                }

            if (!BCrypt.checkpw(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
                return@post
            }

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("userId", user.id)
                .withClaim("email", user.email)
                .withClaim("role", user.role)
                .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC256(secret))

            call.respond(
                AuthResponse(
                    token = token,
                    userId = user.id,
                    name = user.name,
                    email = user.email,
                    role = user.role
                )
            )
        }
    }
}