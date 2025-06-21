package com.example.smartlife.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.R
import kotlin.math.absoluteValue

enum class Gender(val displayName: String, val illustration: Int) {
    FEMALE("I Am Female", R.drawable.female_icon),
    MALE("I Am Male", R.drawable.male_icon),
    OTHER("Other", R.drawable.other_icon)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelectionScreen(onContinueClicked: (Gender) -> Unit) {
    val genders = Gender.values()
    val pagerState = rememberPagerState(initialPage = 1) { genders.size }
    var selectedGender by remember { mutableStateOf(genders[pagerState.currentPage]) }

    LaunchedEffect(pagerState.currentPage) {
        selectedGender = genders[pagerState.currentPage]
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "What is your Gender?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please select your gender for a better personalized health experience.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) { page ->
                    val gender = genders[page]
                    GenderCard(
                        modifier = Modifier
                            .width(380.dp)
                            .aspectRatio(0.7f)
                            .graphicsLayer {
                                val pageOffset = (
                                        (pagerState.currentPage - page) + pagerState
                                            .currentPageOffsetFraction
                                        ).absoluteValue
                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleY = lerp(
                                    start = 0.7f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            },
                        gender = gender,
                        isSelected = selectedGender == gender,
                        onClick = { selectedGender = gender }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onContinueClicked(selectedGender) },
                modifier = Modifier
                    .width(250.dp)
                    .height(76.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Continue", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun GenderCard(
    modifier: Modifier = Modifier,
    gender: Gender,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = gender.illustration),
                contentDescription = gender.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }

}

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

@Preview(showBackground = true)
@Composable
fun GenderSelectionScreenPreview() {
    SmartLifeTheme {
        GenderSelectionScreen(onContinueClicked = {})
    }
}
