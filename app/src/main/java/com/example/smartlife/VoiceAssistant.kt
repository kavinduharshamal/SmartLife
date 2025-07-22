package com.example.smartlife.screen.voiceassistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.smartlife.ui.components.BottomNavigationBar
import kotlinx.coroutines.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp

private const val TAG = "VoiceAssistant"
private const val openAiKey = "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"
private const val elevenApiKey = "ds"
private const val elevenVoiceId = "ds"

@Composable
fun VoiceAssistantScreen(
    onHomeClicked: () -> Unit,
    onCalendarClicked: () -> Unit,
    onRecipesClicked: () -> Unit,
    onVoiceClicked: () -> Unit,
    onMoodSongsClicked: () -> Unit,
    ) {
    val context = LocalContext.current
    val client = remember { OkHttpClient() }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val isListeningState = remember { mutableStateOf(false) }
    val isAssistantSpeaking = remember { mutableStateOf(false) }
    val isProcessingState = remember { mutableStateOf(false) }

    val currentResponseText = remember { mutableStateOf("Hello! I'm your assistant. Tap the mic to start.") }
    val currentHighlightedWordIndex = remember { mutableStateOf(-1) }
    var highlightJob by remember { mutableStateOf<Job?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Microphone permission granted.")
            isListeningState.value = true
        } else {
            Log.d(TAG, "Microphone permission denied.")
            Toast.makeText(context, "Microphone permission is required", Toast.LENGTH_LONG).show()
        }
    }

    val speechRecognizerManager = remember {
        SpeechRecognizerManager(
            context = context,
            onResult = { text ->
                Log.d(TAG, "Speech recognized: $text")
                isListeningState.value = false
                isProcessingState.value = true
                getChatResponse(client, text) { response ->
                    textToSpeech(context, client, response,
                        onStart = {
                            isAssistantSpeaking.value = true
                            currentResponseText.value = response
                            currentHighlightedWordIndex.value = -1
                        },
                        onComplete = {
                            isAssistantSpeaking.value = false
                            isProcessingState.value = false
                            currentHighlightedWordIndex.value = -1
                            highlightJob?.cancel()
                        },
                        onHighlightJobCreated = { job -> highlightJob = job },
                        setMediaPlayer = { mp -> mediaPlayer = mp },
                        updateHighlightedIndex = { index -> currentHighlightedWordIndex.value = index }
                    )
                }
            },
            onError = { error ->
                Log.e(TAG, "Speech recognition error: $error")
                isListeningState.value = false
                isProcessingState.value = false
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    LaunchedEffect(isListeningState.value) {
        if (isListeningState.value) {
            Log.d(TAG, "Starting to listen...")
            speechRecognizerManager.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing VoiceAssistantScreen")
            mediaPlayer?.release()
            speechRecognizerManager.destroy()
            highlightJob?.cancel()
        }
    }

    // This Box provides the background gradient for the entire screen
    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground()
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    onCalendarClicked = onCalendarClicked,
                    onHomeClicked = onHomeClicked,
                    onRecipesClicked = onRecipesClicked,
                    onVoiceClicked = onVoiceClicked,
                    onMoodSongsClicked = onMoodSongsClicked,
                    initialSelectedItem = 3
                )
            },
            containerColor = Color.Transparent // Make Scaffold transparent
        ) { paddingValues ->
            VoiceAssistantUI(
                modifier = Modifier.padding(paddingValues),
                isListening = isListeningState.value,
                isSpeaking = isAssistantSpeaking.value,
                isProcessing = isProcessingState.value,
                assistantResponse = currentResponseText.value,
                highlightedWordIndex = currentHighlightedWordIndex.value,
                onMicClick = {
                    Log.d(TAG, "Mic button clicked.")
                    if (!isAssistantSpeaking.value && !isProcessingState.value) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            isListeningState.value = true
                        } else {
                            Log.d(TAG, "Requesting microphone permission.")
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        Log.d(TAG, "Mic click ignored: assistant is speaking or processing.")
                    }
                }
            )
        }
    }
}

private fun getChatResponse(client: OkHttpClient, userInput: String, callback: (String) -> Unit) {
    Log.d(TAG, "Sending to OpenAI: '$userInput'")
    val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    val body = """
        {
          "model": "gpt-4",
          "messages": [
            {"role": "system", "content": "Respond in less than 40 words."},
            {"role": "user", "content": "$userInput"}
          ],
          "max_tokens": 60
        }
        """.trimIndent().toRequestBody(jsonMediaType)


    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .addHeader("Authorization", "Bearer $openAiKey")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, "OpenAI API call failed: ${e.message}")
            callback("Error: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string().let { responseBody ->
                if (response.isSuccessful && responseBody != null) {
                    Log.d(TAG, "OpenAI response: $responseBody")
                    try {
                        val reply = JSONObject(responseBody)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        callback(reply)
                    } catch (e: Exception) {
                        Log.e(TAG, "JSON parsing error: ${e.message}")
                        callback("Parsing error: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "OpenAI API error: ${response.code} ${response.message}")
                    callback("API error: ${response.code}")
                }
            }
        }
    })
}

private fun textToSpeech(
    context: Context,
    client: OkHttpClient,
    text: String,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onHighlightJobCreated: (Job) -> Unit,
    setMediaPlayer: (MediaPlayer) -> Unit,
    updateHighlightedIndex: (Int) -> Unit
) {
    Log.d(TAG, "Sending to ElevenLabs for TTS: '$text'")
    onStart()
    val body = """
        {
          "model_id": "eleven_monolingual_v1",
          "text": "$text",
          "voice_settings": {"stability": 0.5, "similarity_boost": 0.5}
        }
    """.trimIndent().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("https://api.elevenlabs.io/v1/text-to-speech/$elevenVoiceId")
        .addHeader("xi-api-key", elevenApiKey)
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, "ElevenLabs API call failed: ${e.message}")
            onComplete()
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                Log.e(TAG, "ElevenLabs API error: ${response.code} ${response.message}")
                onComplete()
                return
            }

            val bytes = response.body?.bytes()
            if (bytes != null) {
                Log.d(TAG, "Received ${bytes.size} bytes of audio data.")
                val tempFile = File.createTempFile("voice", ".mp3", context.cacheDir)
                tempFile.writeBytes(bytes)

                val mp = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        Log.d(TAG, "MediaPlayer prepared, starting playback.")
                        start()
                        val words = text.split(" ")
                        if (words.isNotEmpty()) {
                            val timePerWordMs = duration.toLong().coerceAtLeast(1) / words.size.toLong().coerceAtLeast(1)
                            val job = CoroutineScope(Dispatchers.Main).launch {
                                for (i in words.indices) {
                                    if (!isActive) break
                                    updateHighlightedIndex(i)
                                    delay(timePerWordMs)
                                }
                            }
                            onHighlightJobCreated(job)
                        }
                    }
                    setOnCompletionListener {
                        Log.d(TAG, "MediaPlayer playback completed.")
                        tempFile.delete()
                        updateHighlightedIndex(-1)
                        onComplete()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        onComplete()
                        true
                    }
                    prepareAsync()
                }
                setMediaPlayer(mp)
            } else {
                Log.e(TAG, "ElevenLabs response body is null.")
                onComplete()
            }
        }
    })
}


class SpeechRecognizerManager(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val recognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onError(error: Int) {
                onError("Speech error code: $error")
            }

            override fun onResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!result.isNullOrBlank()) onResult(result) else onError("Empty result")
            }
        })
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        recognizer.startListening(intent)
    }

    fun stopListening() = recognizer.stopListening()
    fun destroy() = recognizer.destroy()
}

@Composable
fun AnimatedMicButton(
    isListening: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    val baseColor = Color(0xFF42A5F6)
    val colors = listOf(
        baseColor.copy(alpha = 0.2f),
        baseColor.copy(alpha = 0.4f),
        baseColor.copy(alpha = 0.6f),
        baseColor
    )

    val sizes = listOf(200.dp, 160.dp, 120.dp, 80.dp)
    val durations = listOf(1000, 800, 600, 400)

    val transitions = sizes.indices.map { index ->
        rememberInfiniteTransition(label = "Box$index")
    }

    val scales = transitions.mapIndexed { index, transition ->
        transition.animateFloat(
            initialValue = 1f,
            targetValue = if (isListening || isSpeaking) 1.2f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durations[index],
                    easing = LinearEasing,
                    delayMillis = index * 100
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Scale$index"
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        sizes.indices.forEach { index ->
            Box(
                modifier = Modifier
                    .size(sizes[index])
                    .graphicsLayer(
                        scaleX = scales[index].value,
                        scaleY = scales[index].value
                    )
                    .background(
                        color = colors[index],
                        shape = CircleShape
                    )
            )
        }

        Button(
            onClick = onClick,
            enabled = !isSpeaking,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            shape = CircleShape,
            modifier = Modifier.size(80.dp),
            content = {}
        )
    }
}


@Composable
fun HighlightedText(
    text: String,
    highlightedWordIndex: Int,
    modifier: Modifier = Modifier
) {
    val words = remember(text) { text.split(" ") }

    val annotatedString = buildAnnotatedString {
        words.forEachIndexed { index, word ->
            when {
                index == highlightedWordIndex -> {
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            background = Color(0xFF242E49),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    ) {
                        append(word)
                    }
                }
                else -> {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF9EA7B8),
                            fontSize = 23.sp
                        )
                    ) {
                        append(word)
                    }
                }
            }

            if (index < words.size - 1) {
                append(" ")
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        fontSize = 23.sp,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun GradientBackground() {
    val colorPairs = listOf(
        Color(0xFF7986CB) to Color(0xFFE8EAF6),
        Color(0xFF64B5F6) to Color(0xFFE3F2FD),
        Color(0xFF4FC3F7) to Color(0xFFF1F8FF),
        Color(0xFF90CAF9) to Color(0xFFFFFFFF)
    )

    var currentIndex by remember { mutableStateOf(0) }
    val nextIndex = (currentIndex + 1) % colorPairs.size

    val transition = rememberInfiniteTransition(label = "ColorWave")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Progress"
    )

    LaunchedEffect(progress) {
        if (progress >= 0.99f) {
            currentIndex = nextIndex
        }
    }

    val topColor = lerp(colorPairs[currentIndex].first, colorPairs[nextIndex].first, progress)
    val bottomColor = lerp(colorPairs[currentIndex].second, colorPairs[nextIndex].second, progress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
    )
}


@Composable
fun VoiceAssistantUI(
    modifier: Modifier = Modifier,
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    assistantResponse: String?,
    highlightedWordIndex: Int,
    onMicClick: () -> Unit
) {
    val statusText = when {
        isSpeaking -> "speaking..."
        isListening -> "listening..."
        isProcessing -> "processing..."
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (statusText.isNotEmpty()) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 25.sp,
                    color = Color(0xFF242E49)

                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            AnimatedMicButton(
                isListening = isListening,
                isSpeaking = isSpeaking,
                onClick = onMicClick
            )

            if (!assistantResponse.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(32.dp))
                HighlightedText(
                    text = assistantResponse,
                    highlightedWordIndex = if (isSpeaking) highlightedWordIndex else -1,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
