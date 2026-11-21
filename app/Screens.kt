package com.samveda.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// ---------------------------------------------------------------- shared UI

val languages = listOf(
    "en-IN" to "English",
    "te-IN" to "తెలుగు (Telugu)",
    "hi-IN" to "हिन्दी (Hindi)"
)

@Composable
fun ScreenFrame(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onBack, modifier = Modifier.heightIn(min = 56.dp)) {
            Text("← Back", fontSize = 20.sp)
        }
        Text(title, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(subtitle, fontSize = 20.sp)
        content()
    }
}

@Composable
fun BigButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun InfoCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            lines.forEach { Text("• $it", fontSize = 17.sp) }
        }
    }
}

@Composable
fun ModuleCard(emoji: String, name: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClickLabel = "Open $name") { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 44.sp)
                Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(subtitle, fontSize = 16.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

// --------------------------------------------------------------------- home

@Composable
fun HomeScreen(onOpen: (String) -> Unit, onSos: () -> Unit) {
    val context = LocalContext.current
    val online = remember { isOnline(context) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("SAMVEDA", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("One Device. Multiple Abilities. One Connected World.", fontSize = 18.sp)
        Text(
            if (online) "● Offline-first app. Network is available but not required for implemented features."
            else "● Offline mode. Implemented features run on this device.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModuleCard("👁️", "SPARSHA", "Visual Assistance", Modifier.weight(1f)) { onOpen("sparsha") }
            ModuleCard("🤟", "DRISHTI", "Sign Communication", Modifier.weight(1f)) { onOpen("drishti") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModuleCard("👂", "SHRAVAN", "Hearing Assistance", Modifier.weight(1f)) { onOpen("shravan") }
            ModuleCard("🧭", "SAHAYA", "Daily Assistance", Modifier.weight(1f)) { onOpen("sahaya") }
        }
        Button(
            onClick = onSos,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
        ) {
            Text("🆘  SOS EMERGENCY", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { onOpen("settings") },
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            ) { Text("Settings", fontSize = 18.sp) }
            OutlinedButton(
                onClick = { onOpen("status") },
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            ) { Text("Feature status", fontSize = 18.sp) }
        }
    }
}

@Composable
fun SosDialog(prefs: Prefs, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val hasContact = prefs.contact.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SOS Emergency", fontSize = 24.sp) },
        text = {
            Text(
                if (hasContact) "Open a pre-filled emergency SMS to ${prefs.contact}? You must press Send yourself, nothing is sent automatically."
                else "No trusted contact is saved yet. You can add one in SAHAYA. You can still call 112.",
                fontSize = 18.sp
            )
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasContact) {
                    Button(onClick = {
                        openSms(context, prefs.contact, "EMERGENCY: I need help. Sent from SAMVEDA.")
                        onDismiss()
                    }) { Text("Prepare SMS", fontSize = 18.sp) }
                }
                Button(
                    onClick = { openDialer(context, "112"); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White)
                ) { Text("Call 112", fontSize = 18.sp) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 18.sp) } }
    )
}

// ------------------------------------------------------- camera + OCR panel

@Composable
fun OcrPanel(prefs: Prefs, speaker: Speaker, haptics: Haptics, buttonLabel: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    var result by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Point the camera at printed text, then tap the button.") }
    var busy by remember { mutableStateOf(false) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    DisposableEffect(Unit) {
        onDispose { recognizer.close() }
    }
    DisposableEffect(granted) {
        onDispose {
            if (granted) {
                try {
                    ProcessCameraProvider.getInstance(context).get().unbindAll()
                } catch (e: Exception) {
                    // camera already released
                }
            }
        }
    }

    if (!granted) {
        InfoCard(
            "Camera permission needed",
            listOf("The camera is used only on this device to read text. Nothing is recorded or uploaded.")
        )
        BigButton("Allow camera") { launcher.launch(Manifest.permission.CAMERA) }
        return
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    status = "Camera could not start: ${e.message}"
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )

    Text("📷 Camera is active on this device only. Nothing is saved or sent.", fontSize = 15.sp)

    BigButton(if (busy) "Reading…" else buttonLabel, enabled = !busy) {
        busy = true
        status = "Reading text…"
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = try {
                        image.toBitmap()
                    } catch (e: Exception) {
                        null
                    }
                    val rotation = image.imageInfo.rotationDegrees
                    image.close()
                    if (bitmap == null) {
                        busy = false
                        status = "Could not process the photo."
                        return
                    }
                    recognizer.process(InputImage.fromBitmap(bitmap, rotation))
                        .addOnSuccessListener { r ->
                            busy = false
                            val text = r.text.trim()
                            if (text.isEmpty()) {
                                result = ""
                                status = "No text found. Try better light or move closer."
                                speaker.speak("No text found", "en-IN", prefs.rate)
                            } else {
                                result = text
                                status = "Text found. OCR can make mistakes, please double-check important details."
                                haptics.tap()
                                val err = speaker.speak(text, "en-IN", prefs.rate)
                                if (err != null) status = err
                            }
                        }
                        .addOnFailureListener { e ->
                            busy = false
                            status = "Text recognition failed: ${e.localizedMessage}"
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    busy = false
                    status = "Camera error: ${exception.message}"
                }
            }
        )
    }

    Text(status, fontSize = 17.sp, color = MaterialTheme.colorScheme.secondary)
    if (result.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(result, fontSize = 22.sp, modifier = Modifier.padding(16.dp))
        }
        OutlinedButton(
            onClick = { speaker.speak(result, "en-IN", prefs.rate) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("🔊 Read again", fontSize = 18.sp) }
    }
}

// ------------------------------------------------------------------ SPARSHA

@Composable
fun SparshaScreen(prefs: Prefs, speaker: Speaker, haptics: Haptics, onBack: () -> Unit) {
    ScreenFrame("SPARSHA", "Visual Assistance", onBack) {
        Text("IMPLEMENTED: Read printed text and signboards (English / Latin script).", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        OcrPanel(prefs, speaker, haptics, "Read text in front of camera")

        Text("DEMO: Direction vibration patterns", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("These test the haptic patterns planned for navigation. They are not connected to obstacle detection yet.", fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { haptics.left(); speaker.speak("Left", prefs.language, prefs.rate) },
                modifier = Modifier.weight(1f).heightIn(min = 60.dp)
            ) { Text("Left", fontSize = 18.sp) }
            OutlinedButton(
                onClick = { haptics.right(); speaker.speak("Right", prefs.language, prefs.rate) },
                modifier = Modifier.weight(1f).heightIn(min = 60.dp)
            ) { Text("Right", fontSize = 18.sp) }
            OutlinedButton(
                onClick = { haptics.stopAlert(); speaker.speak("Stop", prefs.language, prefs.rate) },
                modifier = Modifier.weight(1f).heightIn(min = 60.dp)
            ) { Text("Stop", fontSize = 18.sp) }
        }

        InfoCard(
            "PLANNED – not working yet",
            listOf(
                "Object / obstacle detection (needs a YOLO-family model added and tested on iQOO 15)",
                "Distance estimation",
                "Moving-obstacle warnings",
                "Continuous scanning and navigation guidance"
            )
        )
        Text(
            "Safety: this app must never be relied on for collision prevention. Always use your cane, guide, or other mobility aids.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// ------------------------------------------------------------------ DRISHTI

data class Phrase(val label: String, val en: String, val te: String, val hi: String) {
    fun text(tag: String): String = when (tag) {
        "te-IN" -> te
        "hi-IN" -> hi
        else -> en
    }
}

val phrases = listOf(
    Phrase("Hello", "Hello", "నమస్కారం", "नमस्ते"),
    Phrase("Thank you", "Thank you", "ధన్యవాదాలు", "धन्यवाद"),
    Phrase("Please help me", "Please help me", "దయచేసి సహాయం చేయండి", "कृपया मेरी मदद कीजिए"),
    Phrase("I need water", "I need water", "నాకు నీళ్లు కావాలి", "मुझे पानी चाहिए"),
    Phrase("I need a doctor", "I need a doctor", "నాకు డాక్టర్ కావాలి", "मुझे डॉक्टर चाहिए"),
    Phrase("I did not understand", "I did not understand", "నాకు అర్థం కాలేదు", "मुझे समझ नहीं आया"),
    Phrase("Please write it down", "Please write it down", "దయచేసి రాసి చూపించండి", "कृपया लिखकर दिखाइए")
)

@Composable
fun DrishtiScreen(prefs: Prefs, speaker: Speaker, haptics: Haptics, onBack: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    fun say(text: String) {
        message = speaker.speak(text, prefs.language, prefs.rate) ?: ""
    }

    ScreenFrame("DRISHTI", "Sign Communication", onBack) {
        Text("IMPLEMENTED: Type to speak, and quick phrases.", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text("Speaking language: ${languages.firstOrNull { it.first == prefs.language }?.second ?: prefs.language} (change in Settings)", fontSize = 16.sp)

        OutlinedTextField(
            value = typed,
            onValueChange = { typed = it },
            label = { Text("Type your message", fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp),
            minLines = 2
        )
        BigButton("🔊 Speak", enabled = typed.isNotBlank()) { haptics.tap(); say(typed) }
        if (message.isNotEmpty()) Text(message, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)

        Text("Quick phrases", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        phrases.forEach { p ->
            OutlinedButton(
                onClick = { haptics.tap(); say(p.text(prefs.language)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
            ) { Text("${p.label}  —  ${p.text(prefs.language)}", fontSize = 18.sp) }
        }

        InfoCard(
            "PLANNED – not working yet",
            listOf(
                "Indian Sign Language recognition from the front camera (needs a trained ISL model and dataset)",
                "Text-to-sign animation (needs a validated sign-language animation library)",
                "Confidence display and user correction of recognised signs"
            )
        )
        Text("No sign recognition runs in this version. The app does not guess signs.", fontSize = 16.sp)
    }
}

// ------------------------------------------------------------------ SHRAVAN

@Composable
fun ShravanScreen(prefs: Prefs, speaker: Speaker, haptics: Haptics, onBack: () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var listening by remember { mutableStateOf(false) }
    var live by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("") }
    val transcript = remember { mutableStateListOf<String>() }

    val listener = remember {
        LiveListener(
            context = context,
            onPartial = { live = it },
            onFinal = { transcript.add("Them: $it"); live = ""; haptics.tap() },
            onState = { info = it },
            onStopped = { listening = false }
        )
    }
    DisposableEffect(Unit) { onDispose { listener.stop() } }

    fun begin() {
        listening = true
        info = "Listening… The microphone is ON. Audio is not saved."
        listener.start(prefs.language)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        if (it) begin()
    }

    ScreenFrame("SHRAVAN", "Hearing Assistance", onBack) {
        Text("IMPLEMENTED: Live captions and typed replies.", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Captions use Android's speech recognizer in offline-preferred mode. It works fully offline only if the offline speech pack for your language is installed on the phone.",
            fontSize = 15.sp
        )

        if (!listening) {
            BigButton("🎤 Start captions") {
                if (granted) begin() else launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            BigButton("⏹ Stop captions") { listener.stop() }
        }
        if (info.isNotEmpty()) Text(info, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                if (live.isEmpty()) "Live caption appears here…" else live,
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(min = 100.dp)
            )
        }

        Text("Conversation", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (transcript.isEmpty()) Text("No conversation yet.", fontSize = 16.sp)
        transcript.forEach { Text(it, fontSize = 20.sp) }

        OutlinedTextField(
            value = reply,
            onValueChange = { reply = it },
            label = { Text("Type a reply", fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp)
        )
        BigButton("🔊 Speak reply", enabled = reply.isNotBlank()) {
            transcript.add("You: $reply")
            val err = speaker.speak(reply, prefs.language, prefs.rate)
            if (err != null) info = err
            reply = ""
        }
        if (transcript.isNotEmpty()) {
            OutlinedButton(
                onClick = { transcript.clear() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) { Text("Clear conversation", fontSize = 18.sp) }
        }

        InfoCard(
            "PLANNED – not working yet",
            listOf(
                "Detection of alarms, horns, sirens and doorbells (needs a sound-classification model)",
                "Vibration alerts for sound events",
                "Saved transcripts (nothing is stored at present)"
            )
        )
    }
}

// ------------------------------------------------------------------ SAHAYA

@Composable
fun SahayaScreen(prefs: Prefs, speaker: Speaker, haptics: Haptics, onBack: () -> Unit) {
    val context = LocalContext.current
    ScreenFrame("SAHAYA", "Daily Assistance", onBack) {
        Text("IMPLEMENTED: SOS contact", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = prefs.contact,
            onValueChange = { prefs.setContact(it) },
            label = { Text("Trusted contact phone number", fontSize = 16.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp)
        )
        Text(
            "With your consent, the SOS button opens a pre-filled SMS to this number. You press Send. Nothing is sent automatically and your location is not shared.",
            fontSize = 15.sp
        )
        OutlinedButton(
            onClick = {
                if (prefs.contact.isNotBlank()) {
                    openSms(context, prefs.contact, "EMERGENCY: I need help. Sent from SAMVEDA.")
                }
            },
            enabled = prefs.contact.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("Test: prepare SMS", fontSize = 18.sp) }

        Text("IMPLEMENTED: Read documents, medicine labels, signboards", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        OcrPanel(prefs, speaker, haptics, "Read document or label")
        Text(
            "Medicine labels: always confirm the medicine name and dose with a pharmacist or doctor. OCR can be wrong.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.error
        )

        InfoCard(
            "PLANNED – not working yet",
            listOf(
                "Indian banknote recognition (needs a trained currency model)",
                "Door and entrance detection, indoor assistance",
                "Person recognition (will require explicit consent and privacy safeguards)",
                "Sharing location in emergencies"
            )
        )
    }
}

// ----------------------------------------------------------------- SETTINGS

@Composable
fun SettingsScreen(prefs: Prefs, speaker: Speaker, haptics: Haptics, onBack: () -> Unit) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }

    ScreenFrame("Settings", "Language, voice, vibration and theme", onBack) {
        Text("Language (speech and captions)", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        languages.forEach { (tag, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { prefs.setLanguage(tag); haptics.tap() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = prefs.language == tag, onClick = { prefs.setLanguage(tag); haptics.tap() })
                Text(name, fontSize = 20.sp)
            }
        }

        Text("Voice speed: ${"%.1f".format(prefs.rate)}x", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Slider(value = prefs.rate, onValueChange = { prefs.setRate(it) }, valueRange = 0.5f..1.5f)
        BigButton("🔊 Test voice") {
            val sample = phrases.first().text(prefs.language)
            message = speaker.speak(sample, prefs.language, prefs.rate) ?: ""
        }
        if (message.isNotEmpty()) Text(message, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Vibration feedback", fontSize = 20.sp, modifier = Modifier.weight(1f))
            Switch(checked = prefs.vibration, onCheckedChange = { prefs.setVibration(it) })
        }
        OutlinedButton(
            onClick = { haptics.right() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("Test vibration", fontSize = 18.sp) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark theme", fontSize = 20.sp, modifier = Modifier.weight(1f))
            Switch(checked = prefs.dark, onCheckedChange = { prefs.setDark(it) })
        }

        InfoCard(
            "Privacy",
            listOf(
                "Camera and microphone are used only while you are on a screen that needs them.",
                "No audio, images or conversations are recorded, saved or uploaded by SAMVEDA.",
                "Settings and your trusted contact are stored on this phone only.",
                "Telugu and Hindi speech needs those voices installed in Android's text-to-speech settings."
            )
        )
        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("Camera / microphone permissions", fontSize = 18.sp) }
    }
}

// ------------------------------------------------------------------- STATUS

@Composable
fun StatusScreen(onBack: () -> Unit) {
    ScreenFrame("Feature status", "What works today, and what does not", onBack) {
        InfoCard(
            "✅ IMPLEMENTED",
            listOf(
                "Four-module interface, dark/light theme, large touch targets",
                "Settings: language, voice speed, vibration, theme (saved on device)",
                "Text-to-speech (Android on-device engine)",
                "Vibration patterns (left / right / stop demo)",
                "SHRAVAN live captions and typed replies",
                "OCR text reading from camera (English / Latin script, on-device)",
                "SOS: pre-filled SMS to trusted contact and 112 dialer, user confirms"
            )
        )
        InfoCard(
            "⚠️ PARTIAL",
            listOf(
                "Captions: fully offline only if the language's offline speech pack is installed",
                "OCR: Latin script only, one photo at a time, no Telugu/Hindi text yet",
                "Telugu/Hindi voice: needs the voice installed on the phone"
            )
        )
        InfoCard(
            "🛠 PLANNED (no AI model yet)",
            listOf(
                "Obstacle, stair, door, pothole detection and distance estimation",
                "ISL sign recognition and text-to-sign animation",
                "Alarm / horn / siren / doorbell detection",
                "Indian currency recognition",
                "Person recognition with consent",
                "Voice-guided onboarding, screen-reader tuning pass, more languages"
            )
        )
    }
}


