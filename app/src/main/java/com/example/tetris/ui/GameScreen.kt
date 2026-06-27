package com.example.tetris.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.GameStatus
import com.example.tetris.model.GameState
import com.example.tetris.ui.theme.AccentCyan
import com.example.tetris.ui.theme.PrimaryPurple
import com.example.tetris.ui.theme.SurfaceVariant
import com.example.tetris.ui.theme.TextAccent
import com.example.tetris.ui.theme.TextSecondary
import com.example.tetris.viewmodel.TetrisViewModel

/**
 * 信息卡片：显示标题和数值。
 */
@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = TextAccent,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * 顶部信息栏：分数、等级、行数。
 */
@Composable
private fun TopBar(
    state: GameState,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        InfoCard(
            title = "分数",
            value = state.score.toString(),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            title = "等级",
            value = state.level.toString(),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            title = "行数",
            value = state.lines.toString(),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (state.status == GameStatus.Playing || state.status == GameStatus.Paused) {
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .background(SurfaceVariant, RoundedCornerShape(8.dp)),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = if (state.status == GameStatus.Playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.status == GameStatus.Playing) "暂停" else "继续",
                )
            }
        }
    }
}

/**
 * 游戏状态覆盖层。
 */
@Composable
private fun GameStateOverlay(
    title: String,
    subtitle: String? = null,
    buttonText: String,
    onButtonClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextAccent,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = buttonText,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * 俄罗斯方块主游戏界面。
 */
@Composable
fun GameScreen(
    viewModel: TetrisViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // 顶部信息栏
                TopBar(
                    state = state,
                    onPauseClick = {
                        when (state.status) {
                            GameStatus.Playing -> viewModel.pauseGame()
                            GameStatus.Paused -> viewModel.resumeGame()
                            else -> {}
                        }
                    },
                )

                // 主游戏区域：游戏板 + 右侧面板
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // 游戏板
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        GameBoardView(
                            state = state,
                            modifier = Modifier.fillMaxHeight(),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 右侧面板
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        NextPiecePreview(
                            piece = state.nextPiece,
                        )
                        InfoCard(
                            title = "最高分",
                            value = state.highScore.toString(),
                            accentColor = AccentCyan,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // 底部控制按钮
                GameControls(
                    onMoveLeft = viewModel::moveLeft,
                    onMoveRight = viewModel::moveRight,
                    onMoveDown = viewModel::moveDown,
                    onRotate = viewModel::rotate,
                    onHardDrop = viewModel::hardDrop,
                    enabled = state.status == GameStatus.Playing,
                    modifier = Modifier.navigationBarsPadding(),
                )
            }

            // 游戏状态覆盖层
            when (state.status) {
                GameStatus.Ready -> {
                    GameStateOverlay(
                        title = "俄罗斯方块",
                        subtitle = "准备好了吗？",
                        buttonText = "开始游戏",
                        onButtonClick = { viewModel.startGame() },
                    )
                }
                GameStatus.GameOver -> {
                    GameStateOverlay(
                        title = "游戏结束",
                        subtitle = "得分: ${state.score}  最高: ${state.highScore}",
                        buttonText = "再来一局",
                        onButtonClick = { viewModel.startGame() },
                    )
                }
                GameStatus.Paused -> {
                    GameStateOverlay(
                        title = "已暂停",
                        buttonText = "继续游戏",
                        onButtonClick = { viewModel.resumeGame() },
                    )
                }
                else -> {}
            }
        }
    }
}
