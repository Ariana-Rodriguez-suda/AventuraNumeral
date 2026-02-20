package com.example.aventuranumeral

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Avatar(
    val id: String,
    val drawableResource: Int,
    val name: String,
    val price: Int,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false
)

@Suppress("unused")
@Composable
fun AvatarChangeScreen(
    totalCoins: Int,
    currentAvatar: String,
    ownedAvatars: Set<String>,
    onBackPressed: () -> Unit,
    onBuyAvatar: (String, Int) -> Unit,
    onEquipAvatar: (String) -> Unit
) {
    var showPurchaseDialog by remember { mutableStateOf<Avatar?>(null) }
    
    val avatars = listOf(
        Avatar("avatargirl1", R.drawable.avatargirl1, "Chica 1", 10, 
               isOwned = ownedAvatars.contains("avatargirl1"), 
               isEquipped = currentAvatar == "avatargirl1"),
        Avatar("avatargirl2", R.drawable.avatargirl2, "Chica 2", 10, 
               isOwned = ownedAvatars.contains("avatargirl2"), 
               isEquipped = currentAvatar == "avatargirl2"),
        Avatar("avatargirl3", R.drawable.avatargirl3, "Chica 3", 10, 
               isOwned = ownedAvatars.contains("avatargirl3"), 
               isEquipped = currentAvatar == "avatargirl3"),
        Avatar("avatarboy1", R.drawable.avatarboy1, "Chico 1", 10, 
               isOwned = ownedAvatars.contains("avatarboy1"), 
               isEquipped = currentAvatar == "avatarboy1"),
        Avatar("avatarboy2", R.drawable.avatarboy2, "Chico 2", 10, 
               isOwned = ownedAvatars.contains("avatarboy2"), 
               isEquipped = currentAvatar == "avatarboy2"),
        Avatar("avatarboy3", R.drawable.avatarboy3, "Chico 3", 10, 
               isOwned = ownedAvatars.contains("avatarboy3"), 
               isEquipped = currentAvatar == "avatarboy3")
    )

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
                    text = "👤 AVATARES",
                    fontSize = 28.sp,
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

            Spacer(modifier = Modifier.height(30.dp))

            // Grid de avatares usando LazyColumn + Rows (compatible con API 29)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(10.dp)
            ) {
                val avatarRows = avatars.chunked(2)
                items(avatarRows) { rowAvatars ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        rowAvatars.forEach { avatar ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(200.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (avatar.isEquipped) Color(0xFF4CAF50) 
                                                   else Color(0xFF2D5A3D)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                onClick = {
                                    when {
                                        avatar.isEquipped -> { /* Ya equipado, no hacer nada */ }
                                        avatar.isOwned -> onEquipAvatar(avatar.id)
                                        totalCoins >= avatar.price -> showPurchaseDialog = avatar
                                        else -> { /* No hay suficientes monedas */ }
                                    }
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Imagen del avatar
                                    Image(
                                        painter = painterResource(avatar.drawableResource),
                                        contentDescription = avatar.name,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .weight(1f),
                                        colorFilter = if (avatar.isOwned && !avatar.isEquipped) 
                                            ColorFilter.tint(Color.Gray.copy(alpha = 0.3f)) else null
                                    )

                                    // Nombre
                                    Text(
                                        text = avatar.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    // Status/Price
                                    if (avatar.isEquipped) {
                                        Text(
                                            text = "✅ EQUIPADO",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    } else if (avatar.isOwned) {
                                        Text(
                                            text = "COMPRADO\n(Toca para equipar)",
                                            fontSize = 10.sp,
                                            color = Color(0xFFCCCCCC),
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.coin),
                                                contentDescription = "Precio",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${avatar.price}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (totalCoins >= avatar.price) 
                                                    Color(0xFFFFD700) else Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (rowAvatars.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Diálogo de compra
    showPurchaseDialog?.let { avatar ->
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = null },
            title = {
                Text("👤 Confirmar Compra")
            },
            text = {
                Column {
                    Image(
                        painter = painterResource(avatar.drawableResource),
                        contentDescription = avatar.name,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "¿Quieres comprar ${avatar.name} por ${avatar.price} monedas?\n\n" +
                        "Se equipará automáticamente después de la compra.",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurchaseDialog = null
                        onBuyAvatar(avatar.id, avatar.price)
                    }
                ) {
                    Text("Comprar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPurchaseDialog = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}