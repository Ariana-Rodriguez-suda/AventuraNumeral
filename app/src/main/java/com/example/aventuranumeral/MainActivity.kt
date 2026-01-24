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
                var playerName by remember { mutableStateOf("") }

                if (gameStarted) {
                    GameScreen(
                        playerName = playerName,
                        onExitLevel = {
                            gameStarted = false
                            playerName = ""
                        }
                    )
                } else {
                    StartScreen(onStartGame = { name ->
                            playerName = name
                            gameStarted = true
                        }
                    )
                }
            }
        }
    }
}
