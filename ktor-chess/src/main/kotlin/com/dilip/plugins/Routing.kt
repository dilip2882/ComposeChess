package com.dilip.plugins

import com.dilip.models.ChessGame
import com.dilip.socket
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(game: ChessGame) {
    routing {
        socket(game)
    }
}
