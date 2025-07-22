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
import com.example.smartlife.screen.recipes.HealthyRecipesScreen
import com.example.smartlife.screen.recipes.Recipe
import com.example.smartlife.screen.voiceassistant.VoiceAssistantScreen
import com.example.smartlife.ui.theme.*

private const val TAG = "MainAppNavigation"

sealed class Screen {
    object Welcome : Screen()
    object NameSelection : Screen()
    object GenderSelection : Screen()
    object HeightSelection : Screen()
    object WeightSelection : Screen()
    object AgeSelection : Screen()
    object Dashboard : Screen()
    object DailyPlanner : Screen()
    object HealthyRecipes : Screen()
    object VoiceAssistant : Screen()
    data class RecipeDetail(val recipe: Recipe) : Screen()
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

    when (val screen = currentScreen) {
        is Screen.Welcome -> {
            WelcomeScreen(onGetStartedClicked = {
                currentScreen = Screen.NameSelection
            })
        }
        is Screen.NameSelection -> {
            NameSelectionScreen(onContinueClicked = { name ->
                with(sharedPreferences.edit()) {
                    putString("user_name", name)
                    apply()
                }
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
                currentScreen = Screen.WeightSelection
            })
        }
        is Screen.WeightSelection -> {
            WeightSelectionScreen(onContinueClicked = { selectedWeight ->
                with(sharedPreferences.edit()) {
                    putInt("user_weight", selectedWeight)
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
            DashboardScreen (
                onCalendarClicked = { currentScreen = Screen.DailyPlanner },
                onHomeClicked = { /* Already on Dashboard */ },
                onRecipesClicked = { currentScreen = Screen.HealthyRecipes },
                onVoiceClicked = { currentScreen = Screen.VoiceAssistant }
            )
        }
        is Screen.DailyPlanner -> {
            DailyPlannerScreen(
                viewModel = taskViewModel,
                onHomeClicked = { currentScreen = Screen.Dashboard },
                onCalendarClicked = { /* Already on DailyPlanner */ },
                onRecipesClicked = { currentScreen = Screen.HealthyRecipes },
                onVoiceClicked = { currentScreen = Screen.VoiceAssistant }
            )
        }
        is Screen.HealthyRecipes -> {
            HealthyRecipesScreen(
                onRecipeSelected = { recipe ->
                    currentScreen = Screen.RecipeDetail(recipe)
                },
                onHomeClicked = { currentScreen = Screen.Dashboard },
                onCalendarClicked = { currentScreen = Screen.DailyPlanner },
                onRecipesClicked = { /* Already on Recipes */ },
                onVoiceClicked = { currentScreen = Screen.VoiceAssistant }
            )
        }
        is Screen.VoiceAssistant -> {
            VoiceAssistantScreen (
                onHomeClicked = { currentScreen = Screen.Dashboard },
                onCalendarClicked = { currentScreen = Screen.DailyPlanner },
                onRecipesClicked = { currentScreen = Screen.HealthyRecipes },
                onVoiceClicked = { /* Already on Voice Assistant */ }
            )
        }
        is Screen.RecipeDetail -> {
            RecipeDetailScreen(
                recipe = screen.recipe,
                onBackClicked = { currentScreen = Screen.HealthyRecipes }
            )
        }
    }
}
