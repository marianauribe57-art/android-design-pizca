package com.adso.pizca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adso.pizca.presentation.PizcaApp
import com.adso.pizca.ui.theme.PizcaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizcaTheme {
                PizcaApp()
            }
        }
    }
}