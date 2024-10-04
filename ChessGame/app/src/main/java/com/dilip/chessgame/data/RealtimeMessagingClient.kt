package com.dilip.chessgame.data

import com.dilip.models.GameState
import kotlinx.coroutines.flow.Flow

interface RealtimeMessagingClient {
    fun getGameStateStream(): Flow<GameState>
    suspend fun sendAction(action: MovePiece)
    suspend fun close()
}