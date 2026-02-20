package com.example.aventuranumeral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.aventuranumeral.ui.theme.AventuraNumeralTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject

suspend fun getStudentAvatar(classId: Int, studentName: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("https://aventuranumeralbackend.onrender.com/classes/$classId/student-names")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val studentsArray = json.getJSONArray("students")

            for (i in 0 until studentsArray.length()) {
                val student = studentsArray.getJSONObject(i)
                if (student.getString("student_name") == studentName) {
                    val avatar = if (student.isNull("avatar")) "" else student.getString("avatar")
                    return@withContext avatar
                }
            }
            ""  // Return empty string if student not found
        } catch (e: Exception) {
            e.printStackTrace()
            ""  // Return empty string on error
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AventuraNumeralTheme {
                var currentScreen by remember { mutableStateOf("start") }
                var className by remember { mutableStateOf("") }
                var studentName by remember { mutableStateOf("") }
                var classId by remember { mutableIntStateOf(0) }
                var selectedAvatar by remember { mutableStateOf("avatargirl1") }
                var isLoadingAvatar by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                var totalCoins by remember { mutableIntStateOf(0) }
                var ownedAvatars by remember { mutableStateOf(setOf<String>()) }

                // Saved finish screen data
                var lastCoins by remember { mutableIntStateOf(0) }
                var lastStars by remember { mutableIntStateOf(0) }
                var lastTime by remember { mutableFloatStateOf(0f) }
                var lastCheckpoint by remember { mutableStateOf(false) }
                var lastCheckpointTime by remember { mutableStateOf<Float?>(null) }

                when (currentScreen) {
                    "start" -> {
                        StartScreen(
                            onStartGame = { cls, name, clsId ->
                                className = cls
                                studentName = name
                                classId = clsId
                                
                                // Check if student has avatar
                                isLoadingAvatar = true
                                coroutineScope.launch {
                                    val avatar = getStudentAvatar(clsId, name)
                                    isLoadingAvatar = false
                                    
                                    if (avatar.isEmpty() || avatar == "null") {
                                        // Student has no avatar - send to avatar selection
                                        currentScreen = "avatar"
                                    } else {
                                        // Student has avatar - go to game
                                        selectedAvatar = avatar
                                        ownedAvatars = setOf(avatar)
                                        currentScreen = "game"
                                    }
                                }
                            }
                        )
                    }
                    "avatar" -> {
                        AvatarScreen(
                            classId = classId,
                            studentName = studentName,
                            onAvatarSelected = { avatar ->
                                selectedAvatar = avatar
                                ownedAvatars = setOf(avatar)
                                currentScreen = "game"
                            },
                            onBack = {
                                currentScreen = "start"
                            }
                        )
                    }
                    "game" -> {
                        GameScreen(
                            className = className,
                            studentName = studentName,
                            avatarSprite = selectedAvatar,
                            onExitLevel = {
                                currentScreen = "start"
                                className = ""
                                studentName = ""
                                classId = 0
                                selectedAvatar = "avatargirl1"
                            },
                            onLevelComplete = { coins, stars, time, checkpoint, checkpointTime ->
                                // Save level results
                                lastCoins = coins
                                lastStars = stars
                                lastTime = time
                                lastCheckpoint = checkpoint
                                lastCheckpointTime = checkpointTime
                                
                                // Add earned coins to total
                                totalCoins += coins
                                
                                // Navigate to finish screen
                                currentScreen = "finish"
                                
                                // Send level data to server
                                coroutineScope.launch {
                                    sendLevelData(
                                        className = className,
                                        studentName = studentName,
                                        levelTime = time,
                                        checkpointTime = checkpointTime,
                                        reachedCheckpoint = checkpoint,
                                        coinsCollected = coins,
                                        starsEarned = stars
                                    )
                                }
                            }
                        )
                    }
                    "finish" -> {
                        FinishScreen(
                            studentName = studentName,
                            levelTime = lastTime,
                            coinsCollected = lastCoins,
                            starsEarned = lastStars,
                            checkpointReached = lastCheckpoint,
                            checkpointTime = lastCheckpointTime,
                            onBackToStart = {
                                currentScreen = "start"
                                className = ""
                                studentName = ""
                                classId = 0
                                selectedAvatar = "avatargirl1"
                            },
                            onGoToShop = {
                                currentScreen = "shop"
                            },
                            onGoToAvatarChange = {
                                currentScreen = "avatarchange"
                            }
                        )
                    }
                    "shop" -> {
                        ShopScreen(
                            totalCoins = totalCoins,
                            onBackPressed = {
                                currentScreen = "finish"
                            },
                            onBuyLifePotion = {
                                if (totalCoins >= 15) totalCoins -= 15
                            }
                        )
                    }
                    "avatarchange" -> {
                        AvatarChangeScreen(
                            totalCoins = totalCoins,
                            currentAvatar = selectedAvatar,
                            ownedAvatars = ownedAvatars,
                            onBackPressed = {
                                currentScreen = "finish"
                            },
                            onBuyAvatar = { avatar, price ->
                                if (totalCoins >= price) {
                                    totalCoins -= price
                                    // Buying auto-equips: remove old avatar from owned, add new one
                                    ownedAvatars = setOf(avatar)
                                    selectedAvatar = avatar
                                }
                            },
                            onEquipAvatar = { avatar ->
                                selectedAvatar = avatar
                            }
                        )
                    }
                }
            }
        }
    }
}