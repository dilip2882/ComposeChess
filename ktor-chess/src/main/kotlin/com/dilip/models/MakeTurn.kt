package com.dilip.models

import kotlinx.serialization.Serializable

@Serializable
data class MakeTurn(
    val fromX: Int, // Starting x-coordinate (0-7)
    val fromY: Int, // Starting y-coordinate (0-7)
    val toX: Int,   // Ending x-coordinate (0-7)
    val toY: Int,   // Ending y-coordinate (0-7)
    val piece: Char, // The piece being moved (e.g., 'P', 'R', 'N', etc. for White, 'p', 'r', 'n', etc. for Black)
    val isCapture: Boolean = false // Indicates if the move is a capture
)
