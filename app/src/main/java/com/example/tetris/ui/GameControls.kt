package com.example.tetris.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tetris.ui.theme.AccentCyan
import com.example.tetris.ui.theme.PrimaryPurple

/**
 * 游戏控制按钮组。
 * 底部排列5个控制按钮：左移、旋转、软降、右移、硬降。
 */
@Composable
fun GameControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveDown: () -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlButton(
                icon = Icons.Filled.ArrowLeft,
                contentDescription = "左移",
                onClick = onMoveLeft,
                enabled = enabled,
            )
            ControlButton(
                icon = Icons.Filled.Rotate90DegreesCcw,
                contentDescription = "旋转",
                onClick = onRotate,
                enabled = enabled,
                containerColor = PrimaryPurple,
            )
            ControlButton(
                icon = Icons.Filled.ArrowRight,
                contentDescription = "右移",
                onClick = onMoveRight,
                enabled = enabled,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlButton(
                icon = Icons.Filled.ArrowDownward,
                contentDescription = "软降",
                onClick = onMoveDown,
                enabled = enabled,
            )
            ControlButton(
                icon = Icons.Filled.KeyboardDoubleArrowDown,
                contentDescription = "硬降",
                onClick = onHardDrop,
                enabled = enabled,
                containerColor = AccentCyan,
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = androidx.compose.ui.graphics.Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp),
        )
    }
}
