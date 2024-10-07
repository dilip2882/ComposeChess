package com.dilip.chessgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dilip.chessgame.presentation.ChessField
import com.dilip.chessgame.presentation.ChessViewModel
import com.dilip.chessgame.ui.theme.ChessGameTheme
import com.dilip.models.GameStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessGameTheme {
                val viewModel = hiltViewModel<ChessViewModel>()
                val state by viewModel.state.collectAsState()
                val isConnecting by viewModel.isConnecting.collectAsState()
                val showConnectionError by viewModel.showConnectionError.collectAsState()

                if (showConnectionError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Couldn't connect to the server",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    return@ChessGameTheme
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Show waiting status for players
                    Column(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        if (!state.connectedPlayers.contains('W')) {
                            Text(
                                text = "Waiting for player White",
                                fontSize = 32.sp
                            )
                        } else if (!state.connectedPlayers.contains('B')) {
                            Text(
                                text = "Waiting for player Black",
                                fontSize = 32.sp
                            )
                        }
                    }

                    // If both players are connected and no checkmate or stalemate, show the next turn
                    if (state.connectedPlayers.size == 2 && state.gameStatus == GameStatus.IDLE) {
                        Text(
                            text = if (state.playerAtTurn == 'W') {
                                "White's turn"
                            } else "Black's turn",
                            fontSize = 32.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                        )
                    }

                    // Chess field displaying the game state
                    ChessField(
                        state = state,
                        onTapInField = { fromX, fromY, toX, toY -> viewModel.finishTurn(fromX, fromY, toX, toY) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(16.dp)
                    )

                    // Endgame display: checkmate or stalemate
                    if (state.gameStatus != GameStatus.IDLE) {
                        Text(
                            text = when (state.gameStatus) {
                                GameStatus.CHECKMATE -> "${state.winningPlayer} won by checkmate!"
                                GameStatus.STALEMATE -> "It's a stalemate!"
                                else -> ""
                            },
                            fontSize = 32.sp,
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    // Show a loading spinner while connecting
                    if (isConnecting) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
