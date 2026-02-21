package com.example.aventuranumeral

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.compose.foundation.clickable

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
fun StartScreen(onStartGame: (String, String, Int) -> Unit) {

    var classes by remember { mutableStateOf<List<ClassInfo>>(emptyList()) }
    var students by remember { mutableStateOf<List<StudentInfo>>(emptyList()) }

    var selectedClass by remember { mutableStateOf<ClassInfo?>(null) }
    var selectedStudent by remember { mutableStateOf<StudentInfo?>(null) }

    var classExpanded by remember { mutableStateOf(false) }
    var studentExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        classes = fetchClasses()
    }

    LaunchedEffect(selectedClass) {
        selectedStudent = null
        if (selectedClass != null) {
            students = fetchStudents(selectedClass!!.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "Fondo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 60.dp)
            ) {

                // Row con clase a la izquierda y nombre a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (selectedClass != null) Arrangement.SpaceEvenly else Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    // Dropdown de clase
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
                                .width(280.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
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
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown de nombre - aparece a la derecha cuando se selecciona clase
                    if (selectedClass != null) {
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
                                    .width(280.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
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
                    }
                }

                if (selectedClass != null) {
                    Spacer(modifier = Modifier.height(30.dp))

                    val canStart = selectedClass != null && selectedStudent != null

                    Image(
                        painter = painterResource(id = R.drawable.jugar),
                        contentDescription = "Jugar",
                        modifier = Modifier
                            .width(280.dp)
                            .height(80.dp)
                            .clickable(enabled = canStart) {
                                val classInfo = selectedClass ?: return@clickable
                                val studentInfo = selectedStudent ?: return@clickable
                                onStartGame(classInfo.name, studentInfo.name, classInfo.id)
                            },
                        alpha = if (canStart) 1f else 0.5f
                    )

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}