package com.dilip

import com.dilip.models.ChessGame
import com.dilip.models.MovePiece
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Route.socket(game: ChessGame) {
    route("/play") {
        webSocket {
            val player = game.connectPlayer(this)

            if (player == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "2 players already connected"))
                return@webSocket
            }

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val action = extractAction(frame.readText())
                        game.finishTurn(player, action.fromX, action.fromY, action.toX, action.toY)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                game.disconnectPlayer(player)
            }
        }
    }
}

private fun extractAction(message: String): MovePiece {
    // move_piece#{...}
    val type = message.substringBefore("#")
    val body = message.substringAfter("#")
    return if (type == "move_piece") {
        Json.decodeFromString(body)
    } else MovePiece(-1, -1, -1, -1, ' ') // Default values, piece character can be modified
}

@Serializable
data class MovePiece(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

// move_piece#{"fromX":0,"fromY":1,"toX":0,"toY":2,"piece":"P","isCapture":false}
