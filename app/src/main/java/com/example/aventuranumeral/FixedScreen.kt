package com.example.aventuranumeral

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun FixedScreen(
    errorType: String,
    errorDetail: String,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (errorType == "wrong_block") "¡Bloque Incorrecto!" else "¡Suma Incorrecta!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE84820),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC2D5A3D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (errorType == "wrong_block") {
                        Text(
                            text = "¿Cómo se representan las fracciones?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Las fracciones se representan dividiendo un bloque en partes iguales. El numerador (arriba) indica las partes coloreadas y el denominador (abajo) el total de partes.",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(R.drawable.bloquecespeddos),
                                    contentDescription = "Bloque 2/5",
                                    modifier = Modifier.size(120.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("2/5 ✓", color = Color(0xFF4CAF50), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("5 partes, 2 coloreadas", color = Color.White, fontSize = 12.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(R.drawable.bloquecesped),
                                    contentDescription = "Bloque 2/4",
                                    modifier = Modifier.size(120.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("2/4 ✗", color = Color(0xFFE84820), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("4 partes, 2 coloreadas", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "El agujero necesitaba el bloque de 2/5, no el de 2/4. Aunque ambos tienen 2 partes coloreadas, el total de partes es diferente.",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    } else {
                        Text(
                            text = "¿Cómo se suman fracciones?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Para sumar fracciones con el mismo denominador, se suman solo los numeradores y se mantiene el denominador.",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(R.drawable.bloquesumauno),
                                    contentDescription = "Bloque 2/4",
                                    modifier = Modifier.size(120.dp)
                                )
                                Text("2/4", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("+", color = Color(0xFFFFD700), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(R.drawable.bloquesumados),
                                    contentDescription = "Bloque 1/4",
                                    modifier = Modifier.size(120.dp)
                                )
                                Text("1/4", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("=", color = Color(0xFFFFD700), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(R.drawable.bloqueresult),
                                    contentDescription = "Resultado 3/4",
                                    modifier = Modifier.size(120.dp)
                                )
                                Text("3/4", color = Color(0xFF4CAF50), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorDetail,
                            fontSize = 18.sp,
                            color = Color(0xFFE84820),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "La respuesta correcta era 2/4 + 1/4 = 3/4. Recuerda: solo suma los numeradores cuando el denominador es igual.",
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(70.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE84820)),
                onClick = onRetry
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONTINUAR",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}