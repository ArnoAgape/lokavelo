package com.arnoagape.lokavelo.ui.screen.splash

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import kotlinx.coroutines.delay

private enum class SplashPhase { HIDDEN, VISIBLE, EXITING }

private val EaseOutCubic = Easing { t -> 1 - (1 - t) * (1 - t) * (1 - t) }

@Composable
fun LokaveloSplashScreen(onFinished: () -> Unit) {

    var phase by remember { mutableStateOf(SplashPhase.HIDDEN) }

    // --- Logo ---
    val logoScale by animateFloatAsState(
        targetValue = when (phase) {
            SplashPhase.HIDDEN -> 0.3f
            SplashPhase.VISIBLE -> 1f
            SplashPhase.EXITING -> 1.06f
        },
        animationSpec = when (phase) {
            SplashPhase.VISIBLE -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            else -> tween(420)
        },
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = when (phase) {
            SplashPhase.HIDDEN -> 0f
            SplashPhase.VISIBLE -> 1f
            SplashPhase.EXITING -> 0f
        },
        animationSpec = tween(420),
        label = "logoAlpha"
    )

    // --- Halo ---
    val haloScale by animateFloatAsState(
        targetValue = when (phase) {
            SplashPhase.HIDDEN -> 0f
            SplashPhase.VISIBLE -> 1f
            SplashPhase.EXITING -> 1.5f
        },
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "haloScale"
    )

    val haloAlpha by animateFloatAsState(
        targetValue = when (phase) {
            SplashPhase.HIDDEN -> 0f
            SplashPhase.VISIBLE -> 1f
            SplashPhase.EXITING -> 0f
        },
        animationSpec = tween(500),
        label = "haloAlpha"
    )

    LaunchedEffect(Unit) {
        phase = SplashPhase.VISIBLE
        delay(1700)
        phase = SplashPhase.EXITING
        delay(450)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {

        // Halo derrière le logo
        Box(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = haloAlpha * 0.18f
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Logo
        Image(
            painter = painterResource(R.drawable.ic_lokavelo_logo),
            contentDescription = null,
            modifier = Modifier
                .size(210.dp)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                    alpha = logoAlpha
                }
        )
    }
}