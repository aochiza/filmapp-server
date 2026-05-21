package com.filmapp.repositories

import com.zaxxer.hikari.HikariDataSource
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

data class UserData(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val username: String
)

class UserRepository(private val dataSource: HikariDataSource) {

    fun findByEmail(email: String): UserData? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT id, email, password_hash, username FROM users WHERE email = ?").use { stmt ->
                stmt.setString(1, email)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    return UserData(
                        id = rs.getInt("id"),
                        email = rs.getString("email"),
                        passwordHash = rs.getString("password_hash"),
                        username = rs.getString("username")
                    )
                }
            }
        }
        return null
    }

    fun findById(id: Int): UserData? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT id, email, password_hash, username FROM users WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    return UserData(
                        id = rs.getInt("id"),
                        email = rs.getString("email"),
                        passwordHash = rs.getString("password_hash"),
                        username = rs.getString("username")
                    )
                }
            }
        }
        return null
    }

    fun create(email: String, passwordHash: String, username: String): UserData {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO users (email, password_hash, username, created_at) VALUES (?, ?, ?, ?) RETURNING id"
            ).use { stmt ->
                stmt.setString(1, email)
                stmt.setString(2, passwordHash)
                stmt.setString(3, username)
                stmt.setObject(4, LocalDateTime.now())
                val rs = stmt.executeQuery()
                rs.next()
                return UserData(
                    id = rs.getInt("id"),
                    email = email,
                    passwordHash = passwordHash,
                    username = username
                )
            }
        }
    }

    fun existsByEmail(email: String): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?").use { stmt ->
                stmt.setString(1, email)
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getInt(1) > 0
            }
        }
    }
}