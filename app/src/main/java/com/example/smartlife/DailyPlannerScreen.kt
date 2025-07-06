package com.example.smartlife.screen.dailyplanner

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.smartlife.data.TaskEntity
import com.example.smartlife.model.TaskViewModel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyPlannerScreen(viewModel: TaskViewModel,onBack: () -> Unit) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTime by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text("Add Task", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeLabel = selectedTime?.let { timeFormat.format(Date(it)) } ?: "Select Time"

        Button(onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    selectedTime = calendar.timeInMillis
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }) {
            Text(timeLabel)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (name.text.isNotBlank() && selectedTime != null) {
                    val task = TaskEntity(
                        name = name.text,
                        description = description.text,
                        dateTime = selectedTime!!,
                        isCompleted = false
                    )
                    viewModel.addTask(task)
                    name = TextFieldValue("")
                    description = TextFieldValue("")
                    selectedTime = null
                    Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tasks", style = MaterialTheme.typography.titleLarge)

        LazyColumn {
            items(tasks) { task ->
                TaskItem(task = task, onCheckChanged = { isChecked ->
                    viewModel.updateTask(task.copy(isCompleted = isChecked))
                })
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, onCheckChanged: (Boolean) -> Unit) {
    val currentTime = System.currentTimeMillis()
    val isMissed = !task.isCompleted && task.dateTime < currentTime

    val backgroundColor = when {
        task.isCompleted -> Color(0xFFD1F2EB) // Greenish
        isMissed -> Color(0xFFFFCDD2) // Reddish
        else -> Color(0xFFE3F2FD) // Light blue
    }

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(task.dateTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.name, style = MaterialTheme.typography.titleMedium)
                Text(text = task.description ?: "", style = MaterialTheme.typography.bodySmall)
                Text(text = "Time: $formattedTime", style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckChanged
            )
        }
    }
}
