package com.example.aventuranumeral

import android.media.MediaPlayer
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size

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
    var width: Float,
    val height: Float,
    var velocityY: Float = 0f,
    var inHole: Boolean = false,
    var settled: Boolean = false,
    var isFraction: Boolean = false,
    var fractionValue: String = ""  // e.g., "1/4", "1/2", "1/3"
)
data class Coin(
    val x: Float,
    val y: Float,
    val size: Float = 50f,
    var collected: Boolean = false
)

data class NPC(
    val x: Float,
    var y: Float,
    val width: Float = 241f,
    val height: Float = 183f
)
fun blocksOverlap(a: PushBlock, b: PushBlock): Boolean {
    return a.x < b.x + b.width &&
            a.x + a.width > b.x &&
            a.y < b.y + b.height &&
            a.y + a.height > b.y
}

fun addFractions(frac1: String, frac2: String): String {
    // Suma simple: mismo denominador, solo sumar numeradores
    try {
        val parts1 = frac1.split("/")
        val parts2 = frac2.split("/")
        
        val num1 = parts1[0].toInt()
        val den1 = parts1[1].toInt()
        val num2 = parts2[0].toInt()
        val den2 = parts2[1].toInt()
        
        // Verificar que tienen el mismo denominador
        if (den1 != den2) {
            return "?" // Error: denominadores diferentes
        }
        
        val resultNum = num1 + num2
        
        return "$resultNum/$den1"
    } catch (e: Exception) {
        return "?"
    }
}

suspend fun sendLevelData(
    className: String,
    studentName: String,
    levelTime: Float,
    checkpointTime: Float?,
    reachedCheckpoint: Boolean,
    coinsCollected: Int = 0,
    starsEarned: Int = 0
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
                  "reached_checkpoint": $reachedCheckpoint,
                  "coins_collected": $coinsCollected,
                  "stars_earned": $starsEarned
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
@Suppress("UNUSED_PARAMETER")
fun GameScreen(className: String, studentName: String, avatarSprite: String, onExitLevel: () -> Unit, onLevelComplete: (coins: Int, stars: Int, time: Float, checkpointReached: Boolean, checkpointTime: Float?) -> Unit = { _, _, _, _, _ -> }) {

    val context = LocalContext.current
    val coinSound = remember { MediaPlayer.create(context, R.raw.coin) }
    val construSound = remember { MediaPlayer.create(context, R.raw.constru) }
    val martilloSound = remember { MediaPlayer.create(context, R.raw.martillo) }

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
    val npcv2Img = ImageBitmap.imageResource(R.drawable.npcv2)
    val platformImg = ImageBitmap.imageResource(R.drawable.plataforma)
    val groundBlockImg = ImageBitmap.imageResource(R.drawable.bloquecesped)
    val groundBlockDosImg = ImageBitmap.imageResource(R.drawable.bloquecespeddos)
    val bloqueSumaUnoImg = ImageBitmap.imageResource(R.drawable.bloquesumauno)
    val bloqueSumaDosImg = ImageBitmap.imageResource(R.drawable.bloquesumados)
    val bloqueHalfImg = ImageBitmap.imageResource(R.drawable.bloque)
    val bloqueResultImg = ImageBitmap.imageResource(R.drawable.bloqueresult)
    val puenteBrokenImg = ImageBitmap.imageResource(R.drawable.puentebroken)
    val puenteFixedImg = ImageBitmap.imageResource(R.drawable.puentefixed)
    val fullVidaImg = ImageBitmap.imageResource(R.drawable.fullvida)
    val oneVidaImg = ImageBitmap.imageResource(R.drawable.onevida)
    val twoVidaImg = ImageBitmap.imageResource(R.drawable.twovida)
    val zeroVidaImg = ImageBitmap.imageResource(R.drawable.zerovida)
    val coinScoreImg = ImageBitmap.imageResource(R.drawable.coinscore)
    val dialogoImg = ImageBitmap.imageResource(R.drawable.dialogo)
    val textboxImg = ImageBitmap.imageResource(R.drawable.textbox)
    
    var flagOn by remember { mutableStateOf(false) }

    // ===== WORLD =====
    val groundY = 700f
    val playerSize = 80f
    val spriteHeight = 200f
    val spriteOffsetY = spriteHeight - playerSize

    val holeX = 1200f
    val holeWidth = 400f
    val hole2X = 3800f
    val hole2Width = 500f  // Puente más grande

    val flagX = holeX + holeWidth + 150f
    val flagHeight = 200f
    val floorTopY = groundY + playerSize
    val flagY = floorTopY - flagHeight

    val endX = hole2X + hole2Width + 100f

    // ===== LIVES AND COINS =====
    var playerLives by remember { mutableIntStateOf(3) }
    var coinsCollected by remember { mutableIntStateOf(0) }

    // ===== TUTORIAL AND NPC MECHANICS =====
    var showTutorial by remember { mutableStateOf(true) }
    var tutorialStep by remember { mutableIntStateOf(0) }
    
    // ===== NPC AND BRIDGE MECHANICS =====
    var npcState by remember { mutableStateOf("idle") } // idle, talking, working, finished, gone
    var showNpcDialog by remember { mutableStateOf(false) }
    var npcDialogMessage by remember { mutableStateOf("") }
    var bridgeRepaired by remember { mutableStateOf(false) }
    var constructionTimer by remember { mutableFloatStateOf(0f) }
    val constructionDuration = 5f  // 5 seconds
    var hasAddMode by remember { mutableStateOf(false) }  // sum mode
    var sumMode by remember { mutableStateOf(false) }  // sum mode activated
    val requiredFraction = "3/4"  // answer frac

    // ===== FIXED SCREEN STATE =====
    var showFixedScreen by remember { mutableStateOf(false) }
    var fixedScreenType by remember { mutableStateOf("") }
    var fixedScreenDetail by remember { mutableStateOf("") }

    // ===== NPC at broken bridge (hole2X) =====
    val npc = remember {
        NPC(
            x = hole2X - 300f,
            y = floorTopY - 220f  // Ajuste base, se recalcula al dibujar
        )
    }

    // ===== COINS =====
    var coins by remember {
        mutableStateOf(
            listOf(
                Coin(400f, floorTopY - 50f, 50f),
                Coin(2200f, floorTopY - 50f, 50f),
                Coin(3200f, floorTopY - 50f, 50f),
            )
        )
    }

    // Moneda especial de fin de nivel
    var endCoinCollected by remember { mutableStateOf(false) }

    // Pared invisible antes del puente (después del NPC)
    val wallX = hole2X - 50f

    val platforms = listOf(
        // Ground platforms
        Platform(0f, groundY + playerSize, holeX, 100f),
        // Sección 2 floor - desde agujero hasta puente (largo)
        Platform(holeX + holeWidth, groundY + playerSize, hole2X - (holeX + holeWidth), 100f),
        // Floor después del puente
        Platform(hole2X + hole2Width, groundY + playerSize, 1500f, 100f),

        // Section 1: Escalón + 2 plataformas altas (lejos del agujero)
        Platform(400f, groundY - 120f, 200f, 30f),       // Escalón bajo para subir
        Platform(150f, groundY - 280f, 250f, 30f),       // Plataforma alta izq (bloque 2/4)
        Platform(700f, groundY - 280f, 250f, 30f),       // Plataforma alta der (bloque 2/5)

        // Section 2: Escalón + plataformas amplias para bloques de suma
        // Flag está en ~1750. Las plataformas empiezan después (~2200+)
        Platform(2200f, groundY - 120f, 200f, 30f),       // Escalón para saltar
        Platform(2000f, groundY - 260f, 250f, 30f),        // Plataforma bloque 2/4
        Platform(2450f, groundY - 350f, 250f, 30f),        // Plataforma bloque 1/4 (alta)
        Platform(2850f, groundY - 260f, 250f, 30f),        // Plataforma bloque 1/2
    )

    // ===== BLOCKS =====
    var blocks by remember {
        mutableStateOf(
            listOf(
                // ===== SECTION 1: Bloques de fracción para el agujero =====
                // Bloque correcto 2/4 (en plataforma alta izquierda)
                PushBlock(
                    x = 185f,
                    y = groundY - 280f - playerSize,
                    width = 180f,
                    height = playerSize,
                    velocityY = 0f,
                    inHole = false,
                    settled = true,
                    isFraction = true,
                    fractionValue = "2/4"
                ),
                // Bloque incorrecto 2/5 (en plataforma alta derecha)
                PushBlock(
                    x = 735f,
                    y = groundY - 280f - playerSize,
                    width = 180f,
                    height = playerSize,
                    velocityY = 0f,
                    inHole = false,
                    settled = true,
                    isFraction = true,
                    fractionValue = "2/5"
                ),

                // ===== SECTION 2: Bloques para suma de fracciones =====
                // Bloque 2/4 (parte de la respuesta correcta)
                PushBlock(
                    x = 2020f,
                    y = groundY - 260f - playerSize,
                    width = playerSize,
                    height = playerSize,
                    velocityY = 0f,
                    inHole = false,
                    settled = true,
                    isFraction = true,
                    fractionValue = "2/4"
                ),
                // Bloque 1/4 (parte de la respuesta correcta)
                // 2/4 + 1/4 = 3/4 ✅
                PushBlock(
                    x = 2470f,
                    y = groundY - 350f - playerSize,
                    width = playerSize,
                    height = playerSize,
                    velocityY = 0f,
                    inHole = false,
                    settled = true,
                    isFraction = true,
                    fractionValue = "1/4"
                ),
                // Bloque 1/2 (DISTRACTOR)
                PushBlock(
                    x = 2870f,
                    y = groundY - 260f - playerSize,
                    width = playerSize,
                    height = playerSize,
                    velocityY = 0f,
                    inHole = false,
                    settled = true,
                    isFraction = true,
                    fractionValue = "1/2"
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
    var timerRunning by remember { mutableStateOf(false) }  // Iniciar parado hasta terminar tutorial
    var showGameOver by remember { mutableStateOf(false) }
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
                    block.width = holeWidth  // Expandir para cubrir el agujero
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
            
            // Check if wrong fraction block fell into hole (Section 1)
            if (!showFixedScreen) {
                val wrongBlock = blocks.find { it.inHole && it.isFraction && it.fractionValue != "2/5" }
                if (wrongBlock != null) {
                    showFixedScreen = true
                    fixedScreenType = "wrong_block"
                    fixedScreenDetail = "Pusiste el bloque ${wrongBlock.fractionValue} en el agujero, pero el correcto era 2/5."
                    timerRunning = false
                    moveLeft = false
                    moveRight = false
                    pushing = false
                    velocityY = 0f
                }
            }

            // FRAC SUM - Buscar dos bloques de fracción que estén tocándose
            if (sumMode && pushing) {
                var mergedPair: Pair<PushBlock, PushBlock>? = null
                
                // Buscar todos los pares de bloques de fracción que estén tocándose
                for (i in blocks.indices) {
                    val block1 = blocks[i]
                    if (!block1.isFraction || block1.fractionValue.isEmpty()) continue
                    
                    // Ver si el jugador está cerca de este bloque
                    val playerNearBlock1 = 
                        playerY + playerSize > block1.y - 50f &&
                        playerY < block1.y + block1.height + 50f &&
                        playerX + playerSize > block1.x - 50f &&
                        playerX < block1.x + block1.width + 50f
                    
                    if (!playerNearBlock1) continue
                    
                    for (j in i + 1 until blocks.size) {
                        val block2 = blocks[j]
                        if (!block2.isFraction || block2.fractionValue.isEmpty()) continue
                        
                        // Ver si block2 está tocando a block1
                        val touching = 
                            block1.x < block2.x + block2.width + 10f &&
                            block1.x + block1.width > block2.x - 10f &&
                            abs(block1.y - block2.y) < 30f
                        
                        if (touching) {
                            mergedPair = Pair(block1, block2)
                            break
                        }
                    }
                    
                    if (mergedPair != null) break
                }
                
                // Si encontramos un par, verificar la suma
                if (mergedPair != null) {
                    val (block1, block2) = mergedPair
                    val newFraction = addFractions(block1.fractionValue, block2.fractionValue)

                    if (newFraction != requiredFraction) {
                        // Suma incorrecta - mostrar FixedScreen
                        showFixedScreen = true
                        fixedScreenType = "wrong_sum"
                        fixedScreenDetail = "${block1.fractionValue} + ${block2.fractionValue} = $newFraction, pero se necesitaba $requiredFraction"
                        timerRunning = false
                        moveLeft = false
                        moveRight = false
                        pushing = false
                        velocityY = 0f
                        blocks = blocks.filter { it != block1 && it != block2 }
                        sumMode = false
                    } else {
                        // Suma correcta - fusionar bloques
                        val newX = (block1.x + block2.x) / 2
                        val newY = minOf(block1.y, block2.y)

                        val mergedBlock = PushBlock(
                            x = newX,
                            y = newY,
                            width = playerSize,
                            height = playerSize,
                            velocityY = 0f,
                            inHole = false,
                            settled = true,
                            isFraction = true,
                            fractionValue = newFraction
                        )

                        blocks = blocks.filter { it != block1 && it != block2 } + mergedBlock
                        sumMode = false
                    }
                }
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

            val currentNpcWidth = if (npcState == "working") 340f else 320f
            val currentNpcHeight = if (npcState == "working") 240f else 220f
            val currentNpcY = floorTopY - currentNpcHeight

            // NPC collision - block player from passing
            if (playerX + playerSize > npc.x &&
                playerX < npc.x + currentNpcWidth &&
                playerY + playerSize > currentNpcY &&
                playerY < currentNpcY + currentNpcHeight &&
                npcState != "gone"
            ) {
                // Always push player LEFT if bridge not repaired
                playerX = if (!bridgeRepaired || playerX < npc.x) {
                    npc.x - playerSize
                } else {
                    npc.x + currentNpcWidth
                }
            }

            // Pared invisible - impide pasar el área del puente completamente
            if (!bridgeRepaired && playerX + playerSize > wallX) {
                playerX = wallX - playerSize
            }

            // Block access to hole2X (broken bridge area) - SOLO si el puente NO está reparado
            if (!bridgeRepaired &&
                playerX + playerSize > hole2X &&
                playerX < hole2X + hole2Width &&
                playerY + playerSize >= floorTopY - 10f
            ) {
                playerX = if (playerX < hole2X + hole2Width / 2) {
                    hole2X - playerSize
                } else {
                    hole2X + hole2Width
                }
            }
            
            // Si el puente está reparado, el jugador puede caminar sobre él
            if (bridgeRepaired &&
                playerX + playerSize > hole2X &&
                playerX < hole2X + hole2Width
            ) {
                // Bridge surface at ground level for seamless walking
                if (velocityY >= 0f &&
                    playerY + playerSize >= floorTopY - 5f &&
                    playerY + playerSize <= floorTopY + 50f
                ) {
                    playerY = floorTopY - playerSize  // Same as groundY
                    velocityY = 0f
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
                    // Respawn logic
                    if (checkpointReached) {
                        // Si había llegado al checkpoint, respawn ahí y mantener progreso
                        playerX = flagX
                        playerY = groundY
                        flagOn = true  // Mantener bandera ON
                    } else {
                        // Si no había llegado al checkpoint, reiniciar todo
                        playerX = 100f
                        playerY = groundY
                        flagOn = false
                        // Reiniciar monedas recolectadas
                        coinsCollected = 0
                        // Resetear monedas del nivel
                        coins = coins.map { it.copy(collected = false) }
                    }
                    velocityY = 0f
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

            // ===== NPC INTERACTION LOGIC =====
            // Área de interacción más grande
            val interactionRange = 200f
            val playerNearNPC = playerX + playerSize > npc.x - interactionRange &&
                                playerX < npc.x + currentNpcWidth + interactionRange &&
                                playerY + playerSize > currentNpcY - interactionRange &&
                                playerY < currentNpcY + currentNpcHeight + interactionRange
            
            if (playerNearNPC && npcState == "idle") {
                // Primera interacción: NPC explica el problema
                npcState = "talking"
                showNpcDialog = true
                npcDialogMessage = "¡Hola! El puente está roto, así que no puedes pasar. Quisiera repararlo pero mi equipo se ha quedado sin madera, necesito $requiredFraction de madera para repararlo. Hay bloques de madera más atrás, pero para unirlos necesitarás esto..."
            }
            
            // Segunda interacción: Da el sum mode y explica cómo usarlo
            if (playerNearNPC && npcState == "talking" && !showNpcDialog && !hasAddMode) {
                hasAddMode = true
                showNpcDialog = true
                npcDialogMessage = "Con esto podrás unir dos bloques de fracciones para hacer uno más grande. Solo actívalo y junta dos bloques. ¡Ahora ve y tráeme madera!"
            }
            
            // Cuando el jugador trae el bloque correcto al NPC
            if (playerNearNPC && npcState == "talking" && !showNpcDialog) {
                // Verificar si hay un bloque con la fracción correcta cerca
                val correctBlock = blocks.find { block ->
                    block.isFraction && 
                    block.fractionValue == requiredFraction &&
                    block.x + block.width > npc.x - 100f &&
                    block.x < npc.x + currentNpcWidth + 100f
                }
                
                if (correctBlock != null) {
                    // ¡Bloque correcto entregado!
                    npcState = "working"
                    constructionTimer = 0f
                    showNpcDialog = true
                    npcDialogMessage = "¡Gracias! Ahora mi equipo y yo podemos empezar a trabajar."
                    
                    // Remover el bloque used
                    blocks = blocks.filter { it != correctBlock }
                    
                    // Reproducir sonido de construcción
                    try {
                        construSound.isLooping = true
                        construSound.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Timer de construcción
            if (npcState == "working") {
                constructionTimer += delta
                
                if (constructionTimer >= constructionDuration) {
                    // Construcción completada
                    npcState = "finished"
                    bridgeRepaired = true
                    showNpcDialog = true
                    npcDialogMessage = "¡Ya está! El puente está reparado, ahora puedes cruzar. ¡Buenos viajes!"
                    
                    // Detener sonidos
                    try {
                        construSound.stop()
                        martilloSound.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Después de un tiempo, el NPC desaparece
            if (npcState == "finished" && !showNpcDialog) {
                npcState = "gone"
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

            // Endpoint - moneda especial de fin de nivel
            val endCoinX = endX + 300f
            val endCoinY = groundY - 50f
            val endCoinSize = 80f

            if (!endCoinCollected && !levelCompleted && bridgeRepaired &&
                playerX + playerSize > endCoinX &&
                playerX < endCoinX + endCoinSize &&
                playerY + playerSize > endCoinY &&
                playerY < endCoinY + endCoinSize
            ) {
                endCoinCollected = true
                coinsCollected += 1  // Moneda del final vale 1
                try {
                    if (coinSound.isPlaying) coinSound.seekTo(0)
                    coinSound.start()
                } catch (e: Exception) { e.printStackTrace() }
            }

            if (!levelCompleted && endCoinCollected) {
                levelCompleted = true
                timerRunning = false
                moveLeft = false
                moveRight = false
                pushing = false
                velocityY = 0f
                
                // Calculate stars based on time AND lives remaining
                starsEarned = when {
                    levelTime < 45f && playerLives == 3 -> 3  // Perfect: <45s + 3 lives
                    levelTime < 60f && playerLives >= 2 -> 2  // Good: <60s + 2+ lives  
                    levelTime < 75f && playerLives >= 1 -> 1  // Completed: <75s + 1+ life
                    playerLives == 2 -> 2                     // 2 lives regardless of time
                    playerLives == 1 -> 1                     // 1 life = always 1 star
                    else -> 1                                  // Default 1 star
                }
                
                // Navigate to finish screen with level data
                onLevelComplete(coinsCollected, starsEarned, levelTime, checkpointReached, if (checkpointReached) checkpointTime else null)
            }

            delay(16L)
        }
    }

    // Data sending is now handled by MainActivity's onLevelComplete callback

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
                    var sumButton = false

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

                        // Control buttons area
                        if (y > h * 0.75f) {
                            if (x < w * 0.25f) left = true
                            if (x in (w * 0.25f)..(w * 0.5f)) right = true
                            if (x in (w * 0.55f)..(w * 0.7f)) push = true
                            if (x > w * 0.75f) jump = true
                        }
                        
                        // Sum mode button
                        if (hasAddMode && y in (h * 0.70f)..(h * 0.80f) && 
                            x in (w * 0.45f)..(w * 0.55f)) {
                            sumButton = true
                        }
                        
                        // Tutorial click area (anywhere on textbox)
                        if (showTutorial && y in (h * 0.25f)..(h * 0.75f) && 
                            x in (w * 0.25f)..(w * 0.75f)) {
                            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                                if (tutorialStep < 4) {
                                    tutorialStep++
                                } else {
                                    showTutorial = false
                                    timerRunning = true
                                }
                            }
                        }
                    }
                    
                    // Toggle sum mode
                    if (event.actionMasked == MotionEvent.ACTION_DOWN && sumButton) {
                        sumMode = !sumMode
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

            // Draw NPC at broken bridge (solo si no se ha ido)
            if (npcState != "gone") {
                val currentNpcImg = if (npcState == "working") npcv2Img else npcImg
                val npcWidth = if (npcState == "working") 340f else 320f
                val npcHeight = if (npcState == "working") 240f else 220f
                // NPC debe tocar el suelo
                val npcDrawY = floorTopY - npcHeight
                drawImage(
                    currentNpcImg,
                    dstOffset = IntOffset((npc.x - cameraX).toInt(), npcDrawY.toInt()),
                    dstSize = IntSize(npcWidth.toInt(), npcHeight.toInt())
                )
                
                // Dibujar nube de trabajo si está trabajando
                if (npcState == "working") {
                    // Nube de trabajo encima del puente
                    val cloudX = hole2X + hole2Width / 2 - cameraX
                    val cloudY = groundY - 100f
                    val cloudSize = 80f + (constructionTimer * 20f % 20f)
                    
                    drawCircle(
                        Color.White.copy(alpha = 0.7f),
                        cloudSize,
                        Offset(cloudX, cloudY)
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.5f),
                        cloudSize - 20f,
                        Offset(cloudX - 40f, cloudY + 20f)
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.5f),
                        cloudSize - 20f,
                        Offset(cloudX + 40f, cloudY + 20f)
                    )
                }
            }

            // Draw bridge at hole2X - elevated above ground
            val bridgeImg = if (bridgeRepaired) puenteFixedImg else puenteBrokenImg
            drawImage(
                bridgeImg,
                dstOffset = IntOffset((hole2X - cameraX).toInt(), (groundY - 50f).toInt()),
                dstSize = IntSize(hole2Width.toInt(), 150)
            )

            drawImage(if (flagOn) onFlag else offFlag,
                Offset(flagX - cameraX, flagY))

            // Moneda especial de fin de nivel (grande y dorada)
            if (!endCoinCollected && bridgeRepaired) {
                val endCoinX = endX + 300f
                val endCoinY = groundY - 50f
                drawImage(
                    coinImg,
                    dstOffset = IntOffset((endCoinX - cameraX).toInt(), endCoinY.toInt()),
                    dstSize = IntSize(80, 80)
                )
            }

            // Draw blocks con imagenes específicas por fracción (sin texto)
            blocks.forEach { block ->
                val blockImg = when (block.fractionValue) {
                    "2/5" -> groundBlockDosImg     // bloquecespeddos
                    "1/4" -> bloqueSumaDosImg      // bloquesumados
                    "1/2" -> bloqueHalfImg          // bloque (distractor)
                    "3/4" -> bloqueResultImg        // bloqueresult (resultado de suma)
                    "2/4" -> if (block.x >= 1400f) bloqueSumaUnoImg else groundBlockImg
                    else -> groundBlockImg
                }
                drawImage(
                    blockImg,
                    dstOffset = IntOffset((block.x - cameraX).toInt(), block.y.toInt()),
                    dstSize = IntSize(block.width.toInt(), block.height.toInt())
                )
            }

            drawImage(
                playerSprite,
                Offset(playerX - cameraX,
                    playerY - spriteOffsetY))

            // ✅ BUTTONS (control buttons at bottom)
            drawCircle(Color(0xAA000000), 40f, Offset(w * 0.15f, h * 0.85f))
            drawCircle(Color(0xAA000000), 40f, Offset(w * 0.35f, h * 0.85f))
            drawCircle(Color(0xAA000000), 40f, Offset(w * 0.6f, h * 0.85f))
            drawCircle(Color(0xAA000000), 40f, Offset(w * 0.85f, h * 0.85f))
            
            // Botón de sum mode (solo si tiene la habilidad)
            if (hasAddMode) {
                val sumButtonColor = if (sumMode) Color(0xAAFFD700) else Color(0xAA4CAF50)
                drawCircle(sumButtonColor, 40f, Offset(w * 0.5f, h * 0.75f))
                // Draw "+" using two lines instead of nativeCanvas (which renders white on API 29)
                val plusCenterX = w * 0.5f
                val plusCenterY = h * 0.75f
                val plusLen = 18f
                drawLine(Color.Black, Offset(plusCenterX - plusLen, plusCenterY), Offset(plusCenterX + plusLen, plusCenterY), strokeWidth = 5f)
                drawLine(Color.Black, Offset(plusCenterX, plusCenterY - plusLen), Offset(plusCenterX, plusCenterY + plusLen), strokeWidth = 5f)
            }

            // Draw lives at top-left (BIGGER SIZE)
            val vidaImg = when (playerLives) {
                3 -> fullVidaImg
                2 -> twoVidaImg
                1 -> oneVidaImg
                else -> zeroVidaImg
            }
            drawImage(
                vidaImg,
                dstOffset = IntOffset(50, 50),
                dstSize = IntSize(200, 70)
            )

            // Draw coins collected at top-right (504x250 original, scaled down)
            val coinScoreX = w - 280f
            val coinScoreY = 40f
            drawImage(
                coinScoreImg,
                dstOffset = IntOffset(coinScoreX.toInt(), coinScoreY.toInt()),
                dstSize = IntSize(252, 125)
            )

            // Textos del HUD se dibujan como Compose overlays (después del Canvas)
            
            // Diálogo del NPC - solo la imagen, el texto se dibuja como Compose overlay
            if (showNpcDialog && npcDialogMessage.isNotEmpty()) {
                val dialogWidth = 520f
                val dialogHeight = 320f
                val npcHeightForDialog = if (npcState == "working") 240f else 220f
                val npcDrawYForDialog = floorTopY - npcHeightForDialog
                val rawDialogX = npc.x - cameraX - 180f
                val dialogX = rawDialogX.coerceIn(20f, w - dialogWidth - 20f)
                val dialogY = (npcDrawYForDialog - dialogHeight - 20f).coerceAtLeast(40f)
                
                drawImage(
                    dialogoImg,
                    dstOffset = IntOffset(dialogX.toInt(), dialogY.toInt()),
                    dstSize = IntSize(dialogWidth.toInt(), dialogHeight.toInt())
                )
            }
            
            // Tutorial - solo la imagen, el texto se dibuja como Compose overlay
            if (showTutorial) {
                val tutorialWidth = minOf(820f, w - 80f)
                val tutorialHeight = minOf(520f, h - 120f)
                val tutorialX = (w - tutorialWidth) / 2
                val tutorialY = (h - tutorialHeight) / 2
                
                drawImage(
                    textboxImg,
                    dstOffset = IntOffset(tutorialX.toInt(), tutorialY.toInt()),
                    dstSize = IntSize(tutorialWidth.toInt(), tutorialHeight.toInt())
                )
            }
        }
        
        // ===== COMPOSE TEXT OVERLAYS =====
        
        // Timer oculto (se usa internamente para estrellas pero no se muestra)
        
        val density = LocalDensity.current
        
        // Contador de monedas - DENTRO de la imagen coinScore
        // coinScore image is at pixel (w-280, 40) with size (252, 125)
        with(density) {
            Box(
                modifier = Modifier
                    .offset(x = (w - 280f + 50f).toDp(), y = 40f.toDp())
                    .size(width = 200f.toDp(), height = 125f.toDp()),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "x$coinsCollected",
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        
        // Tutorial overlay - texto centrado DENTRO de la imagen textbox
        if (showTutorial) {
            val tutorialMessages = listOf(
                "¡Hola! Bienvenido a Aventura Numeral. Aquí aprenderás fracciones jugando.",
                "Las fracciones son bloques divididos en partes iguales. Las partes coloreadas son el numerador, el total de partes es el denominador.",
                "Arriba hay dos bloques: uno de 2/4 y otro de 2/5. Solo uno es correcto para rellenar el agujero.",
                "El agujero necesita el bloque de 2/5. ¡Elige bien! Si te equivocas, tendrás que volver a intentar.",
                "¡Empuja los bloques desde las plataformas hacia el agujero! ¡Buena suerte!"
            )
            // Use exact same coordinates as the Canvas textbox image
            val tutorialWidth = minOf(820f, w - 80f)
            val tutorialHeight = minOf(520f, h - 120f)
            val tutorialX = (w - tutorialWidth) / 2
            val tutorialY = (h - tutorialHeight) / 2
            
            with(density) {
                Box(
                    modifier = Modifier
                        .offset(x = tutorialX.toDp(), y = tutorialY.toDp())
                        .size(width = tutorialWidth.toDp(), height = tutorialHeight.toDp())
                        .clickable {
                            if (tutorialStep < 4) tutorialStep++ else { showTutorial = false; timerRunning = true }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 50.dp, vertical = 40.dp)
                    ) {
                        Text(
                            text = tutorialMessages[tutorialStep],
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (tutorialStep < 4) "➤ Siguiente" else "\uD83D\uDE80 ¡Empezar!",
                            color = Color(0xFFFF6B35),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Diálogo del NPC overlay - texto centrado DENTRO de la imagen dialogo
        if (showNpcDialog && npcDialogMessage.isNotEmpty()) {
            val dialogWidth = 520f
            val dialogHeight = 320f
            val npcHeightForDialog = if (npcState == "working") 240f else 220f
            val npcDrawYForDialog = floorTopY - npcHeightForDialog
            val rawDialogX = npc.x - cameraX - 180f
            val dialogX = rawDialogX.coerceIn(20f, w - dialogWidth - 20f)
            val dialogY = (npcDrawYForDialog - dialogHeight - 20f).coerceAtLeast(40f)
            
            with(density) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showNpcDialog = false }
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = dialogX.toDp(), y = dialogY.toDp())
                            .size(width = dialogWidth.toDp(), height = dialogHeight.toDp())
                            .padding(start = 40.dp, end = 40.dp, top = 30.dp, bottom = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = npcDialogMessage,
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else if (showNpcDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showNpcDialog = false }
            )
        }
    } // Cierra BoxWithConstraints
    
    // Estos composables van DENTRO de GameScreen pero FUERA de BoxWithConstraints
    // FinishScreen is now handled by MainActivity
    
    if (showFixedScreen) {
        FixedScreen(
            errorType = fixedScreenType,
            errorDetail = fixedScreenDetail,
            onRetry = {
                showFixedScreen = false
                onExitLevel()
            }
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
} // Cierra GameScreen