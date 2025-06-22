package com.example.smartlife.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.absoluteValue

@Composable
fun AgeSelectionScreen(onContinueClicked: (Int) -> Unit) {
    var selectedAge by remember { mutableStateOf(16) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "What is your age?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF2F2F2F)
            )
            Spacer(modifier = Modifier.weight(0.5f))

            AgePicker(
                initialAge = selectedAge,
                onAgeSelected = { age -> selectedAge = age },
                modifier = Modifier.weight(2f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onContinueClicked(selectedAge) },
                modifier = Modifier
                    .width(250.dp)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Continue", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun AgePicker(
    modifier: Modifier = Modifier,
    initialAge: Int,
    onAgeSelected: (Int) -> Unit,
    minAge: Int = 10,
    maxAge: Int = 100
) {
    val ageRange = (minAge..maxAge).toList()
    val listState = rememberLazyListState()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val itemHeightPx = with(density) { 220.dp.toPx() }
        val screenHeightPx = constraints.maxHeight.toFloat()
        val scrollOffset = -((screenHeightPx / 2) - (itemHeightPx / 2)).toInt()

        LaunchedEffect(initialAge) {
            val initialIndex = ageRange.indexOf(initialAge)
            if (initialIndex != -1) {
                listState.animateScrollToItem(initialIndex, scrollOffset)
                onAgeSelected(initialAge)
            }
        }

        val centeredItemIndex by remember {
            derivedStateOf {
                listState.layoutInfo.visibleItemsInfo
                    .minByOrNull { (it.offset + it.size / 2 - screenHeightPx / 2).absoluteValue }
                    ?.index ?: -1
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(ageRange.size) { index ->
                val age = ageRange[index]
                val isSelected = index == centeredItemIndex && centeredItemIndex != -1

                val scale = if(isSelected) 1f else 0.75f

                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ){
                            Box(
                                modifier = Modifier
                                    .size(165.dp)
                                    .background(PrimaryBlue, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$age",
                                    color = Color.White,
                                    fontSize = 120.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                    } else {
                        Text(
                            text = "$age",
                            color = Color.Gray,
                            fontSize = 68.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.isScrollInProgress }
                .filter { !it && centeredItemIndex != -1 }
                .distinctUntilChanged()
                .collect {
                    val targetIndex = centeredItemIndex
                    listState.animateScrollToItem(targetIndex, scrollOffset)
                    onAgeSelected(ageRange[targetIndex])
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AgeSelectionScreenPreview() {
    SmartLifeTheme {
        AgeSelectionScreen(onContinueClicked = {})
    }
}
