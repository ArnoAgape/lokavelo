package com.arnoagape.lokavelo.ui.screen.main.contact

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.SubmitButton
import com.arnoagape.lokavelo.ui.screen.bikes.BikeItemRow
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.vibrateError
import com.arnoagape.lokavelo.ui.utils.vibrateMessageSent
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    bikeId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    onConversationCreated: (String) -> Unit,
    onBack: () -> Unit
) {

    val viewModel: ContactViewModel = hiltViewModel()
    val bike by viewModel.bike.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                context.vibrateError()
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            is Event.ShowSuccessMessage -> {
                context.vibrateMessageSent()

                Toast.makeText(
                    context,
                    resources.getString(event.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(bikeId) {
        viewModel.setBikeId(bikeId)
    }

    LaunchedEffect(Unit) {
        viewModel.openConversation.collect { conversationId ->
            onConversationCreated(conversationId)
        }
    }

    var message by remember { mutableStateOf("") }

    ContactContent(
        snackbarHostState = snackbarHostState,
        bike = bike,
        startDate = startDate,
        endDate = endDate,
        message = message,
        onMessageChange = { message = it },
        isSendEnabled = message.isNotBlank(),
        onSendClick = {
            viewModel.sendMessage(
                text = message,
                startDate = startDate,
                endDate = endDate
            )
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactContent(
    snackbarHostState: SnackbarHostState,
    bike: Bike?,
    startDate: LocalDate,
    endDate: LocalDate,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBack: () -> Unit,
    isSendEnabled: Boolean
) {

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    actionColor = MaterialTheme.colorScheme.error,
                    dismissActionContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.send_message)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },

        bottomBar = {
            SubmitButton(
                modifier = Modifier.imePadding(),
                isLoading = false,
                enabled = isSendEnabled,
                onClick = onSendClick,
                submitText = stringResource(R.string.send)
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {

                Column(Modifier.padding(16.dp)) {

                    bike?.let {

                        BikeItemRow(
                            bike = it,
                            startDate = startDate,
                            endDate = endDate
                        )
                    }
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.hint_message_owner),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactContentPreview() {
    LokaveloTheme {

        ContactContent(
            snackbarHostState = SnackbarHostState(),
            bike = PreviewData.bike,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(3),
            message = "",
            onMessageChange = {},
            onSendClick = {},
            onBack = {},
            isSendEnabled = false
        )
    }
}