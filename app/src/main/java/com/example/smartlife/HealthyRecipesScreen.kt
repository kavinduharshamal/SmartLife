package com.example.smartlife.screen.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.R
import com.example.smartlife.ui.components.BottomNavigationBar
import com.example.smartlife.ui.theme.SmartLifeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthyRecipesScreen(
    onRecipeSelected: (Recipe) -> Unit,
    onHomeClicked: () -> Unit,
    onCalendarClicked: () -> Unit,
    onRecipesClicked: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Breakfast") }
    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
    val recipes = getRecipesByCategory(selectedCategory)
    val popularRecipes = getPopularRecipes()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onCalendarClicked = onCalendarClicked,
                onHomeClicked = onHomeClicked,
                onRecipesClicked = onRecipesClicked,
                initialSelectedItem = 1 // Recipes is the second item (index 1)
            )
        },
        containerColor = Color(0xFFF7F8FC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Healthy Food Recipes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F2F2F)
            )
            Text(
                text = "Healthy and nutritious food recipes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFD9800)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { category ->
                    Button(
                        onClick = { selectedCategory = category },
                        modifier = Modifier
                            .width(130.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == category) Color(0xFFC4D96C) else Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val icon = when (category) {
                                "Breakfast" -> Icons.Default.BreakfastDining
                                "Lunch" -> Icons.Default.LunchDining
                                "Dinner" -> Icons.Default.DinnerDining
                                else -> Icons.Default.BreakfastDining
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = category,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "$selectedCategory Recipes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeCard(recipe = recipe, onClick = { onRecipeSelected(recipe) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                        append("Popular ")
                    }
                    withStyle(style = SpanStyle(color = Color.Gray, fontWeight = FontWeight.Bold)) {
                        append("Recipes")
                    }
                },
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                popularRecipes.forEach { recipe ->
                    PopularRecipeCard(recipe = recipe, onClick = { onRecipeSelected(recipe) })
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(245.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.5.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = recipe.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = recipe.ingredients.joinToString(", "), fontSize = 12.sp)
            Text(
                text = "${recipe.calories} Kcal",
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PopularRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = recipe.name,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = recipe.ingredients.joinToString(" & "),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${recipe.calories} Kcal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = recipe.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class Recipe(
    val name: String,
    val imageRes: Int,
    val ingredients: List<String>,
    val calories: Int,
    val nutrition: Map<String, Int>,
    val preparationSteps: List<String>
)

fun getRecipesByCategory(category: String): List<Recipe> {
    return when (category) {
        "Breakfast" -> listOf(
            Recipe("Keto Salad", R.drawable.keto_salad, listOf("Beans", "Mandarin", "Avocado", "Olive Oil"), 370,
                mapOf("Calories" to 370, "Vitamin" to 35, "Protein" to 6),
                listOf("Chop beans, spinach, cucumber, avocado", "Drizzle olive oil and mandarine", "Sprinkle salt, pepper and seeds." , "Mix well and serve fresh")),
            Recipe("Fruit Salad", R.drawable.fruit_salad, listOf("Apple", "Banana", "Mango", "Berries", "Yogurt" ), 250,
                mapOf("Calories" to 250, "Vitamin" to 20, "Protein" to 4),
                listOf("Dice apples, bananas, mangoes and berries", "Mix with yogurt", "Squeeze fresh orange juice", "Mix well and serve fresh")),
            Recipe("Oatmeal Bowl", R.drawable.oatmeal_bowl, listOf("Oats", "Milk", "Honey", "Fruits", "Chia Seeds" ), 300,
                mapOf("Calories" to 300, "Vitamin" to 15, "Protein" to 8),
                listOf("Boil oats in milk", "Add sliced fruits", "Drizzle honey", "Stir well and sprinkle chia seeds", "Serve warm")),
            Recipe("Avocado Toast", R.drawable.avocado_toast, listOf("Avocado", "Bread", "Egg", "Tomato", "Salt", "Pepper"), 320,
                mapOf("Calories" to 320, "Vitamin" to 25, "Protein" to 10),
                listOf("Mash ripe avocado smoothly", "Toast bread slices", "Spread avocado on toast and add egg", "Add tomato, salt and pepper", "Serve fresh"))
        )
        "Lunch" -> listOf(
            Recipe("Skewers Salad", R.drawable.skewers_salad, listOf("Chicken", "Cherry Tomatoes", "Mozzarella", "Cucumber", "Olives"), 580,
                mapOf("Calories" to 580, "Vitamin" to 40, "Protein" to 25),
                listOf("Skewer cherry tomatoes and mozzarella", "Add cucumber and olives", "Drizzle balsamic vinegar and oil", "Sprinkle salt and pepper lightly", "Arrange on greens and serve")),
            Recipe("Grilled Veggies", R.drawable.grilled_veggies, listOf("Zucchini", "Bell Pepper","Eggplant" ,"Olive Oil"), 400,
                mapOf("Calories" to 400, "Vitamin" to 30, "Protein" to 5),
                listOf("Slice zucchini, bellpeppers and eggplant", "Brush with olive oil", "Grill until charred, tender", "Season with salt, herbs", "Serve warm or chilled")),
            Recipe("Tuna Salad", R.drawable.tuna_salad, listOf("Tuna", "Lettuce", "Mayo", "Onion", "Celery"), 450,
                mapOf("Calories" to 450, "Vitamin" to 25, "Protein" to 20),
                listOf("Drain canned tuna well", "Mix with celery and onion", "Add mayo and lemon juice", "Season with salt and pepper", "Serve on lettuce leaves")),
            Recipe("Quinoa Bowl", R.drawable.quinoa_bowl, listOf("Quinoa", "Sweet Potatoes", "Kale", "Avacado"), 380,
                mapOf("Calories" to 380, "Vitamin" to 35, "Protein" to 15),
                listOf("Cook quinoa in broth", "Add roasted sweet potatoes", "Mix in kale and avocado", "Drizzle tahini and lemon dressing", "Top with seeds and serve"))
        )
        "Dinner" -> listOf(
            Recipe("Cavolo Nero Salad", R.drawable.cavolo_nero, listOf("Cavolo Nero", "Parmesan", "Lemon Juice", "Pine Nuts"), 230,
                mapOf("Calories" to 230, "Vitamin" to 20, "Protein" to 5),
                listOf("Chop cavolo nero finely", "Massage with lemon juice", "Add parmesan and pine nuts", "Drizzle olive oil and salt", "Toss and serve immediately")),
            Recipe("Salmon Plate", R.drawable.salmon_plate, listOf("Salmon", "Cucumber", "Raddish", "Avacado", "Lemon", "Green Vegetables"), 500,
                mapOf("Calories" to 500, "Vitamin" to 40, "Protein" to 30),
                listOf("Bake salmon with herbs", "Slice cucumber and radish thinly", "Add mixed greens and avocado", "Drizzle lemon and olive oil", "Serve with lemon wedges")),
            Recipe("Stuffed Peppers", R.drawable.stuffed_peppers, listOf("Bell Pepper", "Quinoa", "Black Beans"), 420,
                mapOf("Calories" to 420, "Vitamin" to 25, "Protein" to 12),
                listOf("Halve and deseed bell peppers", "Mix quinoa and black beans", "Stuff peppers with mixture", "Bake until peppers soften", "Garnish and serve")),
            Recipe("Roasted Veggies", R.drawable.roasted_veggies, listOf("Carrot", "Potato", " Broccoli", "Cauliflower", "Rosemary"), 350,
                mapOf("Calories" to 350, "Vitamin" to 30, "Protein" to 6),
                listOf("Chop carrots,potatoes, broccoli and cauliflower", "Toss with olive oil", "Season with salt", "Roast until golden brown", "Garnish with Rosemary and Serve warm"))
        )
        "Snacks" -> listOf(
            Recipe(
                "Microwave Popcorn", R.drawable.popcorn,
                listOf("6 Cups Popcorn", "Salt", "Olive Oil"), 100,
                mapOf("Calories" to 100, "Vitamin" to 0, "Protein" to 3),
                listOf("Place popcorn in microwave-safe bowl", "Add oil and salt", "Microwave 2-3 mins until popped")
            ),
            Recipe(
                "Baked Apple", R.drawable.baked_apple,
                listOf("1 Apple", "Cinnamon", "Honey"), 120,
                mapOf("Calories" to 120, "Vitamin" to 10, "Protein" to 1),
                listOf("Core the apple", "Drizzle honey and cinnamon", "Bake at 180°C for 15 mins")
            ),
            Recipe(
                "Crackers With Cheese", R.drawable.crackers_cheese,
                listOf("3 Crackers", "Cheddar Cheese"), 150,
                mapOf("Calories" to 150, "Vitamin" to 5, "Protein" to 6),
                listOf("Place cheese slices on crackers", "Serve as is or heat lightly")
            ),
            Recipe(
                "Fourteen Almonds", R.drawable.almonds,
                listOf("14 Almonds"), 100,
                mapOf("Calories" to 100, "Vitamin" to 2, "Protein" to 4),
                listOf("Serve almonds in a small bowl", "Enjoy as a quick snack")
            ),
            Recipe(
                "Baby Carrots with Hummus", R.drawable.baby_carrots_hummus,
                listOf("8 Baby Carrots", "2 tbsp Hummus"), 80,
                mapOf("Calories" to 80, "Vitamin" to 15, "Protein" to 2),
                listOf("Wash and peel baby carrots if needed", "Serve with a side of hummus for dipping")
            )

        )

        else -> emptyList()
    }
}

fun getPopularRecipes(): List<Recipe> {
    val allCategories = listOf("Breakfast", "Lunch", "Dinner", "Snacks" )

    val allRecipes = allCategories.flatMap { getRecipesByCategory(it) }

    return allRecipes.filter {
        it.name == "Avocado Toast" || it.name == "Roasted Veggies" || it.name == "Baby Carrots with Hummus" || it.name == "Tuna Salad"
    }
}


@Preview(showBackground = true)
@Composable
fun HealthyRecipesScreenPreview() {
    SmartLifeTheme {
        HealthyRecipesScreen(
            onRecipeSelected = {},
            onHomeClicked = {},
            onCalendarClicked = {},
            onRecipesClicked = {}
        )
    }
}
