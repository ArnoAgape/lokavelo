package com.arnoagape.lokavelo.ui.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.loading))
        }
    }
}

@Composable
fun DeletingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.deleting))
        }
    }
}

@Composable
fun ErrorOverlay(
    type: ErrorType,
    onRetry: () -> Unit = {}
) {

    val (message, images, imageAfterText) = when (type) {

        ErrorType.NETWORK -> Triple(
            stringResource(R.string.error_no_network),
            listOf(R.drawable.ic_bike_no_wifi),
            false
        )

        ErrorType.EMPTY_BIKES -> Triple(
            stringResource(R.string.no_bike),
            listOf(R.drawable.ic_bike_add_bike_arrow),
            true
        )

        ErrorType.EMPTY_MESSAGES -> Triple(
            stringResource(R.string.empty_messaging),
            listOf(R.drawable.ic_bike_no_message),
            false
        )

        ErrorType.EMPTY_RENTALS -> Triple(
            stringResource(R.string.empty_messaging),
            listOf(R.drawable.ic_bike_no_message),
            false
        )

        ErrorType.GENERIC -> Triple(
            stringResource(R.string.error_generic),
            listOf(R.drawable.ic_bike_broken),
            false
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (imageAfterText) {

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(24.dp))
            }

            images.forEach { imageRes ->

                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.height(140.dp)
                )

                Spacer(Modifier.height(24.dp))
            }

            if (!imageAfterText) {

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(24.dp))
            }

            if (
                type != ErrorType.EMPTY_MESSAGES &&
                type != ErrorType.EMPTY_RENTALS &&
                type != ErrorType.EMPTY_BIKES
            ) {

                Spacer(Modifier.height(12.dp))

                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

enum class ErrorType {
    NETWORK,
    EMPTY_BIKES,
    EMPTY_MESSAGES,
    EMPTY_RENTALS,
    GENERIC
}

@PreviewLightDark
@Composable
private fun ErrorOverlayPreview() {
    LokaveloTheme {
        ErrorOverlay(
            type = ErrorType.EMPTY_RENTALS,
            onRetry = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun LoadingOverlayPreview() {
    LokaveloTheme {
        LoadingOverlay()
    }
}