package com.filmapp

import com.filmapp.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    println("!!! APPLICATION MODULE IS RUNNING !!!")
    configureDatabase()
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureSecurity()
    configureRouting()
}