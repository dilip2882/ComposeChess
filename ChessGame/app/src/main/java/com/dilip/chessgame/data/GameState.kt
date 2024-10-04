package com.dilip.models

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val playerAtTurn: Char? = 'W', // 'W' for White, 'B' for Black
    val board: Array<Array<Char?>> = emptyBoard(),
    val gameStatus: GameStatus = GameStatus.IDLE,
    val winningPlayer: Char? = null,
    val connectedPlayers: List<Char> = emptyList()
) {
    companion object {
        fun emptyBoard(): Array<Array<Char?>> {
            return arrayOf(
                arrayOf('R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'), // 1st rank (White)
                arrayOf('P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'), // 2nd rank (White)
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf('p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'), // 7th rank (Black)
                arrayOf('r', 'n', 'b', 'q', 'k', 'b', 'n', 'r')  // 8th rank (Black)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (playerAtTurn != other.playerAtTurn) return false
        if (!board.contentDeepEquals(other.board)) return false
        if (gameStatus != other.gameStatus) return false
        if (winningPlayer != other.winningPlayer) return false
        if (connectedPlayers != other.connectedPlayers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerAtTurn?.hashCode() ?: 0
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + gameStatus.hashCode()
        result = 31 * result + (winningPlayer?.hashCode() ?: 0)
        result = 31 * result + connectedPlayers.hashCode()
        return result
    }
}

enum class GameStatus {
    IDLE,
    CHECK,
    CHECKMATE,
    STALEMATE
}
