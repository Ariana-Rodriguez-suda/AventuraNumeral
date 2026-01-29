package com.example.aventuranumeral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.aventuranumeral.ui.theme.AventuraNumeralTheme
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AventuraNumeralTheme {
                var gameStarted by remember { mutableStateOf(false) }
                var className by remember { mutableStateOf("") }
                var studentName by remember { mutableStateOf("") }

                if (gameStarted) {
                    GameScreen(
                        className = className,
                        studentName = studentName,
                        onExitLevel = {
                            gameStarted = false
                            className = ""
                            studentName = ""
                        }
                    )
                } else {
                    StartScreen(onStartGame = { cls, name ->
                        className = cls
                        studentName = name
                        gameStarted = true
                    })
                }
            }
        }
    }
}