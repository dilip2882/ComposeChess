package com.dilip

import com.dilip.models.ChessGame
import com.dilip.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val game = ChessGame()
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureMonitoring()
    configureRouting(game)
}
