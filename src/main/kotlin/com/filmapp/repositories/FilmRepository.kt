package com.filmapp.repositories

import com.filmapp.dto.FilmRequest
import com.filmapp.dto.FilmResponse
import com.filmapp.dto.FilmsListResponse
import com.zaxxer.hikari.HikariDataSource
import java.time.LocalDateTime

class FilmRepository(private val dataSource: HikariDataSource) {

    private fun mapFilm(rs: java.sql.ResultSet, userId: Int? = null): FilmResponse {
        val filmId = rs.getInt("id")
        val isFavorite = userId?.let { checkFavorite(filmId, it) } ?: false
        val isWatchLater = userId?.let { checkWatchLater(filmId, it) } ?: false
        return FilmResponse(
            id = filmId,
            title = rs.getString("title"),
            originalTitle = rs.getString("original_title"),
            description = rs.getString("description"),
            releaseYear = rs.getInt("release_year"),
            rating = rs.getDouble("rating").takeIf { !rs.wasNull() },
            posterUrl = rs.getString("poster_url"),
            genreId = rs.getInt("genre_id").takeIf { !rs.wasNull() },
            genreName = rs.getString("genre_name"),
            director = rs.getString("director"),
            duration = rs.getInt("duration").takeIf { !rs.wasNull() },
            isFavorite = isFavorite,
            isWatchLater = isWatchLater
        )
    }

    private fun checkFavorite(filmId: Int, userId: Int): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT COUNT(*) FROM favorite_films WHERE film_id = ? AND user_id = ?"
            ).use { stmt ->
                stmt.setInt(1, filmId)
                stmt.setInt(2, userId)
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getInt(1) > 0
            }
        }
    }

    private fun checkWatchLater(filmId: Int, userId: Int): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT COUNT(*) FROM watch_later_films WHERE film_id = ? AND user_id = ?"
            ).use { stmt ->
                stmt.setInt(1, filmId)
                stmt.setInt(2, userId)
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getInt(1) > 0
            }
        }
    }

    fun getAll(
        page: Int = 1,
        pageSize: Int = 20,
        search: String? = null,
        genreId: Int? = null,
        userId: Int? = null
    ): Pair<List<FilmResponse>, Long> {
        dataSource.connection.use { conn ->
            var sql = """
                SELECT f.*, g.name as genre_name 
                FROM films f 
                LEFT JOIN genres g ON f.genre_id = g.id
                WHERE 1=1
            """
            val params = mutableListOf<Any>()

            search?.let {
                sql += " AND (LOWER(f.title) LIKE ? OR LOWER(f.director) LIKE ?)"
                params.add("%${it.lowercase()}%")
                params.add("%${it.lowercase()}%")
            }

            genreId?.let {
                sql += " AND f.genre_id = ?"
                params.add(it)
            }

            val countSql = "SELECT COUNT(*) FROM ($sql) as count_query"
            val total = conn.prepareStatement(countSql).use { stmt ->
                params.forEachIndexed { i, p ->
                    when (p) {
                        is String -> stmt.setString(i + 1, p)
                        is Int -> stmt.setInt(i + 1, p)
                    }
                }
                val rs = stmt.executeQuery()
                rs.next()
                rs.getLong(1)
            }

            sql += " ORDER BY f.rating DESC NULLS LAST LIMIT ? OFFSET ?"
            params.add(pageSize)
            params.add((page - 1) * pageSize)

            val films = conn.prepareStatement(sql).use { stmt ->
                params.forEachIndexed { i, p ->
                    when (p) {
                        is String -> stmt.setString(i + 1, p)
                        is Int -> stmt.setInt(i + 1, p)
                    }
                }
                val rs = stmt.executeQuery()
                val list = mutableListOf<FilmResponse>()
                while (rs.next()) list.add(mapFilm(rs, userId))
                list
            }

            return Pair(films, total)
        }
    }

    fun getById(id: Int, userId: Int? = null): FilmResponse? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("""
                SELECT f.*, g.name as genre_name 
                FROM films f 
                LEFT JOIN genres g ON f.genre_id = g.id
                WHERE f.id = ?
            """).use { stmt ->
                stmt.setInt(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) return mapFilm(rs, userId)
            }
        }
        return null
    }

    fun create(request: FilmRequest): FilmResponse {
        dataSource.connection.use { conn ->
            conn.prepareStatement("""
                INSERT INTO films (title, original_title, description, release_year, rating, poster_url, genre_id, director, duration, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
            """).use { stmt ->
                stmt.setString(1, request.title)
                stmt.setString(2, request.originalTitle)
                stmt.setString(3, request.description)
                stmt.setInt(4, request.releaseYear)
                if (request.rating != null) stmt.setDouble(5, request.rating) else stmt.setNull(5, java.sql.Types.DECIMAL)
                stmt.setString(6, request.posterUrl)
                if (request.genreId != null) stmt.setInt(7, request.genreId) else stmt.setNull(7, java.sql.Types.INTEGER)
                stmt.setString(8, request.director)
                if (request.duration != null) stmt.setInt(9, request.duration) else stmt.setNull(9, java.sql.Types.INTEGER)
                stmt.setObject(10, LocalDateTime.now())
                val rs = stmt.executeQuery()
                rs.next()
                return getById(rs.getInt("id"))!!
            }
        }
    }

    fun update(id: Int, request: FilmRequest): FilmResponse? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("""
                UPDATE films SET title=?, original_title=?, description=?, release_year=?, 
                rating=?, poster_url=?, genre_id=?, director=?, duration=? WHERE id=?
            """).use { stmt ->
                stmt.setString(1, request.title)
                stmt.setString(2, request.originalTitle)
                stmt.setString(3, request.description)
                stmt.setInt(4, request.releaseYear)
                if (request.rating != null) stmt.setDouble(5, request.rating) else stmt.setNull(5, java.sql.Types.DECIMAL)
                stmt.setString(6, request.posterUrl)
                if (request.genreId != null) stmt.setInt(7, request.genreId) else stmt.setNull(7, java.sql.Types.INTEGER)
                stmt.setString(8, request.director)
                if (request.duration != null) stmt.setInt(9, request.duration) else stmt.setNull(9, java.sql.Types.INTEGER)
                stmt.setInt(10, id)
                stmt.executeUpdate()
            }
        }
        return getById(id)
    }

    fun delete(id: Int): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM films WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun addToFavorites(userId: Int, filmId: Int): Boolean {
        if (checkFavorite(filmId, userId)) return false
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO favorite_films (user_id, film_id) VALUES (?, ?)"
            ).use { stmt ->
                stmt.setInt(1, userId)
                stmt.setInt(2, filmId)
                stmt.executeUpdate()
                return true
            }
        }
    }

    fun removeFromFavorites(userId: Int, filmId: Int): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "DELETE FROM favorite_films WHERE user_id = ? AND film_id = ?"
            ).use { stmt ->
                stmt.setInt(1, userId)
                stmt.setInt(2, filmId)
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun getFavorites(userId: Int): List<FilmResponse> {
        dataSource.connection.use { conn ->
            conn.prepareStatement("""
                SELECT f.*, g.name as genre_name 
                FROM films f 
                LEFT JOIN genres g ON f.genre_id = g.id
                JOIN favorite_films ff ON f.id = ff.film_id
                WHERE ff.user_id = ?
            """).use { stmt ->
                stmt.setInt(1, userId)
                val rs = stmt.executeQuery()
                val list = mutableListOf<FilmResponse>()
                while (rs.next()) list.add(mapFilm(rs, userId))
                return list
            }
        }
    }

    fun addToWatchLater(userId: Int, filmId: Int): Boolean {
        if (checkWatchLater(filmId, userId)) return false
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO watch_later_films (user_id, film_id, added_at) VALUES (?, ?, ?)"
            ).use { stmt ->
                stmt.setInt(1, userId)
                stmt.setInt(2, filmId)
                stmt.setObject(3, LocalDateTime.now())
                stmt.executeUpdate()
                return true
            }
        }
    }

    fun removeFromWatchLater(userId: Int, filmId: Int): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "DELETE FROM watch_later_films WHERE user_id = ? AND film_id = ?"
            ).use { stmt ->
                stmt.setInt(1, userId)
                stmt.setInt(2, filmId)
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun getWatchLater(userId: Int): List<FilmResponse> {
        dataSource.connection.use { conn ->
            conn.prepareStatement("""
                SELECT f.*, g.name as genre_name 
                FROM films f 
                LEFT JOIN genres g ON f.genre_id = g.id
                JOIN watch_later_films wl ON f.id = wl.film_id
                WHERE wl.user_id = ?
                ORDER BY wl.added_at DESC
            """).use { stmt ->
                stmt.setInt(1, userId)
                val rs = stmt.executeQuery()
                val list = mutableListOf<FilmResponse>()
                while (rs.next()) list.add(mapFilm(rs, userId))
                return list
            }
        }
    }
}