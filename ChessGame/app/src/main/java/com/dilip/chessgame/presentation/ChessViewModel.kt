package com.dilip.chessgame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dilip.chessgame.data.GameState
import com.dilip.chessgame.data.MovePiece
import com.dilip.chessgame.data.RealtimeMessagingClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

class ChessViewModel @Inject constructor(
    private val client: RealtimeMessagingClient
) : ViewModel() {

    // Live game state streaming from the WebSocket client
    val state = client
        .getGameStateStream()
        .onStart { _isConnecting.value = true }
        .onEach { _isConnecting.value = false }
        .catch { t -> _showConnectionError.value = t is ConnectException }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameState())

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _showConnectionError = MutableStateFlow(false)
    val showConnectionError = _showConnectionError.asStateFlow()

    fun finishTurn(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        // Check no winning player, valid move area
        if (state.value.board[fromY][fromX] == null || state.value.winningPlayer != null) {
            return
        }

        // Create a move action and send it through the WebSocket
        viewModelScope.launch {
            val piece = state.value.board[fromY][fromX] ?: return@launch
            val movePiece = MovePiece(fromX, fromY, toX, toY, piece)
            client.sendAction(movePiece)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            client.close()
        }
    }
}
