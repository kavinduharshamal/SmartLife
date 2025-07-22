package com.example.smartlife

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.smartlife.screen.dashboard.DashboardScreen
import com.example.smartlife.ui.theme.*

private const val TAG = "MainAppNavigation"

sealed class Screen {
    object Welcome : Screen()
    object GenderSelection : Screen()
    object HeightSelection : Screen()
    object WeightSelection : Screen()
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
    val taskDao = db.taskDao()
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(taskDao))


    var currentScreen by remember {
        mutableStateOf<Screen>(if (isOnboardingComplete) Screen.Dashboard else Screen.Welcome)
    }

    Log.d(TAG, "Current Screen: ${currentScreen::class.java.simpleName}")

    when (currentScreen) {
        is Screen.Welcome -> {
            WelcomeScreen(onGetStartedClicked = {
                Log.d(TAG, "Navigating from Welcome to GenderSelection")
                currentScreen = Screen.GenderSelection
            })
        }
        is Screen.GenderSelection -> {
            GenderSelectionScreen(onContinueClicked = { selectedGender ->
                with(sharedPreferences.edit()) {
                    putString("user_gender", selectedGender.name)
                    apply()
                }
                Log.d(TAG, "Navigating from GenderSelection to HeightSelection")
                currentScreen = Screen.HeightSelection
            })
        }
        is Screen.HeightSelection -> {
            HeightSelectionScreen(onContinueClicked = { selectedHeight ->
                with(sharedPreferences.edit()) {
                    putInt("user_height", selectedHeight)
                    apply()
                }
                Log.d(TAG, "Navigating from HeightSelection to WeightSelection")
                currentScreen = Screen.WeightSelection
            })
        }
        is Screen.WeightSelection -> {
            WeightSelectionScreen(onContinueClicked = { selectedWeight ->
                with(sharedPreferences.edit()) {
                    putInt("user_weight", selectedWeight)
                    apply()
                }
                Log.d(TAG, "Navigating from WeightSelection to AgeSelection")
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
                Log.d(TAG, "Navigating from AgeSelection to Dashboard")
                currentScreen = Screen.Dashboard
            })
        }
        is Screen.Dashboard -> {
            DashboardScreen (
                onCalendarClicked = {
                    Log.d(TAG, "Dashboard: onCalendarClicked triggered. Attempting to navigate to DailyPlanner.")
                    currentScreen = Screen.DailyPlanner
                },
                onHomeClicked = {
                    Log.d(TAG, "Dashboard: onHomeClicked triggered. Already on Dashboard.")
                }
            )
        }
        is Screen.DailyPlanner -> {
            DailyPlannerScreen(
                viewModel = taskViewModel,
                onHomeClicked = {
                    Log.d(TAG, "DailyPlanner: onHomeClicked triggered. Attempting to navigate to Dashboard.")
                    currentScreen = Screen.Dashboard
                },
                onCalendarClicked = {
                    Log.d(TAG, "DailyPlanner: onCalendarClicked triggered. Already on DailyPlanner.")
                }
            )
        }
    }
}
