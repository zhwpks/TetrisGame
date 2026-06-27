package com.example.tetris.model

/**
 * 游戏状态枚举。
 */
enum class GameStatus {
    Ready,    // 等待开始
    Playing,  // 游戏进行中
    Paused,   // 暂停
    GameOver, // 游戏结束
}

/**
 * 俄罗斯方块完整游戏状态。
 *
 * @param boardGrid    游戏板网格快照，[row][col] = 颜色或 null
 * @param currentPiece 当前方块
 * @param nextPiece    下一个方块
 * @param ghostY       幽灵方块的 Y 坐标（当前方块的预测落点）
 * @param score        得分
 * @param level        当前等级
 * @param lines        总消除行数
 * @param status       游戏状态
 * @param highScore    最高分
 */
data class GameState(
    val boardGrid: Array<Array<Long?>> = Array(GameBoard.BOARD_HEIGHT) { Array(GameBoard.BOARD_WIDTH) { null } },
    val currentPiece: Tetromino? = null,
    val nextPiece: Tetromino? = null,
    val ghostY: Int = 0,
    val score: Int = 0,
    val level: Int = 1,
    val lines: Int = 0,
    val status: GameStatus = GameStatus.Ready,
    val highScore: Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        return score == other.score &&
            level == other.level &&
            lines == other.lines &&
            status == other.status &&
            highScore == other.highScore &&
            ghostY == other.ghostY &&
            currentPiece == other.currentPiece &&
            nextPiece == other.nextPiece &&
            boardGrid.contentDeepEquals(other.boardGrid)
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + level
        result = 31 * result + lines
        result = 31 * result + status.hashCode()
        result = 31 * result + highScore
        result = 31 * result + ghostY
        result = 31 * result + (currentPiece?.hashCode() ?: 0)
        result = 31 * result + (nextPiece?.hashCode() ?: 0)
        return result
    }
}
