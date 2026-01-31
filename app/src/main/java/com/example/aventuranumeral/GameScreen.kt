package com.example.aventuranumeral

import android.media.MediaPlayer
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlin.math.abs
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column

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
data class Coin(
    val x: Float,
    val y: Float,
    val size: Float = 50f,
    var collected: Boolean = false
)

data class NPC(
    val x: Float,
    val y: Float,
    val width: Float = 80f,
    val height: Float = 120f
)
fun blocksOverlap(a: PushBlock, b: PushBlock): Boolean {
    return a.x < b.x + b.width &&
            a.x + a.width > b.x &&
            a.y < b.y + b.height &&
            a.y + a.height > b.y
}

suspend fun sendLevelData(
    className: String,
    studentName: String,
    levelTime: Float,
    checkpointTime: Float?,
    reachedCheckpoint: Boolean
) {
    withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("https://aventuranumeralbackend.onrender.com/save-level-time")
            val conn = url.openConnection() as java.net.HttpURLConnection

            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val json = """
                {
                  "student_name": "$studentName",
                  "class_name": "$className",
                  "level_name": "level-1",
                  "time_elapsed": $levelTime,
                  "checkpoint_time": ${checkpointTime ?: "null"},
                  "reached_checkpoint": $reachedCheckpoint
                }
            """.trimIndent()

            conn.outputStream.use {
                it.write(json.toByteArray())
            }

            conn.responseCode
            conn.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// ===== GAME =====

@Composable
fun GameScreen(className: String, studentName: String, avatarSprite: String, onExitLevel: () -> Unit) {

    val context = LocalContext.current
    val coinSound = remember { MediaPlayer.create(context, R.raw.coin) }

    val playerSpriteId = when (avatarSprite) {
        "avatargirl1" -> R.drawable.avatargirl1run
        "avatargirl2" -> R.drawable.avatargirl2run
        "avatargirl3" -> R.drawable.avatargirl3run
        "avatarboy1" -> R.drawable.avatarboy1run
        "avatarboy2" -> R.drawable.avatarboy2run
        "avatarboy3" -> R.drawable.avatarboy3run
        else -> R.drawable.avatargirl1run
    }
    
    val playerSprite = ImageBitmap.imageResource(playerSpriteId)
    val offFlag = ImageBitmap.imageResource(R.drawable.offflag)
    val onFlag = ImageBitmap.imageResource(R.drawable.onflag)
    val coinImg = ImageBitmap.imageResource(R.drawable.coin)
    val npcImg = ImageBitmap.imageResource(R.drawable.npc)
    val platformImg = ImageBitmap.imageResource(R.drawable.plataforma)
    val groundBlockImg = ImageBitmap.imageResource(R.drawable.bloquecesped)
    val puenteBrokenImg = ImageBitmap.imageResource(R.drawable.puentebroken)
    val fullVidaImg = ImageBitmap.imageResource(R.drawable.fullvida)
    val oneVidaImg = ImageBitmap.imageResource(R.drawable.onevida)
    val twoVidaImg = ImageBitmap.imageResource(R.drawable.twovida)
    val zeroVidaImg = ImageBitmap.imageResource(R.drawable.zerovida)
    val coinScoreImg = ImageBitmap.imageResource(R.drawable.coinscore)
    
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
    val endX = 4000f
    val endY = 800f
    val endWidth = 100f
    val endHeight = 100f

    // ===== LIVES AND COINS =====
    var playerLives by remember { mutableIntStateOf(3) }
    var coinsCollected by remember { mutableIntStateOf(0) }

    // ===== NPC at broken bridge (hole2X) =====
    val npc = remember {
        NPC(
            x = hole2X - 200f,  // Position NPC before the second hole
            y = groundY - 200f,  // Stand on ground
            width = 150f,
            height = 200f
        )
    }

    // ===== COINS =====
    var coins by remember {
        mutableStateOf(
            listOf(
                Coin(600f, groundY - 350f),
                Coin(1000f, groundY - 500f),
                Coin(1400f, groundY - 350f),
                Coin(1800f, groundY - 500f),
                Coin(2200f, groundY - 350f),
            )
        )
    }

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
                    width = 170f,
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

    // ===== TIMER =====
    var checkpointReached by remember { mutableStateOf(false) }
    var checkpointTime by remember { mutableFloatStateOf(0f) }

    var levelCompleted by remember { mutableStateOf(false) }
    var levelTime by remember { mutableFloatStateOf(0f) }
    var timerRunning by remember { mutableStateOf(true) }
    var showEndDialog by remember { mutableStateOf(false) }
    var showGameOver by remember { mutableStateOf(false) }
    var dataSent by remember { mutableStateOf(false) }
    var starsEarned by remember { mutableIntStateOf(0) }

    // ===== GAME LOOP =====
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()

        while (true) {
            val now = System.nanoTime()
            val delta = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // ===== UPDATE TIMER =====
            if (timerRunning) {
                levelTime += delta
            }

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

            // NPC collision - block player from passing
            if (playerX + playerSize > npc.x &&
                playerX < npc.x + npc.width &&
                playerY + playerSize > npc.y &&
                playerY < npc.y + npc.height
            ) {
                // Push player back
                if (playerX < npc.x) {
                    playerX = npc.x - playerSize
                } else {
                    playerX = npc.x + npc.width
                }
            }

            // Block access to hole2X (broken bridge area)
            if (playerX + playerSize > hole2X &&
                playerX < hole2X + hole2Width &&
                playerY + playerSize >= groundY
            ) {
                // Push player back from hole2X
                if (playerX < hole2X + hole2Width / 2) {
                    playerX = hole2X - playerSize
                } else {
                    playerX = hole2X + hole2Width
                }
            }

            cameraX = playerX - 200f

            if (playerY > 1500f) {
                // Lose a life when falling
                playerLives -= 1
                
                if (playerLives <= 0) {
                    // Game over
                    showGameOver = true
                    timerRunning = false
                    moveLeft = false
                    moveRight = false
                    pushing = false
                    velocityY = 0f
                } else {
                    // Respawn at start
                    playerX = 100f
                    playerY = groundY
                    velocityY = 0f
                    flagOn = false
                }
            }

            // Coin collection
            coins = coins.map { coin ->
                if (!coin.collected &&
                    playerX + playerSize > coin.x &&
                    playerX < coin.x + coin.size &&
                    playerY + playerSize > coin.y &&
                    playerY < coin.y + coin.size
                ) {
                    coinsCollected += 1
                    // Play coin sound
                    try {
                        if (coinSound.isPlaying) {
                            coinSound.seekTo(0)
                        }
                        coinSound.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    coin.copy(collected = true)
                } else {
                    coin
                }
            }

            // Checkpoint
            if (!checkpointReached &&
                playerX + playerSize > flagX &&
                playerX < flagX + playerSize &&
                playerY + playerSize > flagY &&
                playerY < flagY + flagHeight
            ) {
                checkpointReached = true
                checkpointTime = levelTime
                flagOn = true
                println("Checkpoint reached! Time: $checkpointTime")
            }

            // Endpoint
            val playerRect = android.graphics.RectF(
                playerX,
                playerY,
                playerX + playerSize,
                playerY + playerSize
            )
            val endRect = android.graphics.RectF(
                endX,
                endY - endHeight,
                endX + endWidth,
                endY
            )

            if (!levelCompleted && RectF.intersects(playerRect, endRect)) {
                levelCompleted = true
                showEndDialog = true
                timerRunning = false
                moveLeft = false
                moveRight = false
                pushing = false
                velocityY = 0f
                
                // Calculate stars based on time
                starsEarned = when {
                    levelTime < 15f -> 3  // Very fast: 3 stars
                    levelTime < 30f -> 2  // Good: 2 stars
                    else -> 1             // Completed: 1 star
                }
            }

            delay(16L)
        }
    }

    LaunchedEffect(levelCompleted) {
        if (levelCompleted && !dataSent) {
            dataSent = true
            sendLevelData(
                className = className,
                studentName = studentName,
                levelTime = levelTime,
                checkpointTime = if (checkpointReached) checkpointTime else null,
                reachedCheckpoint = checkpointReached
            )
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

            // Draw platforms
            platforms.forEach { platform ->
                // Ground platforms (thick ones) use green color
                if (platform.height > 50f) {
                    drawRect(
                        Color(0xFF6FCF97),
                        Offset(platform.x - cameraX, platform.y),
                        Size(platform.width, platform.height)
                    )
                } else {
                    // Floating platforms use platform image
                    drawImage(
                        platformImg,
                        dstOffset = IntOffset((platform.x - cameraX).toInt(), platform.y.toInt()),
                        dstSize = IntSize(platform.width.toInt(), platform.height.toInt())
                    )
                }
            }

            // Draw coins
            coins.forEach { coin ->
                if (!coin.collected) {
                    drawImage(
                        coinImg,
                        dstOffset = IntOffset((coin.x - cameraX).toInt(), coin.y.toInt()),
                        dstSize = IntSize(coin.size.toInt(), coin.size.toInt())
                    )
                }
            }

            // Draw NPC at broken bridge
            drawImage(
                npcImg,
                dstOffset = IntOffset((npc.x - cameraX).toInt(), npc.y.toInt()),
                dstSize = IntSize(npc.width.toInt(), npc.height.toInt())
            )

            // Draw broken bridge at hole2X
            drawImage(
                puenteBrokenImg,
                dstOffset = IntOffset((hole2X - cameraX).toInt(), (groundY + 10f).toInt()),
                dstSize = IntSize(hole2Width.toInt(), 80)
            )

            drawImage(if (flagOn) onFlag else offFlag,
                Offset(flagX - cameraX, flagY))

            blocks.forEach {
                drawImage(
                    groundBlockImg,
                    dstOffset = IntOffset((it.x - cameraX).toInt(), it.y.toInt()),
                    dstSize = IntSize(it.width.toInt(), it.height.toInt())
                )
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

            drawRect(
                Color.Magenta,
                Offset(endX - cameraX, endY - endHeight),
                Size(endWidth, endHeight)
            )

            // Draw lives at top-left (use appropriate image based on lives)
            val vidaImg = when (playerLives) {
                3 -> fullVidaImg
                2 -> twoVidaImg
                1 -> oneVidaImg
                else -> zeroVidaImg
            }
            drawImage(
                vidaImg,
                dstOffset = IntOffset(50, 50),
                dstSize = IntSize(150, 50)
            )

            // Draw coins collected at top-right
            drawImage(
                coinScoreImg,
                dstOffset = IntOffset((w - 250f).toInt(), 50),
                dstSize = IntSize(50, 50)
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 50f
                    textAlign = android.graphics.Paint.Align.LEFT
                }

                // Display coins count
                paint.textAlign = android.graphics.Paint.Align.LEFT
                drawText("x$coinsCollected", w - 180f, 90f, paint)

                // Time display
                drawText("Time: ${"%.2f".format(levelTime)} s", 50f, 150f, paint)

                if (checkpointReached) {
                    drawText("Checkpoint: ${"%.2f".format(checkpointTime)} s", 50f, 210f, paint)
                }
            }
        }
    }
    if (showEndDialog) {
        FinishScreen(
            studentName = studentName,
            levelTime = levelTime,
            coinsCollected = coinsCollected,
            starsEarned = starsEarned,
            checkpointReached = checkpointReached,
            checkpointTime = checkpointTime,
            onBackToStart = onExitLevel
        )
    }
    
    if (showGameOver) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = {
                    showGameOver = false
                    onExitLevel()
                }) {
                    Text("Volver al Inicio")
                }
            },
            title = {
                Text("💀 Game Over")
            },
            text = {
                Column {
                    Text("Te quedaste sin vidas!")
                    Text("Jugador: $studentName")
                    Text("Monedas recolectadas: $coinsCollected")
                }
            }
        )
    }
}