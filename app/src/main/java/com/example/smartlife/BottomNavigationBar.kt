package com.example.smartlife

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartlife.ui.theme.SecondaryLightBlue

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    onCalendarClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    initialSelectedItem: Int
) {
    var selectedItem by remember { mutableStateOf(initialSelectedItem) }
    val items = listOf(
        "Home" to Icons.Default.Home,
        "Stats" to Icons.Default.Menu,
        "Calendar" to Icons.Default.DateRange,
        "Settings" to Icons.Default.Settings
    )

    val topRoundedCorners = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

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
                        when (item.first) {
                            "Home" -> onHomeClicked()
                            "Calendar" -> onCalendarClicked()
                        }
                    },
                    alwaysShowLabel = false
                )
            }
        }
    }
}
