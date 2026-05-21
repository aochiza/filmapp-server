package com.filmapp.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Genres : IntIdTable("genres") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description").nullable()
}

class Genre(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Genre>(Genres)
    var name by Genres.name
    var description by Genres.description
}

object Films : IntIdTable("films") {
    val title = varchar("title", 255)
    val originalTitle = varchar("original_title", 255).nullable()
    val description = text("description").nullable()
    val releaseYear = integer("release_year")
    val rating = decimal("rating", 3, 1).nullable()
    val posterUrl = varchar("poster_url", 500).nullable()
    val genreId = reference("genre_id", Genres).nullable()
    val director = varchar("director", 255).nullable()
    val duration = integer("duration").nullable()  // в минутах
    val createdAt = datetime("created_at")
}

class Film(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Film>(Films)
    var title by Films.title
    var originalTitle by Films.originalTitle
    var description by Films.description
    var releaseYear by Films.releaseYear
    var rating by Films.rating
    var posterUrl by Films.posterUrl
    var genre by Genre optionalReferencedOn Films.genreId
    var director by Films.director
    var duration by Films.duration
    var createdAt by Films.createdAt
}

object FavoriteFilms : IntIdTable("favorite_films") {
    val userId = reference("user_id", Users)
    val filmId = reference("film_id", Films)
}

object WatchLaterFilms : IntIdTable("watch_later_films") {
    val userId = reference("user_id", Users)
    val filmId = reference("film_id", Films)
    val addedAt = datetime("added_at")
}