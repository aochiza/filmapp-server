package com.filmapp.repositories

import com.filmapp.dto.GenreRequest
import com.filmapp.dto.GenreResponse
import com.zaxxer.hikari.HikariDataSource

class GenreRepository(private val dataSource: HikariDataSource) {

    private fun mapGenre(rs: java.sql.ResultSet) = GenreResponse(
        id = rs.getInt("id"),
        name = rs.getString("name"),
        description = rs.getString("description")
    )

    fun getAll(): List<GenreResponse> {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM genres ORDER BY name").use { stmt ->
                val rs = stmt.executeQuery()
                val list = mutableListOf<GenreResponse>()
                while (rs.next()) list.add(mapGenre(rs))
                return list
            }
        }
    }

    fun getById(id: Int): GenreResponse? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM genres WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) return mapGenre(rs)
            }
        }
        return null
    }

    fun create(request: GenreRequest): GenreResponse {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO genres (name, description) VALUES (?, ?) RETURNING id"
            ).use { stmt ->
                stmt.setString(1, request.name)
                stmt.setString(2, request.description)
                val rs = stmt.executeQuery()
                rs.next()
                return GenreResponse(rs.getInt("id"), request.name, request.description)
            }
        }
    }

    fun update(id: Int, request: GenreRequest): GenreResponse? {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "UPDATE genres SET name = ?, description = ? WHERE id = ?"
            ).use { stmt ->
                stmt.setString(1, request.name)
                stmt.setString(2, request.description)
                stmt.setInt(3, id)
                if (stmt.executeUpdate() == 0) return null
                return GenreResponse(id, request.name, request.description)
            }
        }
    }

    fun delete(id: Int): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM genres WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                return stmt.executeUpdate() > 0
            }
        }
    }
}