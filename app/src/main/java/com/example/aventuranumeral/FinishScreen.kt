package com.example.aventuranumeral

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinishScreen(
    studentName: String,
    levelTime: Float,
    coinsCollected: Int,
    starsEarned: Int,
    checkpointReached: Boolean,
    checkpointTime: Float?,
    onBackToStart: () -> Unit,
    onGoToShop: () -> Unit,
    onGoToAvatarChange: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content - no scroll, fits on one page
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Puntaje title
            Text(
                text = "Puntaje",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                textAlign = TextAlign.Center
            )

            // Stars row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3) { index ->
                    Image(
                        painter = painterResource(R.drawable.star),
                        contentDescription = "Estrella ${index + 1}",
                        modifier = Modifier.size(80.dp),
                        alpha = if (index < starsEarned) 1f else 0.3f
                    )
                }
            }

            // Coins earned with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.coin),
                    contentDescription = "Monedas",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "$coinsCollected",
                    fontSize = 32.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            }

            // Shop & Avatar buttons row (using drawable images, same size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(R.drawable.tienda),
                    contentDescription = "Tienda",
                    modifier = Modifier
                        .height(55.dp)
                        .clickable { onGoToShop() },
                    contentScale = ContentScale.FillHeight
                )

                Image(
                    painter = painterResource(R.drawable.avatar),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .height(55.dp)
                        .clickable { onGoToAvatarChange() },
                    contentScale = ContentScale.FillHeight
                )
            }

            // CONTINUAR button - using drawable image, same height as other buttons
            Image(
                painter = painterResource(R.drawable.continuar),
                contentDescription = "Continuar",
                modifier = Modifier
                    .height(55.dp)
                    .clickable { onBackToStart() },
                contentScale = ContentScale.FillHeight
            )
        }
    }
}
