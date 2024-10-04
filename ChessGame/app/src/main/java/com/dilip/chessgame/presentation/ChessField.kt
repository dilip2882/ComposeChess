package com.dilip.chessgame.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dilip.models.GameState

@Composable
fun ChessField(
    state: GameState,
    modifier: Modifier = Modifier,
    onTapInField: (fromX: Int, fromY: Int, toX: Int, toY: Int) -> Unit
) {
    var selectedPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(true) {
                detectTapGestures { offset ->
                    val boardX = (8 * offset.x.toInt() / size.width)
                    val boardY = (8 * offset.y.toInt() / size.height)

                    if (selectedPosition == null) {
                        // First tap: Select the piece
                        if (state.board[boardY][boardX] != null) {
                            selectedPosition = Pair(boardX, boardY)
                        }
                    } else {
                        // Second tap: Move the piece
                        val (fromX, fromY) = selectedPosition!!
                        if (fromX != boardX || fromY != boardY) {
                            if (isValidMove(state, fromX, fromY, boardX, boardY)) {
                                onTapInField(fromX, fromY, boardX, boardY)
                            }
                        }
                        selectedPosition = null
                    }
                }
            }
    ) {
        drawChessBoard()
        drawPieces(state)
        selectedPosition?.let {
            drawSelectionHighlight(it.first, it.second)
        }
    }
}

private fun isValidMove(state: GameState, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
    val piece = state.board[fromY][fromX] ?: return false

    // General movement rules for each piece
    return when (piece.uppercaseChar()) {
        'P' -> isValidPawnMove(state, fromX, fromY, toX, toY)
        'R' -> isValidRookMove(state, fromX, fromY, toX, toY)
        'N' -> isValidKnightMove(fromX, fromY, toX, toY)
        'B' -> isValidBishopMove(state, fromX, fromY, toX, toY)
        'Q' -> isValidQueenMove(state, fromX, fromY, toX, toY)
        'K' -> isValidKingMove(state, fromX, fromY, toX, toY)
        else -> false
    }
}

// Pawn movement: Forward 1 step or diagonal capture, special rules for initial move and en passant
private fun isValidPawnMove(state: GameState, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
    val direction = if (state.playerAtTurn == 'W') -1 else 1
    val opponent = if (state.playerAtTurn == 'W') 'B' else 'W'

    // Move forward
    if (fromX == toX && state.board[toY][toX] == null) {
        if (fromY + direction == toY) return true
        if ((fromY == 1 || fromY == 6) && fromY + 2 * direction == toY && state.board[fromY + direction][fromX] == null) return true
    }

    // Capture
    if (fromX != toX && fromY + direction == toY) {
        val targetPiece = state.board[toY][toX]
        if (targetPiece != null && targetPiece.uppercaseChar() == opponent) {
            return true
        }
    }

    return false
}

// Rook movement: Horizontal or vertical, must be clear path
private fun isValidRookMove(state: GameState, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
    return (fromX == toX || fromY == toY) && isPathClear(state, fromX, fromY, toX, toY)
}

// Knight movement: L-shape (2 and 1 steps)
private fun isValidKnightMove(fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
    val dx = Math.abs(toX - fromX)
    val dy = Math.abs(toY - fromY)
    return (dx == 2 && dy == 1) || (dx == 1 && dy == 2)
}

// Bishop movement: Diagonal, must be clear path
private fun isValidBishopMove(
    state: GameState,
    fromX: Int,
    fromY: Int,
    toX: Int,
    toY: Int
): Boolean {
    return Math.abs(toX - fromX) == Math.abs(toY - fromY) && isPathClear(
        state,
        fromX,
        fromY,
        toX,
        toY
    )
}

// Queen movement: Combination of rook and bishop
private fun isValidQueenMove(
    state: GameState,
    fromX: Int,
    fromY: Int,
    toX: Int,
    toY: Int
): Boolean {
    return (isValidRookMove(state, fromX, fromY, toX, toY) || isValidBishopMove(
        state,
        fromX,
        fromY,
        toX,
        toY
    ))
}

// King movement: One step in any direction
private fun isValidKingMove(state: GameState, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
    val dx = Math.abs(toX - fromX)
    val dy = Math.abs(toY - fromY)
    return dx <= 1 && dy <= 1
}

// Checks if path between two positions is clear (for Rook, Bishop, Queen)
private fun isPathClear(state: GameState, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
    val dx = Integer.signum(toX - fromX)
    val dy = Integer.signum(toY - fromY)
    var x = fromX + dx
    var y = fromY + dy

    while (x != toX || y != toY) {
        if (state.board[y][x] != null) return false
        x += dx
        y += dy
    }
    return true
}

private fun DrawScope.drawChessBoard() {
    val tileSize = size.width / 8
    for (x in 0 until 8) {
        for (y in 0 until 8) {
            val color = if ((x + y) % 2 == 0) Color.White else Color.Gray
            drawRect(
                color = color,
                topLeft = Offset(x * tileSize, y * tileSize),
                size = androidx.compose.ui.geometry.Size(tileSize, tileSize)
            )
        }
    }
}

private fun DrawScope.drawPieces(state: GameState) {
    val tileSize = size.width / 8
    state.board.forEachIndexed { y, row ->
        row.forEachIndexed { x, piece ->
            piece?.let {
                val center = Offset(
                    x = x * tileSize + tileSize / 2,
                    y = y * tileSize + tileSize / 2
                )
                drawPiece(it, center)
            }
        }
    }
}

private fun DrawScope.drawPiece(piece: Char, center: Offset) {
    drawContext.canvas.nativeCanvas.drawText(
        piece.toString(),
        center.x,
        center.y,
        android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
        }
    )
}

private fun DrawScope.drawSelectionHighlight(x: Int, y: Int) {
    val tileSize = size.width / 8
    drawRect(
        color = Color.Yellow.copy(alpha = 0.5f),
        topLeft = Offset(x * tileSize, y * tileSize),
        size = androidx.compose.ui.geometry.Size(tileSize, tileSize),
    )
}

@Preview(showBackground = true)
@Composable
fun ChessFieldPreview() {
    ChessField(
        state = GameState(
            board = arrayOf(
                arrayOf('R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'),
                arrayOf('P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf('p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'),
                arrayOf('r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'),
            ),
            playerAtTurn = 'W',
        ),
        onTapInField = { _, _, _, _ -> },
        modifier = Modifier.size(300.dp)
    )
}