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
                    return@withContext student.optString("avatar", "avatargirl1")
                }
            }
            "avatargirl1"
        } catch (e: Exception) {
            e.printStackTrace()
            "avatargirl1"
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
                var isNewStudent by remember { mutableStateOf(false) }
                var selectedAvatar by remember { mutableStateOf("avatargirl1") }
                var isLoadingAvatar by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                when (currentScreen) {
                    "start" -> {
                        StartScreen(
                            onStartGame = { cls, name, clsId, isNew ->
                                className = cls
                                studentName = name
                                classId = clsId
                                isNewStudent = isNew
                                
                                if (isNew) {
                                    currentScreen = "avatar"
                                } else {
                                    isLoadingAvatar = true
                                    coroutineScope.launch {
                                        selectedAvatar = getStudentAvatar(clsId, name)
                                        isLoadingAvatar = false
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
                                isNewStudent = false
                                selectedAvatar = "avatargirl1"
                            }
                        )
                    }
                }
            }
        }
    }
}