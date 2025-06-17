package com.example.smartlife

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.smartlife.ui.theme.Gender
import com.example.smartlife.ui.theme.GenderSelectionScreen
import com.example.smartlife.ui.theme.SmartLifeTheme
import com.example.smartlife.ui.theme.WelcomeScreen

sealed class Screen {
    object Welcome : Screen()
    object GenderSelection : Screen()
    object Home : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartLifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("smartlife_prefs", Context.MODE_PRIVATE)
    }

    val isOnboardingComplete = sharedPreferences.getString("user_gender", null) != null

    var currentScreen by remember {
        mutableStateOf<Screen>(if (isOnboardingComplete) Screen.Home else Screen.Welcome)
    }

    when (currentScreen) {
        is Screen.Welcome -> {
            WelcomeScreen(onGetStartedClicked = {
                currentScreen = Screen.GenderSelection
            })
        }
        is Screen.GenderSelection -> {
            GenderSelectionScreen(onContinueClicked = { selectedGender ->
                with(sharedPreferences.edit()) {
                    putString("user_gender", selectedGender.name)
                    putBoolean("welcome_screen_shown", true)
                    apply()
                }
                currentScreen = Screen.Home
            })
        }
        is Screen.Home -> {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("smartlife_prefs", Context.MODE_PRIVATE)
    }
    val savedGender = sharedPreferences.getString("user_gender", "Not Selected")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Main App Content\nYour Gender is: $savedGender")
    }
}
