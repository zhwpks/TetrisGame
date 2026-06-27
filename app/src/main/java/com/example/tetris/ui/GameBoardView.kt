package com.example.tetris.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.tetris.model.GameBoard as TetrisBoard
import com.example.tetris.model.GameState
import com.example.tetris.model.Tetromino
import com.example.tetris.ui.theme.BoardBackground
import com.example.tetris.ui.theme.GridLine

/**
 * 绘制单个方块格子（3D 凸起效果）。
 */
private fun DrawScope.drawCell(
    x: Float,
    y: Float,
    cellSize: Float,
    color: Color,
    isGhost: Boolean = false,
) {
    val padding = cellSize * 0.04f
    val cellRectSize = cellSize - padding * 2
    val left = x + padding
    val top = y + padding

    if (isGhost) {
        // 幽灵方块：半透明描边
        drawRect(
            color = color.copy(alpha = 0.15f),
            topLeft = Offset(left, top),
            size = Size(cellRectSize, cellRectSize),
        )
        drawRect(
            color = color.copy(alpha = 0.5f),
            topLeft = Offset(left, top),
            size = Size(cellRectSize, cellRectSize),
            style = Stroke(width = 2f),
        )
        return
    }

    // 主体填充
    drawRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(cellRectSize, cellRectSize),
    )

    // 高光（左上角）
    val highlight = color.copy(
        red = (color.red + 0.25f).coerceAtMost(1f),
        green = (color.green + 0.25f).coerceAtMost(1f),
        blue = (color.blue + 0.25f).coerceAtMost(1f),
    )
    val highlightWidth = cellRectSize * 0.18f
    drawRect(
        color = highlight,
        topLeft = Offset(left, top),
        size = Size(cellRectSize, highlightWidth),
    )
    drawRect(
        color = highlight,
        topLeft = Offset(left, top),
        size = Size(highlightWidth, cellRectSize),
    )

    // 阴影（右下角）
    val shadow = color.copy(
        red = (color.red - 0.3f).coerceAtLeast(0f),
        green = (color.green - 0.3f).coerceAtLeast(0f),
        blue = (color.blue - 0.3f).coerceAtLeast(0f),
    )
    drawRect(
        color = shadow,
        topLeft = Offset(left + cellRectSize - highlightWidth, top),
        size = Size(highlightWidth, cellRectSize),
    )
    drawRect(
        color = shadow,
        topLeft = Offset(left, top + cellRectSize - highlightWidth),
        size = Size(cellRectSize, highlightWidth),
    )
}

/**
 * 俄罗斯方块游戏板视图。
 * 使用 Canvas 高效渲染 10x20 网格、已锁定方块、当前方块和幽灵方块。
 */
@Composable
fun GameBoardView(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(TetrisBoard.BOARD_WIDTH.toFloat() / TetrisBoard.BOARD_HEIGHT.toFloat())
            .clip(RoundedCornerShape(8.dp))
            .background(BoardBackground)
            .border(2.dp, GridLine, RoundedCornerShape(8.dp)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / TetrisBoard.BOARD_WIDTH
            val cellHeight = size.height / TetrisBoard.BOARD_HEIGHT
            val cellSize = minOf(cellWidth, cellHeight)

            // 绘制网格线
            for (col in 0..TetrisBoard.BOARD_WIDTH) {
                val x = col * cellSize
                drawLine(
                    color = GridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f,
                )
            }
            for (row in 0..TetrisBoard.BOARD_HEIGHT) {
                val y = row * cellSize
                drawLine(
                    color = GridLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }

            // 绘制已锁定的方块
            for (row in 0 until TetrisBoard.BOARD_HEIGHT) {
                for (col in 0 until TetrisBoard.BOARD_WIDTH) {
                    val cellColor = state.boardGrid[row][col]
                    if (cellColor != null) {
                        drawCell(
                            x = col * cellSize,
                            y = row * cellSize,
                            cellSize = cellSize,
                            color = Color(cellColor),
                        )
                    }
                }
            }

            // 绘制幽灵方块（预测落点）
            state.currentPiece?.let { piece ->
                val ghostPiece = piece.copy(y = state.ghostY)
                if (ghostPiece.y > piece.y) {
                    for ((col, row) in ghostPiece.cells) {
                        if (row in 0 until TetrisBoard.BOARD_HEIGHT && col in 0 until TetrisBoard.BOARD_WIDTH) {
                            drawCell(
                                x = col * cellSize,
                                y = row * cellSize,
                                cellSize = cellSize,
                                color = Color(piece.color),
                                isGhost = true,
                            )
                        }
                    }
                }
            }

            // 绘制当前方块
            state.currentPiece?.let { piece ->
                for ((col, row) in piece.cells) {
                    if (row >= 0) {
                        drawCell(
                            x = col * cellSize,
                            y = row * cellSize,
                            cellSize = cellSize,
                            color = Color(piece.color),
                        )
                    }
                }
            }
        }
    }
}
