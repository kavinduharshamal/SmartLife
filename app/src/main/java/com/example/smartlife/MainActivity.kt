package com.example.smartlife

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartlife.model.TaskViewModel.TaskViewModel
import com.example.smartlife.model.TaskViewModelFactory
import com.example.smartlife.screen.dailyplanner.DailyPlannerScreen
import com.example.smartlife.ui.theme.*

sealed class Screen {
    object Welcome : Screen()
    object GenderSelection : Screen()
    object HeightSelection : Screen()
    object AgeSelection : Screen()
    object Dashboard : Screen()
    object DailyPlanner : Screen()
}

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("smartlife_prefs", Context.MODE_PRIVATE)
    }

    val isOnboardingComplete = sharedPreferences.getBoolean("welcome_screen_shown", false)
    val db = AppDatabase.getInstance(context)
    val taskDao = db.taskdto()
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(taskDao))


    var currentScreen by remember {
        mutableStateOf<Screen>(if (isOnboardingComplete) Screen.Dashboard else Screen.Welcome)
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
                    apply()
                }
                currentScreen = Screen.HeightSelection
            })
        }
        is Screen.HeightSelection -> {
            HeightSelectionScreen(onContinueClicked = { selectedHeight ->
                with(sharedPreferences.edit()) {
                    putInt("user_height", selectedHeight)
                    apply()
                }
                currentScreen = Screen.AgeSelection
            })
        }
        is Screen.AgeSelection -> {
            AgeSelectionScreen(onContinueClicked = { selectedAge ->
                with(sharedPreferences.edit()) {
                    putInt("user_age", selectedAge)
                    putBoolean("welcome_screen_shown", true)
                    apply()
                }
                currentScreen = Screen.Dashboard
            })
        }
        is Screen.Dashboard -> {
            DashboardScreen(
                onCalendarClicked = { currentScreen = Screen.DailyPlanner }
            )
        }
        is Screen.DailyPlanner -> {
            DailyPlannerScreen(
                viewModel = taskViewModel,
            )
        }

    }
}

