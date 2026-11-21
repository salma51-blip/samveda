package com.samveda.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        val haptics = Haptics(this, prefs)
        speaker = Speaker(this)
        setContent {
            SamvedaTheme(dark = prefs.dark) {
                App(prefs, speaker, haptics)
            }
        }
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}

@Composable
fun SamvedaTheme(dark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (dark) {
        darkColorScheme(
            primary = Color(0xFFFFD54F),
            onPrimary = Color.Black,
            secondary = Color(0xFF80DEEA),
            onSecondary = Color.Black,
            background = Color(0xFF0B0F14),
            onBackground = Color.White,
            surface = Color(0xFF0B0F14),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1B2530),
            onSurfaceVariant = Color.White,
            error = Color(0xFFFF6B6B)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0D47A1),
            onPrimary = Color.White,
            secondary = Color(0xFF006064),
            onSecondary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFE3EAF3),
            onSurfaceVariant = Color.Black,
            error = Color(0xFFB00020)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun App(prefs: Prefs, speaker: Speaker, haptics: Haptics) {
    var screen by rememberSaveable { mutableStateOf("home") }
    var showSos by remember { mutableStateOf(false) }

    BackHandler(enabled = screen != "home") { screen = "home" }

    val go: (String) -> Unit = { target ->
        haptics.tap()
        screen = target
    }
    val back: () -> Unit = { screen = "home" }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (screen) {
            "sparsha" -> SparshaScreen(prefs, speaker, haptics, back)
            "drishti" -> DrishtiScreen(prefs, speaker, haptics, back)
            "shravan" -> ShravanScreen(prefs, speaker, haptics, back)
            "sahaya" -> SahayaScreen(prefs, speaker, haptics, back)
            "settings" -> SettingsScreen(prefs, speaker, haptics, back)
            "status" -> StatusScreen(back)
            else -> HomeScreen(onOpen = go, onSos = { haptics.stopAlert(); showSos = true })
        }
    }

    if (showSos) {
        SosDialog(prefs = prefs, onDismiss = { showSos = false })
    }
}
