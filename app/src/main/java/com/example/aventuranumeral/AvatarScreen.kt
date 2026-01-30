package com.example.aventuranumeral

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject

suspend fun createStudentWithAvatar(
    classId: Int,
    studentName: String,
    avatar: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("https://aventuranumeralbackend.onrender.com/classes/$classId/students")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val json = """{"student_name": "$studentName", "avatar": "$avatar"}"""
            conn.outputStream.use { it.write(json.toByteArray()) }

            val responseCode = conn.responseCode
            conn.disconnect()

            responseCode == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Composable
fun AvatarScreen(
    classId: Int,
    studentName: String,
    onAvatarSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var currentAvatarIndex by remember { mutableIntStateOf(0) }
    var isCreating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val girlAvatars = listOf(
        "avatargirl1" to R.drawable.avatargirl1,
        "avatargirl2" to R.drawable.avatargirl2,
        "avatargirl3" to R.drawable.avatargirl3
    )

    val boyAvatars = listOf(
        "avatarboy1" to R.drawable.avatarboy1,
        "avatarboy2" to R.drawable.avatarboy2,
        "avatarboy3" to R.drawable.avatarboy3
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            if (selectedGender == null) {
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Selecciona tu avatar",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .height(320.dp)
                            .clickable { selectedGender = "girl" },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.avatargirl1),
                                contentDescription = "Niña",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .height(320.dp)
                            .clickable { selectedGender = "boy" },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.avatarboy1),
                                contentDescription = "Niño",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.volver),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .width(220.dp)
                        .height(70.dp)
                        .clickable { onBack() }
                )

            } else {
                Spacer(modifier = Modifier.height(20.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.volver),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .width(150.dp)
                        .height(60.dp)
                        .clickable {
                            selectedGender = null
                            currentAvatarIndex = 0
                        }
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Elige tu avatar",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                val avatars = if (selectedGender == "girl") girlAvatars else boyAvatars
                val (currentAvatarName, currentAvatarDrawable) = avatars[currentAvatarIndex]

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (currentAvatarIndex > 0) Color.White.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentAvatarIndex > 0) {
                                    currentAvatarIndex--
                                }
                            },
                            enabled = currentAvatarIndex > 0
                        ) {
                            Text(
                                text = "◀",
                                fontSize = 36.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .width(500.dp)
                            .height(500.dp)
                            .border(4.dp, Color(0xFFFFD700), CardDefaults.shape)
                            .clickable {
                                if (!isCreating) {
                                    isCreating = true
                                    coroutineScope.launch {
                                        val success = createStudentWithAvatar(
                                            classId,
                                            studentName,
                                            currentAvatarName
                                        )
                                        if (success) {
                                            onAvatarSelected(currentAvatarName)
                                        }
                                        isCreating = false
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = currentAvatarDrawable),
                                contentDescription = currentAvatarName,
                                modifier = Modifier.fillMaxSize(),
                                alpha = if (isCreating) 0.5f else 1f
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (currentAvatarIndex < avatars.size - 1) Color.White.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentAvatarIndex < avatars.size - 1) {
                                    currentAvatarIndex++
                                }
                            },
                            enabled = currentAvatarIndex < avatars.size - 1
                        ) {
                            Text(
                                text = "▶",
                                fontSize = 36.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isCreating) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
