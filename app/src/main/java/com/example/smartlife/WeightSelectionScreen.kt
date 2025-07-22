package com.example.smartlife

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.ui.theme.PrimaryBlue
import com.example.smartlife.ui.theme.SmartLifeTheme
import kotlin.math.roundToInt

@Composable
fun WeightSelectionScreen(onContinueClicked: (Int) -> Unit) {
    var weight by remember { mutableStateOf(60) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "What is your Weight?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please select your weight for a better personalized health experience.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$weight",
                    fontSize = 66.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "kg",
                    fontSize = 46.sp,
                    color = Color.Gray
                )
            }


            Spacer(modifier = Modifier.weight(1f))

            RulerPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                onValueChange = { weight = it },
                initialValue = weight,
                min = 30,
                max = 150
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onContinueClicked(weight) },
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RulerPicker(
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit,
    initialValue: Int,
    min: Int = 50,
    max: Int = 250
) {
    val spacing = with(LocalDensity.current) { 10.dp.toPx() }
    var offset by remember {
        mutableFloatStateOf((initialValue - min) * spacing)
    }

    val textPaint = android.graphics.Paint().apply {
        color = Color.Black.hashCode()
        textSize = 18.sp.value
        textAlign = android.graphics.Paint.Align.CENTER
    }

    BoxWithConstraints(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                offset -= dragAmount.x
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = width / 2

            val currentValueFloat = min + offset / spacing
            val halfWidthInValues = center / spacing

            val firstVisibleValue = (currentValueFloat - halfWidthInValues).toInt() - 2
            val lastVisibleValue = (currentValueFloat + halfWidthInValues).toInt() + 2

            for (i in firstVisibleValue..lastVisibleValue) {
                if (i in min..max) {
                    val lineX = center - offset + (i - min) * spacing
                    val lineHeight = when {
                        i % 10 == 0 -> 50.dp.toPx()
                        i % 5 == 0 -> 35.dp.toPx()
                        else -> 20.dp.toPx()
                    }
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(x = lineX, y = height / 2 - lineHeight / 2),
                        end = Offset(x = lineX, y = height / 2 + lineHeight / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                    if (i % 10 == 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "$i",
                            lineX,
                            height / 2 + lineHeight / 2 + 20.dp.toPx(),
                            textPaint
                        )
                    }
                }
            }

            drawLine(
                color = PrimaryBlue,
                start = Offset(x = center, y = height/2 - 55.dp.toPx()),
                end = Offset(x = center, y = height/2 + 55.dp.toPx()),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        LaunchedEffect(offset) {
            val currentValue = (min + (offset / spacing)).roundToInt()
            onValueChange(currentValue.coerceIn(min, max))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WeightSelectionScreenPreview() {
    SmartLifeTheme {
        WeightSelectionScreen(onContinueClicked = {})
    }
}
