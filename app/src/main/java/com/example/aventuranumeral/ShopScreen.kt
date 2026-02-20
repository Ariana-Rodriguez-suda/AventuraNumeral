package com.example.aventuranumeral

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Suppress("unused")
@Composable
fun ShopScreen(
    totalCoins: Int,
    onBackPressed: () -> Unit,
    onBuyLifePotion: () -> Unit
) {
    var showPurchaseDialog by remember { mutableStateOf(false) }
    val lifePotionPrice = 15

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    onClick = onBackPressed
                ) {
                    Image(
                        painter = painterResource(R.drawable.volver),
                        contentDescription = "Volver",
                        modifier = Modifier.size(60.dp)
                    )
                }

                Text(
                    text = "🛒 TIENDA",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Contador de monedas
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D5A3D))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.coin),
                            contentDescription = "Monedas",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$totalCoins",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Items de la tienda
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Item: Poción de vida
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D5A3D)),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            if (totalCoins >= lifePotionPrice) {
                                showPurchaseDialog = true
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.item1),
                                contentDescription = "Poción de Vida",
                                modifier = Modifier.size(100.dp)
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🧪 Poción de Vida",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Recupera 1 vida",
                                    fontSize = 16.sp,
                                    color = Color(0xFFCCCCCC)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.coin),
                                        contentDescription = "Precio",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$lifePotionPrice",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalCoins >= lifePotionPrice) Color(0xFFFFD700) else Color.Red
                                    )
                                }
                            }

                            if (totalCoins < lifePotionPrice) {
                                Text(
                                    text = "💰\nInsuficiente",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de compra
    if (showPurchaseDialog) {
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = false },
            title = {
                Text("🧪 Confirmar Compra")
            },
            text = {
                Text(
                    "¿Quieres comprar la Poción de Vida por $lifePotionPrice monedas?\n\n" +
                    "Recupera 1 vida instantáneamente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurchaseDialog = false
                        onBuyLifePotion()
                    }
                ) {
                    Text("Comprar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPurchaseDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}