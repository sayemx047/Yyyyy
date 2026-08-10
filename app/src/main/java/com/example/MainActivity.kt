package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.ArenaXApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // If anything crashes, the full stack trace is always in Logcat
            // under the tag "ARENAX_FATAL" (see ArenaXApplication.kt) —
            // Compose doesn't allow try/catch directly around a composable
            // call, so Logcat is the way to see what went wrong.
            ArenaXApp()
        }
    }
}
