package com.example.aventuranumeral

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Composable
fun GameScreen() {

    val playerSprite = ImageBitmap.imageResource(id = R.drawable.avatargirl1run)

    // ===== WORLD, LEVEL, PLAYER, etc =====
    val groundY = 700f
    val playerSize = 80f
    val platforms = listOf(
        Platform(0f, groundY + playerSize, 600f, 100f),
        Platform(900f, groundY + playerSize, 800f, 100f),
        Platform(1800f, groundY + playerSize, 800f, 100f),
        Platform(2700f, groundY + playerSize, 800f, 100f),
        Platform(500f, groundY - 150f, 200f, 30f),
        Platform(900f, groundY - 250f, 200f, 30f),
        Platform(1300f, groundY - 180f, 200f, 30f),
        Platform(1700f, groundY - 250f, 200f, 30f),
        Platform(2100f, groundY - 180f, 200f, 30f),
        Platform(2500f, groundY - 250f, 200f, 30f)
    )

    var playerX by remember { mutableFloatStateOf(100f) }
    var playerY by remember { mutableFloatStateOf(groundY) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    var cameraX by remember { mutableFloatStateOf(0f) }
    var moveLeft by remember { mutableStateOf(false) }
    var moveRight by remember { mutableStateOf(false) }

    val moveSpeed = 400f
    val gravity = 2500f
    val jumpForce = -1100f

    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val delta = (now - lastTime) / 1_000_000_000f
            lastTime = now

            if (moveLeft) playerX -= moveSpeed * delta
            if (moveRight) playerX += moveSpeed * delta

            velocityY += gravity * delta
            playerY += velocityY * delta

            for (platform in platforms) {
                val isFalling = velocityY > 0
                val collides =
                    playerX + playerSize > platform.x &&
                            playerX < platform.x + platform.width &&
                            playerY + playerSize > platform.y &&
                            playerY + playerSize < platform.y + platform.height &&
                            isFalling
                if (collides) {
                    playerY = platform.y - playerSize
                    velocityY = 0f
                }
            }

            cameraX = playerX - 200f

            if (playerY > 1300f) {
                playerX = 100f
                playerY = groundY
                velocityY = 0f
            }

            kotlinx.coroutines.delay(16L)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF87CEEB))
                .pointerInteropFilter { event ->

                    var left = false
                    var right = false
                    var jump = false

                    if (event.actionMasked == android.view.MotionEvent.ACTION_UP ||
                        event.actionMasked == android.view.MotionEvent.ACTION_CANCEL
                    ) {
                        moveLeft = false
                        moveRight = false
                        return@pointerInteropFilter true
                    }

                    for (i in 0 until event.pointerCount) {
                        val x = event.getX(i)
                        val y = event.getY(i)

                        if (y > screenHeight * 0.65f) {

                            // LEFT
                            if (x < screenWidth * 0.25f) {
                                left = true
                            }

                            // RIGHT
                            if (x in (screenWidth * 0.25f)..(screenWidth * 0.5f)) {
                                right = true
                            }

                            // JUMP
                            if (x > screenWidth * 0.7f) {
                                jump = true
                            }
                        }
                    }

                    moveLeft = left
                    moveRight = right

                    if (
                        (event.actionMasked == android.view.MotionEvent.ACTION_DOWN ||
                                event.actionMasked == android.view.MotionEvent.ACTION_POINTER_DOWN) &&
                        jump &&
                        velocityY == 0f
                    ) {
                        velocityY = jumpForce
                    }

                    true
                }
        ) {
            for (platform in platforms) {
                drawRect(
                    color = Color(0xFF6FCF97),
                    topLeft = Offset(platform.x - cameraX, platform.y),
                    size = Size(platform.width, platform.height)
                )
            }

            drawImage(
                image = playerSprite,
                topLeft = Offset(playerX - cameraX, playerY)
            )

            drawCircle(
                color = Color(0xAA000000),
                radius = 70f,
                center = Offset(size.width * 0.15f, size.height * 0.8f)
            )
            drawCircle(
                color = Color(0xAA000000),
                radius = 70f,
                center = Offset(size.width * 0.35f, size.height * 0.8f)
            )
            drawCircle(
                color = Color(0xAA000000),
                radius = 70f,
                center = Offset(size.width * 0.85f, size.height * 0.8f)
            )
        }
    }
}
