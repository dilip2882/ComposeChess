package com.dilip.models

import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class ChessGame {

    private val state = MutableStateFlow(GameState())
    private val playerSockets = ConcurrentHashMap<Char, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    fun connectPlayer(session: WebSocketSession): Char? {
        val isPlayerWhite = state.value.connectedPlayers.any { it == 'W' }
        val player = if (isPlayerWhite) 'B' else 'W'

        state.update {
            if (state.value.connectedPlayers.contains(player)) {
                return null
            }
            if (!playerSockets.containsKey(player)) {
                playerSockets[player] = session
            }
            it.copy(
                connectedPlayers = it.connectedPlayers + player
            )
        }
        return player
    }

    fun disconnectPlayer(player: Char) {
        playerSockets.remove(player)
        state.update {
            it.copy(
                connectedPlayers = it.connectedPlayers - player
            )
        }
    }

    suspend fun broadcast(state: GameState) {
        val serializedState = Json.encodeToString(state)
        playerSockets.values.forEach { socket ->
            socket.send(serializedState)
        }
    }

    fun finishTurn(player: Char, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        if (state.value.playerAtTurn != player || !isValidMove(fromX, fromY, toX, toY)) {
            return
        }

        state.update {
            val newBoard = it.board.map { row -> row.clone() }.toTypedArray()
            newBoard[toY][toX] = newBoard[fromY][fromX]
            newBoard[fromY][fromX] = null // Empty the previous position

            val newGameStatus = evaluateGameStatus(newBoard, player)

            it.copy(
                playerAtTurn = if (player == 'W') 'B' else 'W',
                board = newBoard,
                gameStatus = newGameStatus
            )
        }
    }

    fun resetGame() {
        state.update {
            GameState(
                playerAtTurn = 'W',
                board = GameState.emptyBoard(),
                connectedPlayers = it.connectedPlayers,
                gameStatus = GameStatus.IDLE
            )
        }
    }

    fun isPlayerConnected(player: Char): Boolean {
        return playerSockets.containsKey(player)
    }

    fun getCurrentGameState(): GameState {
        return state.value
    }

    private fun isValidMove(fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val piece = state.value.board[fromY][fromX] ?: return false

        // Validate piece movement
        when (piece.uppercaseChar()) {
            'P' -> {
                val direction = if (piece.isUpperCase()) -1 else 1 // White moves up, Black down
                if (fromX == toX && (toY - fromY == direction) && state.value.board[toY][toX] == null) {
                    return true // Move forward
                }
                if (Math.abs(fromX - toX) == 1 && (toY - fromY == direction) && state.value.board[toY][toX]?.isLowerCase() != (piece.isLowerCase())) {
                    return true // Capture diagonally
                }
            }

            'R' -> {
                // Rook movement: horizontal or vertical
                if (fromX == toX || fromY == toY) {
                    return isPathClear(fromX, fromY, toX, toY)
                }
            }

            'N' -> {
                // Knight movement: L shape
                if (Math.abs(fromX - toX) == 1 && Math.abs(fromY - toY) == 2 || Math.abs(fromX - toX) == 2 && Math.abs(
                        fromY - toY
                    ) == 1
                ) {
                    return true
                }
            }

            'B' -> {
                // Bishop movement: diagonal
                if (Math.abs(fromX - toX) == Math.abs(fromY - toY)) {
                    return isPathClear(fromX, fromY, toX, toY)
                }
            }

            'Q' -> {
                // Queen movement: horizontal, vertical, or diagonal
                if (fromX == toX || fromY == toY || Math.abs(fromX - toX) == Math.abs(fromY - toY)) {
                    return isPathClear(fromX, fromY, toX, toY)
                }
            }

            'K' -> {
                // King movement: one square in any direction
                if (Math.abs(fromX - toX) <= 1 && Math.abs(fromY - toY) <= 1) {
                    return true
                }
            }

            else -> return false // Unsupported piece
        }
        return false
    }

    private fun isPathClear(fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val stepX = if (toX > fromX) 1 else if (toX < fromX) -1 else 0
        val stepY = if (toY > fromY) 1 else if (toY < fromY) -1 else 0

        var x = fromX + stepX
        var y = fromY + stepY

        while (x != toX || y != toY) {
            if (state.value.board[y][x] != null) {
                return false // Blocked
            }
            x += stepX
            y += stepY
        }
        return true
    }

    private fun evaluateGameStatus(board: Array<Array<Char?>>, player: Char): GameStatus {
        val kingInCheck = isKingInCheck(board, player)
        val kingPosition = findKingPosition(board, player)

        if (kingInCheck && isCheckmate(board, player)) {
            return GameStatus.CHECKMATE
        } else if (kingInCheck) {
            return GameStatus.CHECK
        }
        return GameStatus.IDLE
    }

    private fun isKingInCheck(board: Array<Array<Char?>>, player: Char): Boolean {
        val kingPosition = findKingPosition(board, player)
        for (y in board.indices) {
            for (x in board[y].indices) {
                val piece = board[y][x]
                if (piece != null && piece.isLowerCase() != player.isLowerCase() && isValidMove(
                        x,
                        y,
                        kingPosition.first,
                        kingPosition.second
                    )
                ) {
                    return true // King is in check
                }
            }
        }
        return false
    }

    private fun findKingPosition(board: Array<Array<Char?>>, player: Char): Pair<Int, Int> {
        val kingSymbol = if (player == 'W') 'K' else 'k'
        for (y in board.indices) {
            for (x in board[y].indices) {
                if (board[y][x] == kingSymbol) {
                    return Pair(x, y) // Return coordinates of the king
                }
            }
        }
        throw IllegalStateException("King not found for player $player")
    }

    private fun isCheckmate(board: Array<Array<Char?>>, player: Char): Boolean {
        val kingPosition = findKingPosition(board, player)
        for (y in board.indices) {
            for (x in board[y].indices) {
                val piece = board[y][x]
                if (piece != null && piece.isLowerCase() == player.isLowerCase()) {
                    // Check if this piece can make a valid move that gets the king out of check
                    for (toY in board.indices) {
                        for (toX in board[toY].indices) {
                            if (isValidMove(x, y, toX, toY)) {
                                val newBoard = board.map { row -> row.clone() }.toTypedArray()
                                newBoard[toY][toX] = newBoard[y][x]
                                newBoard[y][x] = null
                                if (!isKingInCheck(newBoard, player)) {
                                    return false // There's a valid move, not checkmate
                                }
                            }
                        }
                    }
                }
            }
        }
        return true // No valid moves, checkmate
    }
}

