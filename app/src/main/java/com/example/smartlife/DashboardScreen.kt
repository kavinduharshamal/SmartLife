package com.example.smartlife.screen.dashboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.launch
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

    var userName by remember { mutableStateOf(sharedPreferences.getString("user_name", "Jade West")) }
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

    var selectedTimeRange by remember { mutableStateOf("Past Week") }
    val chartData = remember(selectedTimeRange, weightEntries.value) {
        when (selectedTimeRange) {
            "Past Month" -> {
                weightEntries.value.takeLast(30).chunked(7).map { if (it.isEmpty()) 0 else it.average().roundToInt() }
            }
            else -> {
                weightEntries.value.takeLast(7)
            }
        }
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
                val updatedEntries = (weightEntries.value + newWeight).takeLast(30)
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
        topBar = { TopAppBar(name = userName ?: "User") },
        bottomBar = {
            BottomNavigationBar(
                onCalendarClicked = onCalendarClicked,
                onHomeClicked = onHomeClicked,
                initialSelectedItem = 0
            )
        },
        containerColor = Color(0xFFF7F8FC)
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
            Spacer(modifier = Modifier.weight(1f))
            PedometerSection(
                weightData = chartData,
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = { selectedTimeRange = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
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
fun TopAppBar(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.male_icon),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Hello,", fontSize = 16.sp, color = Color.Gray)
                Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Sleep", sleep, "hrs", Icons.Default.Face, Modifier.weight(1f))
            StatCard("Steps", steps.toString(), "steps", Icons.Default.LocationOn, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Water", String.format("%.1f", water / 1000f), "liters", Icons.Default.FavoriteBorder, Modifier.weight(1f))
            StatCard("Calories", calories.toString(), "kcal", Icons.Default.Star, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = SecondaryLightBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    values: List<Int>,
    labels: List<String>
) {
    val displayValues = List(labels.size - values.size) { 0 } + values
    val animatables = remember(values) { displayValues.map { Animatable(0f) } }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(values) {
        animatables.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = if (displayValues.getOrNull(index) != null) 1f else 0f,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
        }
    }

    val maxValue = displayValues.maxOrNull()?.takeIf { it > 0 } ?: 1
    val barWidth = 24.dp
    val highlightColor = Color(0xFF2EB5FA)
    val defaultColor = Color(0x802EB5FA)
    val gridLineColor = Color(0xFFE0E0E0)
    val gridLinesCount = 4

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp)
    ) {
        val chartHeight = maxHeight - 40.dp // Space for labels at bottom

        Canvas(modifier = Modifier.fillMaxSize()) {
            val yAxisSpace = 32.dp.toPx()
            val chartDrawableWidth = size.width - yAxisSpace

            val yAxisPaint = android.graphics.Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            val step = (maxValue / gridLinesCount).takeIf { it > 0 } ?: 1
            (0..gridLinesCount).forEach { i ->
                val y = chartHeight.toPx() - (i * (chartHeight.toPx() / gridLinesCount))
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = gridLineColor,
                    start = Offset(yAxisSpace, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        (i * step).toString(),
                        yAxisSpace - 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        yAxisPaint
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp), // Match yAxisSpace
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            displayValues.forEachIndexed { index, value ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedIndex = if (selectedIndex == index) null else index },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (selectedIndex == index && value > 0) {
                        Text(
                            text = "$value kg",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .height(chartHeight * (value / maxValue.toFloat()) * animatables[index].value)
                            .width(barWidth)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (value > 0 && index == displayValues.indexOfLast { it > 0 }) highlightColor else defaultColor
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = labels.getOrElse(index) { "" }, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PedometerSection(
    weightData: List<Int>,
    selectedTimeRange: String,
    onTimeRangeSelected: (String) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val timeRangeOptions = listOf("Past Week", "Past Month")

    val chartLabels = remember(selectedTimeRange) {
        when (selectedTimeRange) {
            "Past Month" -> listOf("Week 1", "Week 2", "Week 3", "Week 4")
            else -> {
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -6)
                (0..6).map {
                    val dayLabel = sdf.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    dayLabel
                }
            }
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Weight Distribution", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Box {
                OutlinedButton(onClick = { isDropdownExpanded = true }) {
                    Text(text = selectedTimeRange)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    timeRangeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onTimeRangeSelected(option)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BarChart(
            values = weightData,
            labels = chartLabels
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun DashboardScreenPreview() {
    SmartLifeTheme {
        DashboardScreen(onCalendarClicked = {}, onHomeClicked = {})
    }
}
