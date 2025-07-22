package com.example.smartlife

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartlife.screen.recipes.Recipe
import com.example.smartlife.ui.theme.SmartLifeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipe: Recipe, onBackClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .background(color = Color.White)
        ) {
            Column(modifier = Modifier.padding(start = 30.dp)) {
            Text(text = "Nutritions", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 10.dp, top = 15.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    recipe.nutrition.forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(8.dp, RoundedCornerShape(50))
                                .background(Color.White, RoundedCornerShape(50))
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDAED84)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = value.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = label,
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                            )
                        }

                    }
                }

                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(32.dp))
                )

            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Ingredients", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(start = 10.dp)) {
                recipe.ingredients.forEach { ingredient ->
                    Text("$ingredient", fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Recipe Preparation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(start = 10.dp)) {
                recipe.preparationSteps.forEach { step ->
                    Text(
                        text = step,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }}
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    SmartLifeTheme {
        RecipeDetailScreen(
            recipe = Recipe(
                name = "Keto Salad",
                imageRes = R.drawable.keto_salad,
                ingredients = listOf(
                    "\u00bd cup extra-virgin olive oil",
                    "\u00bc cup lime juice",
                    "1 avocado",
                    "\u00bd teaspoon salt",
                    "\u00bd teaspoon freshly ground pepper",
                    "Pinch of minced garlic"
                ),
                calories = 370,
                nutrition = mapOf("Calories" to 370, "Vitamin" to 35, "Protein" to 6),
                preparationSteps = listOf(
                    "Chop all the ingredients to the size you want.",
                    "Add olive oil, garlic and lemon.",
                    "Mix them all in a large bowl"
                )
            ),
            onBackClicked = {}
        )
    }
}