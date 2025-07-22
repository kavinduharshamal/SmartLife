package com.example.smartlife

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import com.example.smartlife.ui.theme.SmartLifeTheme


class VoiceAssistant : ComponentActivity() {
    private val client = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var speechRecognizerManager: SpeechRecognizerManager

    private val conversationState = mutableStateOf(listOf<String>())
    private val isListeningState = mutableStateOf(false)
    private val isAssistantSpeaking = mutableStateOf(false)
    private val isProcessingState = mutableStateOf(false)
    private val isSessionActive = mutableStateOf(false)

    // New states for word highlighting
    private val currentResponseText = mutableStateOf("")
    private val currentHighlightedWordIndex = mutableStateOf(-1)
    private var highlightJob: Job? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startSpeechRecognition()
        else Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationState.value = listOf("Assistant: Hello! I'm your assistant. Tap the mic to start.")

        setContent {
            SmartLifeTheme {
                VoiceAssistantScreen(
                    isListening = isListeningState.value,
                    isSpeaking = isAssistantSpeaking.value,
                    isProcessing = isProcessingState.value,
                    assistantResponse = currentResponseText.value,
                    highlightedWordIndex = currentHighlightedWordIndex.value,
                    onMicClick = {
                        if (!isAssistantSpeaking.value) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                isSessionActive.value = true
                                startSpeechRecognition()
                            }
                        }
                    }
                )
            }
        }

        speechRecognizerManager = SpeechRecognizerManager(
            context = this,
            onResult = { text ->
                conversationState.value += "You: $text"
                isListeningState.value = false

                if (text.lowercase().contains("thank you")) {
                    isSessionActive.value = false
                    isProcessingState.value = false
                    return@SpeechRecognizerManager
                }

                isProcessingState.value = true
                getChatResponse(text) { response ->
                    conversationState.value += "Assistant: $response"
                    textToSpeech(response)
                }
            },
            onError = { error ->
                isListeningState.value = false
                isProcessingState.value = false
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )

        textToSpeech("Hello! I'm your assistant. tap button to start.")
    }

    private fun startSpeechRecognition() {
        isListeningState.value = true
        speechRecognizerManager.startListening()
    }

    private fun getChatResponse(userInput: String, callback: (String) -> Unit) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val body = """
            {
                "model": "gpt-4",
                "messages": [{"role": "user", "content": "$userInput"}]
            }
        """.trimIndent().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            //.addHeader("Authorization", "Bearer $openAiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@VoiceAssistant, "OpenAI error: ${e.message}", Toast.LENGTH_SHORT).show()
                    isProcessingState.value = false
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    try {
                        val reply = JSONObject(responseBody)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        callback(reply)
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@VoiceAssistant, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                            isProcessingState.value = false
                        }
                    }
                }
            }
        })
    }

    private fun textToSpeech(text: String) {
        isAssistantSpeaking.value = true
        currentResponseText.value = text
        currentHighlightedWordIndex.value = -1

        val body = """
            {
              "model_id": "eleven_monolingual_v1",
              "text": "$text",
              "voice_settings": {"stability": 0.5, "similarity_boost": 0.5}
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            //.url("https://api.elevenlabs.io/v1/text-to-speech/$elevenVoiceId")
            //.addHeader("xi-api-key", elevenApiKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@VoiceAssistant, "TTS error: ${e.message}", Toast.LENGTH_SHORT).show()
                    isAssistantSpeaking.value = false
                    isProcessingState.value = false
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bytes = response.body?.bytes() ?: return
                val tempFile = File.createTempFile("voice", ".mp3", cacheDir)
                tempFile.writeBytes(bytes)

                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        start()
                        // Start word highlighting when audio starts playing
                        startWordHighlighting(text, duration)
                    }
                    setOnCompletionListener {
                        tempFile.delete()
                        isAssistantSpeaking.value = false
                        isProcessingState.value = false
                        currentHighlightedWordIndex.value = -1
                        highlightJob?.cancel()

                        if (isSessionActive.value) {
                            startSpeechRecognition()
                        }
                    }

                    prepareAsync()
                }
            }
        })
    }

    private fun startWordHighlighting(text: String, audioDurationMs: Int) {
        val words = text.split(" ")
        if (words.isEmpty()) return

        // Calculate approximate time per word
        val timePerWordMs = audioDurationMs / words.size.toLong()

        highlightJob?.cancel()
        highlightJob = CoroutineScope(Dispatchers.Main).launch {
            for (i in words.indices) {
                if (!isActive) break
                currentHighlightedWordIndex.value = i
                delay(timePerWordMs)
            }
            currentHighlightedWordIndex.value = -1
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        speechRecognizerManager.stopListening()
        highlightJob?.cancel()
        super.onDestroy()
    }
}

// SpeechRecognizerManager remains unchanged
class SpeechRecognizerManager(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

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

    fun stopListening() {
        recognizer.stopListening()
        recognizer.destroy()
    }
}

@Composable
fun AnimatedMicButton(
    isListening: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    val baseColor = Color(0xFF42A5F6) // Blue-ish
    val colors = listOf(
        baseColor.copy(alpha = 0.2f),
        baseColor.copy(alpha = 0.4f),
        baseColor.copy(alpha = 0.6f),
        baseColor
    )

    val sizes = listOf(200.dp, 160.dp, 120.dp, 80.dp)
    val durations = listOf(1000, 800, 600, 400) // Different durations for wave effect

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
                    delayMillis = index * 100 // phase shift for wave effect
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
                    // Highlighted: white text, dark background, bold, larger size
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            background = Color(0xFF242E49),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp // Larger size for emphasis
                        )
                    ) {
                        append(word)
                    }
                }
                else -> {
                    // Default assistant response color
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
        fontSize = 23.sp, // Base size (used by default if not overridden)
        modifier = modifier.fillMaxWidth()
    )
}



@Composable
fun VoiceAssistantScreen(
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

    // Color pairs for wave-like transitions
    val colorPairs = listOf(
        Color(0xFF7986CB) to Color(0xFFE8EAF6),
        Color(0xFF64B5F6) to Color(0xFFE3F2FD),
        Color(0xFF4FC3F7) to Color(0xFFF1F8FF),
        Color(0xFF90CAF9) to Color(0xFFFFFFFF)
    )

    // State to track current and next color pair
    var currentIndex by remember { mutableStateOf(0) }
    val nextIndex = (currentIndex + 1) % colorPairs.size

    // Animate progress between the current and next color
    val transition = rememberInfiniteTransition(label = "ColorWave")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Fast color wave every 1.5s
            repeatMode = RepeatMode.Restart
        ),
        label = "Progress"
    )

    // When progress reaches ~100%, update color index
    LaunchedEffect(progress) {
        if (progress >= 0.99f) {
            currentIndex = nextIndex
        }
    }

    // Interpolated gradient colors
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