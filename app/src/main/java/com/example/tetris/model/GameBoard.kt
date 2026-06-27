package com.example.tetris.model

/**
 * 俄罗斯方块游戏板。
 * 标准尺寸：10列 x 20行。
 * 网格存储每个格子的颜色（ARGB Long），null表示空格。
 */
class GameBoard(
    val width: Int = BOARD_WIDTH,
    val height: Int = BOARD_HEIGHT,
) {
    companion object {
        const val BOARD_WIDTH = 10
        const val BOARD_HEIGHT = 20
    }

    private val grid: Array<Array<Long?>> = Array(height) { Array(width) { null } }

    /**
     * 获取指定位置的格子颜色。
     * 越界返回 null。
     */
    fun getCell(x: Int, y: Int): Long? {
        if (x < 0 || x >= width || y < 0 || y >= height) return null
        return grid[y][x]
    }

    /**
     * 检查指定位置是否被占据或越界。
     * y < 0（板上方生成区）视为合法。
     */
    fun isOccupied(x: Int, y: Int): Boolean {
        if (x < 0 || x >= width) return true
        if (y >= height) return true
        if (y < 0) return false
        return grid[y][x] != null
    }

    /**
     * 检查方块能否放在当前位置（无碰撞）。
     */
    fun canPlace(piece: Tetromino): Boolean {
        return piece.cells.none { (x, y) -> isOccupied(x, y) }
    }

    /**
     * 将方块锁定到游戏板上（永久放置）。
     */
    fun lockPiece(piece: Tetromino) {
        for ((x, y) in piece.cells) {
            if (y in 0 until height && x in 0 until width) {
                grid[y][x] = piece.color
            }
        }
    }

    /**
     * 清除所有满行，返回清除的行数。
     * 上方的行会下移填充空缺。
     */
    fun clearLines(): Int {
        var clearedCount = 0
        var y = 0
        while (y < height) {
            if (grid[y].all { it != null }) {
                // 将该行上方的所有行下移
                for (row in y downTo 1) {
                    grid[row] = grid[row - 1].copyOf()
                }
                grid[0] = Array(width) { null }
                clearedCount++
            } else {
                y++
            }
        }
        return clearedCount
    }

    /**
     * 计算方块从当前位置下落到最低点的 Y 坐标（用于幽灵方块）。
     */
    fun getDropY(piece: Tetromino): Int {
        var testPiece = piece
        while (canPlace(testPiece.moved(0, 1))) {
            testPiece = testPiece.moved(0, 1)
        }
        return testPiece.y
    }

    /**
     * 获取整个网格的快照（用于 UI 渲染）。
     * 返回新的二维数组，修改不影响原网格。
     */
    fun getGridSnapshot(): Array<Array<Long?>> {
        return Array(height) { row -> grid[row].copyOf() }
    }

    /**
     * 清空整个游戏板。
     */
    fun clear() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = null
            }
        }
    }
}
