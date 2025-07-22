package com.example.smartlife.screen.dailyplanner

import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartlife.BottomNavigationBar
import com.example.smartlife.data.TaskEntity
import com.example.smartlife.model.TaskViewModel.TaskViewModel
import com.example.smartlife.model.TaskViewModelFactory
import com.example.smartlife.ui.theme.Pink40
import com.example.smartlife.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPlannerScreen(
    viewModel: TaskViewModel,
    onHomeClicked: () -> Unit,
    onCalendarClicked: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()

    val colorGreen = Color(0xFFE0F7E9)
    val colorGreenBorder = Color(0xFF4CAF50)
    val colorRed = Color(0xFFFFE5E5)
    val colorRedBorder = Color(0xFFD32F2F)
    val colorBlue = Color(0xFFD6ECFF)
    val colorBlueBorder = Color(0xFF1976D2)
    val colorPink = Pink40

    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val sunday = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() % 7)
    val week = (0..6).map { sunday.plusDays(it.toLong()) }

    val months = Month.values().map { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(selectedDate.monthValue - 1) }

    LaunchedEffect(selectedDate) {
        selectedMonth = selectedDate.monthValue - 1
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        bottomBar = {
            BottomNavigationBar(
                onHomeClicked = onHomeClicked,
                onCalendarClicked = onCalendarClicked,
                initialSelectedItem = 2
            )
        },
        containerColor = Color.White
    ) { inner ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                Box {
                    Button(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 1.dp),
                        modifier = Modifier
                            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(50))
                    ) {
                        Text(months[selectedMonth], fontSize = 14.sp, color = Color.Black)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.DarkGray)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        months.forEachIndexed { index, month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    selectedMonth = index
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color.DarkGray)
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                items(week) { date ->
                    val isSelected = date == selectedDate
                    val pillColor = when {
                        isSelected && date.dayOfWeek.value == 7 -> colorPink
                        isSelected -> Color(0xFFD7F1FF)
                        else -> Color.White
                    }
                    val borderColor = if (isSelected) Color(0xFF5BC8FF) else Color.LightGray

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(pillColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedDate = date }
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            fontSize = 12.sp,
                            color = if (isSelected) Color.DarkGray else Color.Gray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.DarkGray else Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(tasks.filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
                    val taskDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                    taskDate == selectedDate
                }) { task ->
                    val now = System.currentTimeMillis()
                    val isMissed = !task.isCompleted && task.dateTime < now
                    val bgColor = when {
                        task.isCompleted -> colorGreen
                        isMissed -> colorRed
                        else -> colorBlue
                    }
                    val borderColor = when {
                        task.isCompleted -> colorGreenBorder
                        isMissed -> colorRedBorder
                        else -> colorBlueBorder
                    }

                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(task.dateTime)),
                            fontSize = 12.sp,
                            modifier = Modifier.width(72.dp),
                            color = Color.Gray
                        )

                        Spacer(Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = borderColor)
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    if (!task.description.isNullOrBlank()) {
                                        Text(task.description ?: "", fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                }
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.updateTask(task.copy(isCompleted = it)) },
                                    colors = CheckboxDefaults.colors(checkedColor = borderColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Add Task", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Task Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        val cal = Calendar.getInstance()
                        val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val timeLabel = selectedTime?.let { fmt.format(Date(it)) } ?: "Select Time"
                        Button(onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                cal.set(Calendar.HOUR_OF_DAY, h)
                                cal.set(Calendar.MINUTE, m)
                                selectedTime = cal.timeInMillis
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                        }) { Text(timeLabel) }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                            Button(onClick = {
                                if (name.isNotBlank() && selectedTime != null) {
                                    val finalTime = Calendar.getInstance().apply {
                                        timeInMillis = selectedTime!!
                                        set(Calendar.YEAR, selectedDate.year)
                                        set(Calendar.MONTH, selectedDate.monthValue - 1)
                                        set(Calendar.DAY_OF_MONTH, selectedDate.dayOfMonth)
                                    }.timeInMillis
                                    viewModel.addTask(TaskEntity(name = name, description = description, dateTime = finalTime, isCompleted = false))
                                    name = ""
                                    description = ""
                                    selectedTime = null
                                    showDialog = false
                                    Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                }
                            }, modifier = Modifier.weight(1f)) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DailyPlannerPreview() {
    val context = LocalContext.current
    // This is a simplified setup for preview purposes and won't have a real database.
    // In a real app, you might use a fake ViewModel for previews.
    val viewModel: TaskViewModel = viewModel()
    DailyPlannerScreen(viewModel, onHomeClicked = {}, onCalendarClicked = {})
}
