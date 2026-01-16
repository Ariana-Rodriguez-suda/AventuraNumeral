package com.example.aventuranumeral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.aventuranumeral.ui.theme.AventuraNumeralTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AventuraNumeralTheme {
                GameScreen()
            }
        }
    }
}
