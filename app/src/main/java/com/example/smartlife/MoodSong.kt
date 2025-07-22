package com.example.smartlife.screen.mood

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlife.R
import com.example.smartlife.data.Song
import com.example.smartlife.data.SongDatabaseHelper
import com.example.smartlife.ui.components.BottomNavigationBar

private const val TAG = "MoodSongsScreen"

val moodList = listOf(
    "Happy" to R.drawable.happy,
    "Sad" to R.drawable.sad,
    "Romantic" to R.drawable.romantic,
    "Stressed" to R.drawable.stressed,
    "Tired" to R.drawable.tired,
    "Angry" to R.drawable.angry
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodSongsScreen(
    onHomeClicked: () -> Unit,
    onCalendarClicked: () -> Unit,
    onRecipesClicked: () -> Unit,
    onVoiceClicked: () -> Unit,
    onMoodSongsClicked: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { SongDatabaseHelper(context) }
    var selectedMood by remember { mutableStateOf<String?>(null) }

    Log.d(TAG, "MoodSongsScreen recomposed. Selected mood: $selectedMood")

    Scaffold(
        topBar = {
            if (selectedMood != null) {
                TopAppBar(
                    title = { Text("For Your Mood: $selectedMood") },
                    navigationIcon = {
                        IconButton(onClick = { selectedMood = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                onCalendarClicked = onCalendarClicked,
                onHomeClicked = onHomeClicked,
                onRecipesClicked = onRecipesClicked,
                onVoiceClicked = onVoiceClicked,
                onMoodSongsClicked = onMoodSongsClicked,
                initialSelectedItem = 4
            )
        },
        containerColor = Color(0xFFF7F8FC)
    ) { paddingValues ->
        if (selectedMood == null) {
            MoodSelectionContent(
                modifier = Modifier.padding(paddingValues),
                onMoodSelected = { mood ->
                    Log.d(TAG, "Mood selected: $mood. Navigating to detail view.")
                    selectedMood = mood
                }
            )
        } else {
            MoodDetailContent(
                modifier = Modifier.padding(paddingValues),
                mood = selectedMood!!,
                dbHelper = dbHelper
            )
        }
    }
}

@Composable
fun MoodSelectionContent(modifier: Modifier = Modifier, onMoodSelected: (String) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 1)
    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) -1
            else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: -1
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF9FBFF), Color.White)
                )
            )
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "How Are You Feeling Today?",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Select your mood to get personalized songs and activities 🎵",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            contentPadding = PaddingValues(horizontal = (LocalContext.current.resources.displayMetrics.widthPixels / 2 / LocalContext.current.resources.displayMetrics.density - 50).dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(moodList) { index, (mood, imageRes) ->
                MoodItem(
                    mood = mood,
                    imageRes = imageRes,
                    isSelected = index == centerItemIndex,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun MoodDetailContent(
    modifier: Modifier = Modifier,
    mood: String,
    dbHelper: SongDatabaseHelper
) {
    val songs = remember(mood) { dbHelper.getSongsByMood(mood) }
    val activities = remember(mood) { dbHelper.getActivitiesByMood(mood) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text("Suggested Songs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(songs) { song ->
            SongItem(song)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (activities.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Suggested Activities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(activities) { activity ->
                ActivityItem(activity)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun MoodItem(mood: String, imageRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 0.9f, label = "scale")
    val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.5f, label = "alpha")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3FB)),
            elevation = CardDefaults.cardElevation(if (isSelected) 12.dp else 4.dp),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = mood,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            mood,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF0F67FE) else Color.Gray
        )
    }
}

@Composable
fun SongItem(song: Song) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(song.name, modifier = Modifier.weight(1f), color = Color.Black, fontWeight = FontWeight.Medium)
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.link))
                    context.startActivity(intent)
                },
                modifier = Modifier.background(Color(0xFF0F67FE), CircleShape)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = activity,
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
    }
}
