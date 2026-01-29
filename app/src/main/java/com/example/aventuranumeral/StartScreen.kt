package com.example.aventuranumeral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ClassInfo(val id: Int, val name: String)
data class StudentInfo(val id: Int, val name: String)

suspend fun fetchClasses(): List<ClassInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("https://aventuranumeralbackend.onrender.com/classes")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val classesArray = json.getJSONArray("classes")

            (0 until classesArray.length()).map { i ->
                val obj = classesArray.getJSONObject(i)
                ClassInfo(obj.getInt("id"), obj.getString("class_name"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun fetchStudents(classId: Int): List<StudentInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("https://aventuranumeralbackend.onrender.com/classes/$classId/student-names")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val studentsArray = json.getJSONArray("students")

            (0 until studentsArray.length()).map { i ->
                val obj = studentsArray.getJSONObject(i)
                StudentInfo(obj.getInt("id"), obj.getString("student_name"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(onStartGame: (String, String) -> Unit) {

    var classes by remember { mutableStateOf<List<ClassInfo>>(emptyList()) }
    var students by remember { mutableStateOf<List<StudentInfo>>(emptyList()) }

    var selectedClass by remember { mutableStateOf<ClassInfo?>(null) }
    var selectedStudent by remember { mutableStateOf<StudentInfo?>(null) }

    var classExpanded by remember { mutableStateOf(false) }
    var studentExpanded by remember { mutableStateOf(false) }

    var showCreateNew by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        classes = fetchClasses()
    }

    LaunchedEffect(selectedClass) {
        selectedStudent = null
        if (selectedClass != null) {
            students = fetchStudents(selectedClass!!.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Aventura Numeral",
                fontSize = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            ExposedDropdownMenuBox(
                expanded = classExpanded,
                onExpandedChange = { classExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedClass?.name ?: "Selecciona tu clase",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .width(300.dp)
                )

                ExposedDropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false }
                ) {
                    classes.forEach { classInfo ->
                        DropdownMenuItem(
                            text = { Text(classInfo.name) },
                            onClick = {
                                selectedClass = classInfo
                                classExpanded = false
                                showCreateNew = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedClass != null && !showCreateNew) {
                ExposedDropdownMenuBox(
                    expanded = studentExpanded,
                    onExpandedChange = { studentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.name ?: "Selecciona tu nombre",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .width(300.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = studentExpanded,
                        onDismissRequest = { studentExpanded = false }
                    ) {
                        students.forEach { student ->
                            DropdownMenuItem(
                                text = { Text(student.name) },
                                onClick = {
                                    selectedStudent = student
                                    studentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedButton(
                    onClick = { showCreateNew = true },
                    modifier = Modifier.width(300.dp)
                ) {
                    Text("➕ Crear nuevo jugador")
                }
            }

            if (showCreateNew) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newStudentName,
                    onValueChange = { newStudentName = it },
                    label = { Text("Tu nombre") },
                    singleLine = true,
                    modifier = Modifier.width(300.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        showCreateNew = false
                        selectedStudent = null
                    },
                    modifier = Modifier.width(300.dp)
                ) {
                    Text("❌ Cancelar")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    val className = selectedClass?.name ?: return@Button
                    val studentName = if (showCreateNew) {
                        newStudentName.takeIf { it.isNotBlank() } ?: return@Button
                    } else {
                        selectedStudent?.name ?: return@Button
                    }
                    onStartGame(className, studentName)
                },
                modifier = Modifier.size(width = 250.dp, height = 60.dp),
                enabled = selectedClass != null &&
                        (selectedStudent != null || (showCreateNew && newStudentName.isNotBlank()))
            ) {
                Text("🎮 Comenzar Nivel", fontSize = 18.sp)
            }
        }
    }
}