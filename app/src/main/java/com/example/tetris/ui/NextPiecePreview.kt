package com.example.tetris.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.Tetromino
import com.example.tetris.ui.theme.BoardBackground
import com.example.tetris.ui.theme.GridLine

/**
 * 下一个方块预览组件。
 * 在 4x4 网格中居中显示下一个方块。
 */
@Composable
fun NextPiecePreview(
    piece: Tetromino?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "下一个",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(BoardBackground)
                .border(1.dp, GridLine, RoundedCornerShape(8.dp)),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (piece == null) return@Canvas

                val grid = piece.shape
                val rows = grid.size
                val cols = grid[0].size

                // 计算实际填充范围以居中显示
                var minRow = rows
                var maxRow = -1
                var minCol = cols
                var maxCol = -1
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        if (grid[i][j]) {
                            minRow = minOf(minRow, i)
                            maxRow = maxOf(maxRow, i)
                            minCol = minOf(minCol, j)
                            maxCol = maxOf(maxCol, j)
                        }
                    }
                }
                if (maxRow < 0) return@Canvas

                val pieceRows = maxRow - minRow + 1
                val pieceCols = maxCol - minCol + 1
                val cellSize = minOf(size.width / 4f, size.height / 4f)
                val offsetX = (size.width - pieceCols * cellSize) / 2f
                val offsetY = (size.height - pieceRows * cellSize) / 2f
                val color = Color(piece.color)

                for (i in minRow..maxRow) {
                    for (j in minCol..maxCol) {
                        if (grid[i][j]) {
                            val x = offsetX + (j - minCol) * cellSize
                            val y = offsetY + (i - minRow) * cellSize
                            val padding = cellSize * 0.06f
                            val cellSizePadded = cellSize - padding * 2

                            // 主体填充
                            drawRect(
                                color = color,
                                topLeft = Offset(x + padding, y + padding),
                                size = Size(cellSizePadded, cellSizePadded),
                            )

                            // 高光
                            val highlight = color.copy(
                                red = (color.red + 0.25f).coerceAtMost(1f),
                                green = (color.green + 0.25f).coerceAtMost(1f),
                                blue = (color.blue + 0.25f).coerceAtMost(1f),
                            )
                            val hw = cellSizePadded * 0.18f
                            drawRect(
                                color = highlight,
                                topLeft = Offset(x + padding, y + padding),
                                size = Size(cellSizePadded, hw),
                            )
                            drawRect(
                                color = highlight,
                                topLeft = Offset(x + padding, y + padding),
                                size = Size(hw, cellSizePadded),
                            )

                            // 阴影
                            val shadow = color.copy(
                                red = (color.red - 0.3f).coerceAtLeast(0f),
                                green = (color.green - 0.3f).coerceAtLeast(0f),
                                blue = (color.blue - 0.3f).coerceAtLeast(0f),
                            )
                            drawRect(
                                color = shadow,
                                topLeft = Offset(x + padding + cellSizePadded - hw, y + padding),
                                size = Size(hw, cellSizePadded),
                            )
                            drawRect(
                                color = shadow,
                                topLeft = Offset(x + padding, y + padding + cellSizePadded - hw),
                                size = Size(cellSizePadded, hw),
                            )
                        }
                    }
                }
            }
        }
    }
}
