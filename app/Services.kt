#package com.samveda.app

import android.content.ActivityNotFoundException
        import android.content.Context
        import android.content.Intent
        import android.net.ConnectivityManager
        import android.net.NetworkCapabilities
        import android.net.Uri
        import android.os.Build
        import android.os.Bundle
        import android.os.Handler
        import android.os.Looper
        import android.os.VibrationEffect
        import android.os.Vibrator
        import android.os.VibratorManager
        import android.speech.RecognitionListener
        import android.speech.RecognizerIntent
        import android.speech.SpeechRecognizer
        import android.speech.tts.TextToSpeech
        import android.widget.Toast
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.setValue
        import java.util.Locale

/** User preferences, saved on the device only. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("samveda_prefs", Context.MODE_PRIVATE)

    var language by mutableStateOf(sp.getString("lang", "en-IN") ?: "en-IN")
        private set
    var rate by mutableStateOf(sp.getFloat("rate", 1.0f))
        private set
    var vibration by mutableStateOf(sp.getBoolean("vibration", true))
        private set
    var dark by mutableStateOf(sp.getBoolean("dark", true))
        private set
    var contact by mutableStateOf(sp.getString("contact", "") ?: "")
        private set

    fun setLanguage(v: String) { language = v; sp.edit().putString("lang", v).apply() }
    fun setRate(v: Float) { rate = v; sp.edit().putFloat("rate", v).apply() }
    fun setVibration(v: Boolean) { vibration = v; sp.edit().putBoolean("vibration", v).apply() }
    fun setDark(v: Boolean) { dark = v; sp.edit().putBoolean("dark", v).apply() }
    fun setContact(v: String) { contact = v; sp.edit().putString("contact", v).apply() }
}

/** Wrapper around Android's built-in on-device Text-to-Speech engine. */
class Speaker(context: Context) {
    private var ready = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
    }

    /** Returns an error message, or null if speech started. */
    fun speak(text: String, languageTag: String, rate: Float): String? {
        val engine = tts ?: return "Speech engine not available on this device."
        if (!ready) return "Speech engine is still starting. Please try again."
        val result = engine.setLanguage(Locale.forLanguageTag(languageTag))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            return "Voice for this language is not installed. Install it in Android Settings > Text-to-speech."
        }
        engine.setSpeechRate(rate)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "samveda_utterance")
        return null
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/** Vibration patterns (respects the user's vibration preference). */
class Haptics(context: Context, private val prefs: Prefs) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            legacyVibrator(context)
        }

    @Suppress("DEPRECATION")
    private fun legacyVibrator(context: Context): Vibrator? =
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private fun play(pattern: LongArray) {
        if (!prefs.vibration) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun tap() = play(longArrayOf(0, 30))
    fun left() = play(longArrayOf(0, 80, 80, 80))
    fun right() = play(longArrayOf(0, 300, 100, 300))
    fun stopAlert() = play(longArrayOf(0, 800))
}

/**
 * Continuous speech-to-text using Android's SpeechRecognizer.
 * It asks for offline recognition. It works fully offline only when the
 * device has the offline language pack installed.
 */
class LiveListener(
    private val context: Context,
    private val onPartial: (String) -> Unit,
    private val onFinal: (String) -> Unit,
    private val onState: (String) -> Unit,
    private val onStopped: () -> Unit
) {
    private var recognizer: SpeechRecognizer? = null
    private var active = false
    private var lang = "en-IN"
    private val handler = Handler(Looper.getMainLooper())

    fun start(languageTag: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onState("No speech recognition service was found on this device.")
            onStopped()
            return
        }
        lang = languageTag
        active = true
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }
        listen()
    }

    fun stop() {
        active = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        onPartial("")
        onStopped()
    }

    private fun listen() {
        if (!active) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        recognizer?.startListening(intent)
    }

    private fun restart(delayMs: Long) {
        handler.postDelayed({ listen() }, delayMs)
    }

    private fun fail(message: String) {
        active = false
        onState(message)
        recognizer?.destroy()
        recognizer = null
        onStopped()
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) onPartial(text)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) onFinal(text)
            if (active) restart(300)
        }

        override fun onError(error: Int) {
            if (!active) return
            when (error) {
                6, 7 -> restart(300)          // timeout / no match: keep listening
                5, 8 -> restart(1000)         // client / busy: retry shortly
                9 -> fail("Microphone permission is missing.")
                12, 13 -> fail("This language is not available for offline recognition. Download its offline speech pack in Google app settings.")
                1, 2, 4, 11 -> fail("Speech recognition needs an offline language pack or internet (error $error).")
                else -> fail("Speech recognition stopped (error $error).")
            }
        }
    }
}

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private fun cleanNumber(number: String): String = number.filter { it.isDigit() || it == '+' }

/** Opens the SMS app with a pre-filled message. The user must press Send. */
fun openSms(context: Context, number: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + cleanNumber(number)))
    intent.putExtra("sms_body", body)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No SMS app found.", Toast.LENGTH_LONG).show()
    }
}

/** Opens the phone dialer with the number filled in. The user must press Call. */
fun openDialer(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cleanNumber(number)))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No dialer app found.", Toast.LENGTH_LONG).show()
    }
}


