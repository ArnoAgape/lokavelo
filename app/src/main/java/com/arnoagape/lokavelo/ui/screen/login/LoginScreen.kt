package com.arnoagape.lokavelo.ui.screen.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.components.OrSeparator

/**
 * Displays the login screen with multiple sign-in options:
 * email, Google, and guest access.
 *
 * @param viewModel ViewModel providing authentication state and events.
 * @param onLoginSuccess Callback executed after successful sign-in.
 * @param onGoogleSignInClick Launches the Google sign-in flow.
 * @param onEmailSignInClick Launches the email sign-in flow.
 */
@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    onEmailSignInClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()

    val context = LocalContext.current
    val resources = LocalResources.current

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                Toast.makeText(
                    context,
                    resources.getString(event.message),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> Unit
        }
    }

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoginContent(
                onGoogleSignInClick = { viewModel.onSignInRequested { onGoogleSignInClick() } },
                onEmailSignInClick = { viewModel.onSignInRequested { onEmailSignInClick() } }
            )
        }
    }
}

@Composable
fun LoginContent(
    onGoogleSignInClick: () -> Unit,
    onEmailSignInClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /** ---------- LOGO LOKAVELO ---------- **/
            Image(
                painter = painterResource(id = R.drawable.ic_lokavelo_logo),
                contentDescription = "Logo Lokavelo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.sign_in_title),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            /** ---------- EMAIL BUTTON ---------- **/
            Button(
                onClick = { onEmailSignInClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.sign_in_email))
            }

            Spacer(modifier = Modifier.height(16.dp))

            /** ---------- PHONE BUTTON ---------- **/
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.sign_in_phone))
            }

            Spacer(modifier = Modifier.height(14.dp))

            /** ---------- SEPARATOR ---------- **/
            OrSeparator()

            Spacer(modifier = Modifier.height(14.dp))

            /** ---------- GOOGLE BUTTON ---------- **/
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag(stringResource(R.string.sign_in_google)),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.sign_in_google))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    LokaveloTheme {
        LoginContent(
            onEmailSignInClick = { },
            onGoogleSignInClick = { }
        )
    }
}