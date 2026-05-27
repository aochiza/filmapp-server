package com.filmapp.dto

import kotlinx.serialization.Serializable

@Serializable
data class FilmResponse(
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val description: String?,
    val releaseYear: Int,
    val rating: Double?,
    val posterUrl: String?,
    val genreId: Int?,
    val genreName: String?,
    val director: String?,
    val duration: Int?,
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false
)

@Serializable
data class FilmRequest(
    val title: String,
    val originalTitle: String? = null,
    val description: String? = null,
    val releaseYear: Int,
    val rating: Double? = null,
    val posterUrl: String? = null,
    val genreId: Int? = null,
    val director: String? = null,
    val duration: Int? = null
)

@Serializable
data class GenreResponse(
    val id: Int,
    val name: String,
    val description: String?
)

@Serializable
data class GenreRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class FilmsListResponse(
    val films: List<FilmResponse>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class WatchLaterResponse(
    val films: List<FilmResponse>
)