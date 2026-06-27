package com.example.tetris.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetris.model.GameBoard
import com.example.tetris.model.GameState
import com.example.tetris.model.GameStatus
import com.example.tetris.model.PieceType
import com.example.tetris.model.Tetromino
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 俄罗斯方块 ViewModel。
 * 管理游戏状态、游戏循环、玩家输入和计分系统。
 */
class TetrisViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TetrisViewModel"
        private const val PREFS_NAME = "tetris_prefs"
        private const val KEY_HIGH_SCORE = "high_score"

        /** 消行得分表：[0, 1, 2, 3, 4] 行 -> 基础分 */
        private val LINE_SCORES = intArrayOf(0, 100, 300, 500, 800)

        /** 每多少行升一级 */
        private const val LINES_PER_LEVEL = 10

        /** 初始下落间隔（毫秒） */
        private const val BASE_DROP_INTERVAL = 800L

        /** 每级减少的下落间隔（毫秒） */
        private const val DROP_INTERVAL_DECREMENT = 60L

        /** 最小下落间隔（毫秒） */
        private const val MIN_DROP_INTERVAL = 100L
    }

    private val board = GameBoard()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        GameState(highScore = prefs.getInt(KEY_HIGH_SCORE, 0))
    )
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var currentPiece: Tetromino? = null
    private var nextPiece: Tetromino? = null
    private var score = 0
    private var level = 1
    private var totalLines = 0
    private var highScore = prefs.getInt(KEY_HIGH_SCORE, 0)
    private var gameLoopJob: Job? = null

    // ==================== 公开操作 ====================

    /**
     * 开始新游戏。
     */
    fun startGame() {
        Log.i(TAG, "startGame: starting new game")
        board.clear()
        score = 0
        level = 1
        totalLines = 0
        nextPiece = createRandomPiece()
        spawnPiece()
        _uiState.value = _uiState.value.copy(
            boardGrid = board.getGridSnapshot(),
            currentPiece = currentPiece,
            nextPiece = nextPiece,
            score = score,
            level = level,
            lines = totalLines,
            status = GameStatus.Playing,
            highScore = highScore,
        )
        startGameLoop()
    }

    /**
     * 暂停游戏。
     */
    fun pauseGame() {
        if (_uiState.value.status != GameStatus.Playing) return
        Log.i(TAG, "pauseGame")
        gameLoopJob?.cancel()
        updateStatus(GameStatus.Paused)
    }

    /**
     * 恢复游戏。
     */
    fun resumeGame() {
        if (_uiState.value.status != GameStatus.Paused) return
        Log.i(TAG, "resumeGame")
        updateStatus(GameStatus.Playing)
        startGameLoop()
    }

    /**
     * 向左移动当前方块。
     */
    fun moveLeft() {
        if (_uiState.value.status != GameStatus.Playing) return
        currentPiece?.let { piece ->
            val moved = piece.moved(-1, 0)
            if (board.canPlace(moved)) {
                currentPiece = moved
                updatePieceState()
            }
        }
    }

    /**
     * 向右移动当前方块。
     */
    fun moveRight() {
        if (_uiState.value.status != GameStatus.Playing) return
        currentPiece?.let { piece ->
            val moved = piece.moved(1, 0)
            if (board.canPlace(moved)) {
                currentPiece = moved
                updatePieceState()
            }
        }
    }

    /**
     * 软降（下移一格），得1分。
     */
    fun moveDown() {
        if (_uiState.value.status != GameStatus.Playing) return
        currentPiece?.let { piece ->
            val moved = piece.moved(0, 1)
            if (board.canPlace(moved)) {
                currentPiece = moved
                score += 1
                updatePieceState()
            } else {
                lockPiece()
            }
        }
    }

    /**
     * 旋转当前方块（带简易踢墙）。
     */
    fun rotate() {
        if (_uiState.value.status != GameStatus.Playing) return
        currentPiece?.let { piece ->
            val rotated = piece.rotated()
            // 尝试原位旋转
            if (board.canPlace(rotated)) {
                currentPiece = rotated
                updatePieceState()
                return
            }
            // 简易踢墙：尝试左右平移 1-2 格
            for (offset in intArrayOf(1, -1, 2, -2)) {
                val kicked = rotated.moved(offset, 0)
                if (board.canPlace(kicked)) {
                    currentPiece = kicked
                    updatePieceState()
                    return
                }
            }
        }
    }

    /**
     * 硬降（直接落到底部），每格得2分。
     */
    fun hardDrop() {
        if (_uiState.value.status != GameStatus.Playing) return
        currentPiece?.let { piece ->
            val dropY = board.getDropY(piece)
            val dropDistance = dropY - piece.y
            score += dropDistance * 2
            currentPiece = piece.copy(y = dropY)
            lockPiece()
        }
    }

    // ==================== 游戏循环 ====================

    /**
     * 启动游戏循环，按等级间隔自动下落。
     */
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_uiState.value.status == GameStatus.Playing) {
                delay(getDropInterval())
                if (_uiState.value.status == GameStatus.Playing) {
                    autoTick()
                }
            }
        }
    }

    /**
     * 自动下落一格（游戏循环触发）。
     */
    private fun autoTick() {
        currentPiece?.let { piece ->
            val moved = piece.moved(0, 1)
            if (board.canPlace(moved)) {
                currentPiece = moved
                updatePieceState()
            } else {
                lockPiece()
            }
        }
    }

    // ==================== 内部逻辑 ====================

    /**
     * 生成随机方块，在板顶居中出生。
     */
    private fun createRandomPiece(): Tetromino {
        val type = PieceType.random()
        val startX = (GameBoard.BOARD_WIDTH - getPieceWidth(type)) / 2
        return Tetromino(type = type, x = startX, y = 0)
    }

    /**
     * 获取方块初始形状的宽度（用于居中）。
     */
    private fun getPieceWidth(type: PieceType): Int {
        return PieceType.getShape(type, 0)[0].size
    }

    /**
     * 生成新方块。如果无法放置则游戏结束。
     */
    private fun spawnPiece() {
        currentPiece = nextPiece ?: createRandomPiece()
        nextPiece = createRandomPiece()

        if (!board.canPlace(currentPiece!!)) {
            Log.i(TAG, "spawnPiece: game over")
            gameOver()
        }
    }

    /**
     * 锁定当前方块到游戏板，消除满行，更新分数，生成新方块。
     */
    private fun lockPiece() {
        val piece = currentPiece ?: return
        board.lockPiece(piece)

        val clearedLines = board.clearLines()
        if (clearedLines > 0) {
            totalLines += clearedLines
            score += LINE_SCORES[clearedLines] * level

            // 检查升级
            val newLevel = (totalLines / LINES_PER_LEVEL) + 1
            if (newLevel > level) {
                level = newLevel
                Log.i(TAG, "lockPiece: level up to $level")
                // 升级后重启游戏循环以应用新速度
                if (_uiState.value.status == GameStatus.Playing) {
                    startGameLoop()
                }
            }
        }

        // 更新最高分
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt(KEY_HIGH_SCORE, highScore).apply()
        }

        spawnPiece()

        _uiState.value = _uiState.value.copy(
            boardGrid = board.getGridSnapshot(),
            currentPiece = currentPiece,
            nextPiece = nextPiece,
            ghostY = currentPiece?.let { board.getDropY(it) } ?: 0,
            score = score,
            level = level,
            lines = totalLines,
            status = _uiState.value.status,
            highScore = highScore,
        )
    }

    /**
     * 仅更新方块位置相关的状态（不改变游戏板）。
     */
    private fun updatePieceState() {
        val piece = currentPiece ?: return
        _uiState.value = _uiState.value.copy(
            currentPiece = piece,
            ghostY = board.getDropY(piece),
            score = score,
        )
    }

    /**
     * 更新游戏状态。
     */
    private fun updateStatus(status: GameStatus) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    /**
     * 游戏结束。
     */
    private fun gameOver() {
        gameLoopJob?.cancel()
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt(KEY_HIGH_SCORE, highScore).apply()
        }
        _uiState.value = _uiState.value.copy(
            status = GameStatus.GameOver,
            highScore = highScore,
        )
    }

    /**
     * 根据当前等级计算下落间隔。
     */
    private fun getDropInterval(): Long {
        return maxOf(MIN_DROP_INTERVAL, BASE_DROP_INTERVAL - (level - 1) * DROP_INTERVAL_DECREMENT)
    }

    /**
     * ViewModel 销毁时清理。
     */
    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
