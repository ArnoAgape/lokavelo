package com.arnoagape.lokavelo.ui.screen.messaging.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.toDayLabelYear
import com.arnoagape.lokavelo.ui.utils.toHourMinute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingDetailScreen(
    conversationId: String,
    onBack: () -> Unit
) {

    val viewModel: MessagingDetailViewModel = hiltViewModel()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val rentalState by viewModel.rentalState.collectAsStateWithLifecycle()
    val otherUserName by viewModel.otherUserName.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.markConversationAsRead(conversationId)
        viewModel.setConversationId(conversationId)
        viewModel.setConversationActive(conversationId)
    }

    // Auto scroll au dernier message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearConversationActive()
        }
    }

    MessagingDetailContent(
        messages = messages,
        currentUserId = viewModel.currentUserId,
        otherUserName = otherUserName,
        rentalState = rentalState,
        onAccept = { viewModel.acceptRental() },
        onDecline = { viewModel.declineRental() },
        onOffer = { viewModel.makeOffer(it) },
        listState = listState,
        onSend = { viewModel.sendMessage(it) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingDetailContent(
    messages: List<Message>,
    otherUserName: String?,
    currentUserId: String?,
    rentalState: RentalStateUi,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOffer: (Long) -> Unit,
    listState: LazyListState,
    onSend: (String) -> Unit,
    onBack: () -> Unit
) {

    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    var showAcceptDialog by remember { mutableStateOf(false) }
    var showDeclineDialog by remember { mutableStateOf(false) }
    var showOfferDialog by remember { mutableStateOf(false) }

    LaunchedEffect(imeVisible, messages.size) {
        if (imeVisible && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            MessagingTopBar(
                displayName = otherUserName,
                onBack = onBack
            )
        },
        bottomBar = {
            MessageInputBar(onSend = onSend)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                itemsIndexed(
                    items = messages,
                    key = { _, message -> message.id }
                ) { index, message ->

                    val previousMessage = messages.getOrNull(index - 1)

                    val showDateSeparator =
                        previousMessage == null ||
                                previousMessage.createdAt.toDayLabelYear() != message.createdAt.toDayLabelYear()

                    if (showDateSeparator) {
                        DaySeparator(message.createdAt)
                    }

                    val previousSender = previousMessage?.senderId

                    MessageBubble(
                        message = message,
                        isMine = message.senderId == currentUserId,
                        isGrouped = previousSender == message.senderId
                    )
                }
            }

            // -------- BANNER --------

            AnimatedContent(
                targetState = imeVisible,
                label = "rentalBanner"
            ) { keyboardVisible ->

                when (rentalState) {

                    is RentalStateUi.OwnerRequest ->

                        if (keyboardVisible) {
                            OwnerRentalRequestCompactBanner(
                                onAcceptClick = { showAcceptDialog = true },
                                onDeclineClick = { showDeclineDialog = true },
                                onMakeOfferClick = { showOfferDialog = true }
                            )
                        } else {
                            OwnerRentalRequestBanner(
                                rental = rentalState.rental,
                                onAcceptClick = { showAcceptDialog = true },
                                onDeclineClick = { showDeclineDialog = true },
                                onMakeOfferClick = { showOfferDialog = true }
                            )
                        }

                    is RentalStateUi.RenterWaiting ->
                        if (!keyboardVisible) {
                            RenterWaitingBanner(
                                rental = rentalState.rental
                            )
                        }

                    is RentalStateUi.RenterCounterOffer ->
                        if (keyboardVisible) {
                            RenterCounterOfferCompactBanner(
                                onAcceptClick = onAccept,
                                onDeclineClick = onDecline
                            )
                        } else {
                            RenterCounterOfferBanner(
                                rental = rentalState.rental,
                                onAcceptClick = onAccept,
                                onDeclineClick = onDecline
                            )
                        }

                    RentalStateUi.None -> Unit
                }
            }
        }
    }

    // ---------------- ACCEPT

    if (showAcceptDialog) {

        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAccept()
                        showAcceptDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = stringResource(R.string.accept),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAcceptDialog = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.confirm_accept_rental)) }
        )
    }

    // ---------------- DECLINE

    if (showDeclineDialog) {

        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDecline()
                        showDeclineDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        stringResource(R.string.decline),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeclineDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.confirm_decline_rental))
            }
        )
    }

    // ---------------- MAKE OFFER

    if (showOfferDialog) {

        var offerText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showOfferDialog = false },

            confirmButton = {
                TextButton(
                    onClick = {
                        val offer = offerText.toDoubleOrNull()

                        if (offer != null) {
                            onOffer((offer * 100).toLong())
                        }
                    }
                ) {
                    Text(stringResource(R.string.validate))
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showOfferDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },

            title = {
                Text(stringResource(R.string.make_offer))
            },

            text = {

                OutlinedTextField(
                    value = offerText,
                    onValueChange = { offerText = it },
                    label = {
                        Text(stringResource(R.string.offer_amount))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingTopBar(
    displayName: String?,
    onBack: () -> Unit
) {

    TopAppBar(

        title = {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )

                Spacer(Modifier.width(12.dp))

                if (displayName != null) {
                    Text(displayName)
                }
            }
        },

        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun MessageInputBar(
    onSend: (String) -> Unit
) {

    var text by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val isEnabled = text.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        fun sendMessage() {
            if (text.isNotBlank()) {
                onSend(text)
                text = ""
                focusManager.clearFocus()
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    stringResource(R.string.hint_messaging_conversation),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { sendMessage() }
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = { sendMessage() },
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            enabled = isEnabled
        ) {

            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    isGrouped: Boolean
) {

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement =
                if (isMine) Arrangement.End else Arrangement.Start
        ) {

            Surface(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                color =
                    if (isMine)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent,
                border =
                    if (isMine) null
                    else BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
            ) {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = if (isGrouped) 4.dp else 8.dp
                    )
                ) {

                    Text(
                        text = message.text
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = message.createdAt.toHourMinute(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DaySeparator(
    timestamp: Long
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {

            Text(
                text = timestamp.toDayLabelYear(),
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 4.dp
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun MessagingContentPreview() {

    LokaveloTheme {

        MessagingDetailContent(
            messages = PreviewData.messages,
            currentUserId = "user2",
            listState = rememberLazyListState(),
            onSend = {},
            onBack = {},
            otherUserName = "Arno",
            rentalState = RentalStateUi.OwnerRequest(PreviewData.rental),
            onAccept = {},
            onDecline = {},
            onOffer = {}
        )
    }
}