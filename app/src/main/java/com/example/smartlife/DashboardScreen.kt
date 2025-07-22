package com.example.smartlife.screen.dashboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.BottomNavigationBar
import com.example.smartlife.R
import com.example.smartlife.ui.theme.SecondaryLightBlue
import com.example.smartlife.ui.theme.SmartLifeTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCalendarClicked: () -> Unit,
    onHomeClicked: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("smartlife_prefs", Context.MODE_PRIVATE)
    }

    var userAge by remember { mutableStateOf(sharedPreferences.getInt("user_age", 25)) }
    var userGender by remember { mutableStateOf(sharedPreferences.getString("user_gender", "MALE")) }
    var userHeight by remember { mutableStateOf(sharedPreferences.getInt("user_height", 170)) }
    var userWeight by remember { mutableStateOf(sharedPreferences.getInt("user_weight", 70)) }

    val bmi = remember(userWeight, userHeight) {
        val heightInMeters = userHeight / 100f
        (userWeight / (heightInMeters * heightInMeters))
    }

    val sleepRecommendation = remember(userAge) {
        when {
            userAge in 18..64 -> "7-9"
            userAge >= 65 -> "7-8"
            else -> "8-10"
        }
    }

    val waterIntake = remember(userWeight, userGender) {
        val intake = userWeight * 35
        if (userGender == "FEMALE") (intake * 0.9).roundToInt() else intake
    }

    val stepGoal = remember(bmi) {
        when {
            bmi < 18.5 -> 7000
            bmi < 24.9 -> 8000
            bmi < 29.9 -> 10000
            else -> 12000
        }
    }

    val calorieGoal = remember(userWeight, userHeight, userAge, userGender) {
        val bmr = if (userGender == "MALE") {
            10 * userWeight + 6.25 * userHeight - 5 * userAge + 5
        } else {
            10 * userWeight + 6.25 * userHeight - 5 * userAge - 161
        }
        (bmr * 0.25).roundToInt()
    }

    var showWeightDialog by remember { mutableStateOf(false) }
    val weightEntries = remember {
        val entries = sharedPreferences.getString("weight_entries", null)
        mutableStateOf(entries?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList())
    }

    LaunchedEffect(Unit) {
        val lastEntryDate = sharedPreferences.getString("last_weight_entry_date", null)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        if (weightEntries.value.size < 5) {
            showWeightDialog = true
        } else if (lastEntryDate != today) {
            showWeightDialog = true
        }
    }

    if (showWeightDialog) {
        WeightEntryDialog(
            onDismiss = { showWeightDialog = false },
            onWeightEntered = { newWeight ->
                userWeight = newWeight
                val updatedEntries = (weightEntries.value + newWeight).takeLast(7)
                weightEntries.value = updatedEntries
                with(sharedPreferences.edit()) {
                    putInt("user_weight", newWeight)
                    putString("weight_entries", updatedEntries.joinToString(","))
                    putString("last_weight_entry_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    apply()
                }
                showWeightDialog = false
            },
            entryCount = weightEntries.value.size
        )
    }

    Scaffold(
        topBar = { TopAppBar() },
        bottomBar = {
            BottomNavigationBar(
                onCalendarClicked = onCalendarClicked,
                onHomeClicked = onHomeClicked,
                initialSelectedItem = 0
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            IndexesSection(
                sleep = sleepRecommendation,
                water = waterIntake,
                steps = stepGoal,
                calories = calorieGoal
            )
            Spacer(modifier = Modifier.height(24.dp))
            PedometerSection(weightEntries.value)
        }
    }
}

@Composable
fun WeightEntryDialog(onDismiss: () -> Unit, onWeightEntered: (Int) -> Unit, entryCount: Int) {
    var weightInput by remember { mutableStateOf("") }
    val title = if (entryCount < 5) "Enter Your Weight (${entryCount + 1}/5)" else "What's your weight today?"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                weightInput.toIntOrNull()?.let { onWeightEntered(it) }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            if (entryCount >= 5) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}


@Composable
fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.male_icon),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Hello,", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Jade West", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = { /* TODO: Handle menu click */ }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
    }
}

@Composable
fun IndexesSection(sleep: String, water: Int, steps: Int, calories: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Daily Goals", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard("Sleep", sleep, "hrs", Icons.Default.Face, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatCard("Steps", steps.toString(), "steps", Icons.Default.LocationOn, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard("Water", (water / 1000f).toString(), "liters", Icons.Default.Person, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatCard("Calories", calories.toString(), "kcal", Icons.Default.Star, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = SecondaryLightBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    values: List<Int>
) {
    val maxValue = values.maxOrNull() ?: 0
    val barWidth = 24.dp
    val highlightColor = Color(0xFF2EB5FA)
    val defaultColor = Color(0x802EB5FA)
    val gridLineColor = Color(0xFFE0E0E0)
    val gridLinesCount = 5
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").takeLast(values.size)


    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val chartHeight = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val availableHeight = size.height
            val spacing = availableHeight / gridLinesCount

            repeat(gridLinesCount + 1) { i ->
                val y = i * spacing
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .height((chartHeight - 24.dp) * (value / maxValue.toFloat()))
                            .width(barWidth)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (value == values.maxOrNull()) highlightColor else defaultColor
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = days.getOrElse(index) { "" }, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PedometerSection(weightData: List<Int>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Weight Distribution", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { /* TODO */ }) {
                Text(text = "Past Week")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            values = weightData
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    SmartLifeTheme {
        DashboardScreen(onCalendarClicked = {}, onHomeClicked = {})
    }
}
