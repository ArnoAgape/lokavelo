package com.arnoagape.lokavelo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arnoagape.lokavelo.navigation.LokaveloApp
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* l'utilisateur a fait son choix */ }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Après notifications → demande localisation
        requestLocationIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            // Demande notifs d'abord, localisation suivra dans le callback
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Notifs déjà OK (ou Android < 13) → demande localisation directement
            requestLocationIfNeeded()
        }

        setContent {
            LokaveloTheme {
                LokaveloApp()
            }
        }
    }

    private fun requestLocationIfNeeded() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}