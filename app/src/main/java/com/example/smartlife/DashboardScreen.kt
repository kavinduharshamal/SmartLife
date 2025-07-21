package com.example.smartlife.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCalendarClicked: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar() },
        bottomBar = { BottomNavigationBar(onCalendarClicked = onCalendarClicked) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            IndexesSection()
            Spacer(modifier = Modifier.height(24.dp))
            PedometerSection()
        }
    }
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
fun IndexesSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Indexes", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { /* TODO */ }) {
                Text(text = "Today")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard("Sleep", "6", "h", Icons.Default.Face, Modifier
                .weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatCard("Activities", "1.2k", "steps", Icons.Default.LocationOn, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard("Water", "0.8", "liters", Icons.Default.Person, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatCard("Calories", "35", "kcal", Icons.Default.Star, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.Gray,
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
    values: List<Int> = listOf(1500, 2100, 1500, 1000, 2200, 1500, 1500),
    days: List<String> = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
) {
    val maxValue = values.maxOrNull() ?: 0
    val barWidth = 24.dp
    val highlightColor = Color(0xFF2EB5FA)
    val defaultColor = Color(0x802EB5FA)
    val gridLineColor = Color(0xFFE0E0E0) // Light gray grid lines
    val gridLinesCount = 5

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val chartHeight = maxHeight
        val chartWidth = maxWidth

        Canvas(modifier = Modifier.fillMaxSize()) {
            val availableHeight = size.height
            val spacing = availableHeight / gridLinesCount

            // Draw horizontal grid lines (skip bottom-most line touching the floor)
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
                                if (value == maxValue) highlightColor else defaultColor
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = days[index], fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PedometerSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Pedometer", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { /* TODO */ }) {
                Text(text = "Past Week")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}


@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    onCalendarClicked: () -> Unit
) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        "Home" to Icons.Default.Home,
        "Stats" to Icons.Default.Menu,
        "Calendar" to Icons.Default.DateRange,
        "Settings" to Icons.Default.Settings
    )

    val topRoundedCorners = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val SecondaryLightBlue = SecondaryLightBlue

    Box(
        modifier = modifier
            .shadow(12.dp, topRoundedCorners, clip = false)
            .clip(topRoundedCorners)
    ) {
        NavigationBar(containerColor = Color.White) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.second,
                            contentDescription = item.first,
                            tint = if (selectedItem == index) SecondaryLightBlue else Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        if (item.first == "Calendar") {
                            onCalendarClicked() // ✅ Navigate to planner
                        }
                    },
                    alwaysShowLabel = false
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    SmartLifeTheme {
        DashboardScreen(onCalendarClicked = {})
    }
}
