package com.filmapp.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var dataSource: HikariDataSource

fun Application.configureDatabase() {

    val dbUrl = "jdbc:postgresql://localhost:5432/filmdb?sslmode=disable"
    val dbUser = "filmuser"
    val dbPass = "filmpassword"

    println("=== CONNECTING TO DATABASE ===")
    println("URL: $dbUrl")
    println("User: $dbUser")

    val config = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPass

        driverClassName = "org.postgresql.Driver"

        maximumPoolSize = 5
        minimumIdle = 1
        isAutoCommit = true
        connectionTimeout = 30000
    }

    dataSource = HikariDataSource(config)//созд пул соед

    Database.connect(dataSource)

    println("Database connected successfully!")
}