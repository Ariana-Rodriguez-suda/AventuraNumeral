package com.example.aventuranumeral

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.imageResource
import kotlinx.coroutines.delay
import kotlin.math.abs

// ===== DATA =====

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class PushBlock(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    var velocityY: Float = 0f,
    var inHole: Boolean = false,
    var settled: Boolean = false
)

fun blocksOverlap(a: PushBlock, b: PushBlock): Boolean {
    return a.x < b.x + b.width &&
            a.x + a.width > b.x &&
            a.y < b.y + b.height &&
            a.y + a.height > b.y
}

// ===== GAME =====

@Composable
fun GameScreen() {

    val playerSprite = ImageBitmap.imageResource(R.drawable.avatargirl1run)
    val offFlag = ImageBitmap.imageResource(R.drawable.offflag)
    val onFlag = ImageBitmap.imageResource(R.drawable.onflag)
    var flagOn by remember { mutableStateOf(false) }

    // ===== WORLD =====
    val groundY = 700f
    val playerSize = 80f
    val spriteHeight = 200f
    val spriteOffsetY = spriteHeight - playerSize

    val holeX = 800f
    val holeWidth = 200f
    val hole2X = 2600f
    val hole2Width = 320f

    val flagX = holeX + holeWidth + 150f
    val flagHeight = 200f
    val floorTopY = groundY + playerSize
    val flagY = floorTopY - flagHeight

    val platforms = listOf(
        Platform(0f, groundY + playerSize, holeX, 100f),
        Platform(holeX + holeWidth, groundY + playerSize, hole2X - (holeX + holeWidth), 100f),
        Platform(hole2X + hole2Width, groundY + playerSize, 3000f, 100f),

        Platform(300f, groundY - 180f, 200f, 30f),
        Platform(500f, groundY - 300f, 200f, 30f),
        Platform(900f, groundY - 450f, 200f, 30f),
        Platform(1300f, groundY - 300f, 200f, 30f),
        Platform(1700f, groundY - 450f, 200f, 30f),
        Platform(2100f, groundY - 300f, 200f, 30f),
        Platform(2500f, groundY - 180f, 200f, 30f),
        Platform(2800f, groundY - 400f, 300f, 30f),
        Platform(3200f, groundY - 300f, 300f, 30f),
    )

    // ===== BLOCKS =====
    var blocks by remember {
        mutableStateOf(
            listOf(
                PushBlock(
                    x = 500f,
                    y = groundY,
                    width = 180f,
                    height = playerSize),

                PushBlock(
                    x = 920f,
                    y = groundY - 450f - playerSize,
                    width = playerSize,
                    height = playerSize,
                    velocityY = 0f,
                    inHole = false,
                    settled = true
                )
            )
        )
    }

    // BLOCK FIX: hole occupancy lock
    var holeOccupied by remember { mutableStateOf(false) }

    // ===== PLAYER =====
    var playerX by remember { mutableFloatStateOf(100f) }
    var playerY by remember { mutableFloatStateOf(groundY) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    var cameraX by remember { mutableFloatStateOf(0f) }

    var moveLeft by remember { mutableStateOf(false) }
    var moveRight by remember { mutableStateOf(false) }
    var pushing by remember { mutableStateOf(false) }

    // ===== PHYSICS =====
    val moveSpeed = 400f
    val pushForce = 260f
    val gravity = 2500f
    val jumpForce = -1100f

    // ===== GAME LOOP =====
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
                if (
                    velocityY > 0 &&
                    playerX + playerSize > platform.x &&
                    playerX < platform.x + platform.width &&
                    playerY + playerSize > platform.y &&
                    playerY + playerSize < platform.y + platform.height
                ) {
                    playerY = platform.y - playerSize
                    velocityY = 0f
                }
            }

            blocks = blocks.map { block ->

                if (!block.settled) {
                    block.velocityY += gravity * delta
                    block.y += block.velocityY * delta
                }

                val centerX = block.x + block.width / 2
                if (
                    !block.inHole &&
                    block.y >= groundY &&
                    centerX > holeX &&
                    centerX < holeX + holeWidth
                ) {
                    block.inHole = true
                    block.settled = false
                }

                if (!block.inHole && block.y >= groundY) {
                    block.y = groundY
                    block.velocityY = 0f
                    block.settled = true
                }

                val holeFloorY = groundY + playerSize
                if (block.inHole && block.y >= holeFloorY) {
                    block.y = holeFloorY
                    block.velocityY = 0f
                    block.settled = true
                    block.x = holeX
                }

                var onPlatform = false
                for (platform in platforms) {
                    if (
                        block.velocityY >= 0 &&
                        block.x + block.width > platform.x &&
                        block.x < platform.x + platform.width &&
                        block.y + block.height >= platform.y &&
                        block.y + block.height <= platform.y + platform.height + 10f
                    ) {
                        block.y = platform.y - block.height
                        block.velocityY = 0f
                        block.settled = true
                        onPlatform = true
                    }
                }

                if (!onPlatform && block.y < groundY && !block.inHole) {
                    block.settled = false
                }

                val sideTouch =
                    playerY + playerSize > block.y &&
                            playerY < block.y + block.height &&
                            (abs((playerX + playerSize) - block.x) < 10f ||
                                    abs(playerX - (block.x + block.width)) < 10f)

                if (pushing && sideTouch) {
                    if (moveRight) block.x += pushForce * delta
                    if (moveLeft) block.x -= pushForce * delta
                    block.settled = false
                }

                // ===== BLOCK ↔ BLOCK COLLISION =====
                for (other in blocks) {

                    // Skip self
                    if (other === block) continue

                    // Ignore blocks already inside holes
                    if (other.inHole || block.inHole) continue

                    // Axis overlap
                    val overlapX =
                        minOf(block.x + block.width, other.x + other.width) -
                                maxOf(block.x, other.x)

                    val overlapY =
                        minOf(block.y + block.height, other.y + other.height) -
                                maxOf(block.y, other.y)

                    // If no overlap, skip
                    if (overlapX <= 0f || overlapY <= 0f) continue

                    // ---- VERTICAL RESOLUTION (LANDING) ----
                    if (
                        block.velocityY >= 0f &&       // falling or resting
                        overlapY < overlapX &&         // vertical collision
                        block.y < other.y              // block is above other
                    ) {
                        block.y -= overlapY            // snap on top
                        block.velocityY = 0f
                        block.settled = true
                    }
                    // ---- HORIZONTAL RESOLUTION ----
                    else {
                        if (block.x < other.x) {
                            block.x -= overlapX
                        } else {
                            block.x += overlapX
                        }
                    }
                }

                val overlapX = minOf(playerX + playerSize, block.x + block.width) - maxOf(playerX, block.x)
                val overlapY = minOf(playerY + playerSize, block.y + block.height) - maxOf(playerY, block.y)

                if (overlapX > 0 && overlapY > 0 &&
                    overlapX < overlapY) {
                    if (playerX < block.x) playerX -= overlapX
                    else playerX += overlapX
                }

                block
            }

            blocks.forEach { block ->
                if (
                    velocityY > 0 &&
                    playerX + playerSize > block.x &&
                    playerX < block.x + block.width &&
                    playerY + playerSize > block.y &&
                    playerY + playerSize < block.y + block.height
                ) {
                    playerY = block.y - playerSize
                    velocityY = 0f
                }
            }

            cameraX = playerX - 200f

            if (playerY > 1500f) {
                playerX = 100f
                playerY = groundY
                velocityY = 0f
            }

            if (
                playerX + playerSize > flagX &&
                playerX < flagX + playerSize &&
                playerY + playerSize > flagY &&
                playerY < flagY + playerSize
            ) {
                flagOn = true
            }

            delay(16L)
        }
    }

    // ===== INPUT + DRAW =====
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF87CEEB))
                .pointerInteropFilter { event ->

                    var left = false
                    var right = false
                    var jump = false
                    var push = false

                    if (
                        event.actionMasked == MotionEvent.ACTION_UP ||
                        event.actionMasked == MotionEvent.ACTION_CANCEL
                    ) {
                        moveLeft = false
                        moveRight = false
                        pushing = false
                        return@pointerInteropFilter true
                    }

                    for (i in 0 until event.pointerCount) {
                        val x = event.getX(i)
                        val y = event.getY(i)

                        if (y > h * 0.65f) {
                            if (x < w * 0.25f) left = true
                            if (x in (w * 0.25f)..(w * 0.5f)) right = true
                            if (x in (w * 0.55f)..(w * 0.7f)) push = true
                            if (x > w * 0.75f) jump = true
                        }
                    }

                    moveLeft = left
                    moveRight = right
                    pushing = push

                    if (
                        (event.actionMasked == MotionEvent.ACTION_DOWN ||
                                event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) &&
                        jump &&
                        velocityY == 0f
                    ) {
                        velocityY = jumpForce
                    }

                    true
                }
        ) {

            platforms.forEach {
                drawRect(Color(0xFF6FCF97), Offset(it.x - cameraX, it.y), Size(it.width, it.height))
            }

            drawImage(if (flagOn) onFlag else offFlag,
                Offset(flagX - cameraX, flagY))

            blocks.forEach {
                drawRect(Color(0xFF8D6E63),
                    Offset(it.x - cameraX, it.y),
                    Size(it.width, it.height))
            }

            drawImage(
                playerSprite,
                Offset(playerX - cameraX,
                    playerY - spriteOffsetY))

            // ✅ BUTTONS
            drawCircle(Color(0xAA000000), 70f, Offset(w * 0.15f, h * 0.8f))
            drawCircle(Color(0xAA000000), 70f, Offset(w * 0.35f, h * 0.8f))
            drawCircle(Color(0xAA000000), 70f, Offset(w * 0.6f, h * 0.8f))
            drawCircle(Color(0xAA000000), 70f, Offset(w * 0.85f, h * 0.8f))
        }
    }
}
