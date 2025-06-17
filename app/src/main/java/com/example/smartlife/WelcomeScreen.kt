package com.example.smartlife.ui.theme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.R

@Composable
fun WelcomeScreen(onGetStartedClicked: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Image(
                painter = painterResource(id = R.drawable.monotone_health_plus),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to",
                fontSize = 28.sp,
                color = Color.Black
            )
            Text(
                text = "SmartLife",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Image(
                painter = painterResource(id = R.drawable.login_logo),
                contentDescription = "Welcome Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            )
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGetStartedClicked,
                modifier = Modifier
                    .width(250.dp)
                    .height(76.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Get Started", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    SmartLifeTheme {
        WelcomeScreen(onGetStartedClicked = {})
    }
}
