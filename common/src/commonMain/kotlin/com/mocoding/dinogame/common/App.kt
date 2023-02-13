package com.mocoding.dinogame.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun App() {
    var score by remember { mutableStateOf(0) }
    var highestScore by remember { mutableStateOf(0) }

    var isJumping by remember { mutableStateOf(false) }
    var playerState by remember { mutableStateOf(PlayerState.Stop) }
    var isObstacleMoving by remember { mutableStateOf(false) }

    val jumpProgress = remember { Animatable(0f) }

    LaunchedEffect(isJumping) {
        if (isJumping
            && playerState != PlayerState.Dead
            && playerState != PlayerState.Stop) {
            jumpProgress.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 600
                    1f at 300 with EaseIn
                    0f at 600 with EaseOut
                }
            )
        }
    }

    LaunchedEffect(jumpProgress.value) {
        if (isJumping && !jumpProgress.isRunning && jumpProgress.value == 0f) {
            isJumping = false
        }
    }

    val obstacleProgress = remember { Animatable(0f) }

    var canvasSize by remember { mutableStateOf(Size.Unspecified) }

    LaunchedEffect(obstacleProgress.value) {
        if (canvasSize.isUnspecified) return@LaunchedEffect

        val playerSize = Size(100f, 100f)
        val obstacleSize = Size(60f, 100f)

        val playerY = canvasSize.height - playerSize.height - ((canvasSize.height - playerSize.height) * jumpProgress.value)
        val obstacleX = (canvasSize.width + 100f) * (1f - obstacleProgress.value) - 80f

        val playerWidthRange = (200..(200 + playerSize.width.roundToInt()))
        val playerHeightRange = (playerY).roundToInt()..(playerY + playerSize.height).roundToInt()

        val obstacleWidthRange = (obstacleX.roundToInt()..(obstacleX + obstacleSize.width).roundToInt())
        val obstacleHeightRange = (canvasSize.height - obstacleSize.height).roundToInt()..canvasSize.height.roundToInt()
        if (
            playerWidthRange.any { it in obstacleWidthRange }
        ) {
            if (playerHeightRange.any { it in obstacleHeightRange }) {
                playerState = PlayerState.Dead
            } else {
                playerState = PlayerState.OnTopOfAnObstacle
            }
        } else if ( playerState == PlayerState.OnTopOfAnObstacle) {
            playerState = PlayerState.PassedAnObstacle
        }
    }

    LaunchedEffect(playerState) {
        println(playerState)
        when (playerState) {
            PlayerState.Stop -> {
                isObstacleMoving = false
            }
            PlayerState.Start -> {
                score = 0
                jumpProgress.snapTo(0f)
                obstacleProgress.snapTo(0f)
                isJumping = false
                isObstacleMoving = true
            }
            PlayerState.OnTopOfAnObstacle -> {}
            PlayerState.PassedAnObstacle -> score++
            PlayerState.Dead -> {
                jumpProgress.stop()
                obstacleProgress.stop()
                isObstacleMoving = false
                highestScore = max(score, highestScore)
            }
        }
    }

    LaunchedEffect(isObstacleMoving) {
        if (isObstacleMoving) {
            obstacleProgress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000,
                        delayMillis = 400,
                        easing = LinearEasing
                    ),
                )
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(true) {
                detectTapGestures {
                    isJumping = true
                }
            }
    ) {
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(60.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            canvasSize = size

            val playerSize = Size(100f, 100f)
            val obstacleSize = Size(60f, 100f)

            val topLeft = Offset(
                x = 200f,
                y = size.height - 100f - ((size.height - 100f) * jumpProgress.value)
            )

            val obstacleX = (size.width + 100f) * (1f - obstacleProgress.value) - 80f
            drawRoundRect(
                color = Color.LightGray,
                topLeft = Offset(obstacleX, size.height - obstacleSize.height),
                size = obstacleSize,
                cornerRadius = CornerRadius(20f, 10f)
            )

            drawRect(
                color = Color.DarkGray,
                topLeft = topLeft,
                size = playerSize
            )

            drawLine(
                color = Color.LightGray,
                start = Offset(x = 0f, y = size.height),
                end = Offset(x = size.width, y = size.height),
                strokeWidth = 6f
            )
        }

        if (playerState == PlayerState.Stop
            || playerState == PlayerState.Dead) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = .4f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 60.dp)
                ) {
                    Text(
                        text = "Highest Score: $highestScore",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .padding(10.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { playerState = PlayerState.Start },
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Play"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Start playing",
                            style = MaterialTheme.typography.h5
                        )
                    }
                }
            }
        }
    }
}
