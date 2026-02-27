package com.arnoagape.lokavelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arnoagape.lokavelo.navigation.LokaveloApp
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LokaveloTheme {
                LokaveloApp()
            }
        }
    }
}