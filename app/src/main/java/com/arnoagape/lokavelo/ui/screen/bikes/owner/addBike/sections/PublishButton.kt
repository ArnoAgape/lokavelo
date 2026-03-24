package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@Composable
fun SubmitButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    submitText: String
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = submitText,
                fontSize = 16.sp
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SubmitButtonPreview() {
    LokaveloTheme {
        SubmitButton(
            modifier = Modifier,
            enabled = true,
            onClick = {},
            isLoading = false,
            submitText = "Ajouter"
        )
    }
}