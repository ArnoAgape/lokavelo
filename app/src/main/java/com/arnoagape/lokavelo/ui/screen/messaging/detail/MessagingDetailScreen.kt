package com.arnoagape.lokavelo.ui.screen.messaging.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
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

    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.setConversationId(conversationId)
    }

    // ⭐ Auto scroll au dernier message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    MessagingDetailContent(
        messages = messages,
        currentUserId = viewModel.currentUserId,
        currentUserName = viewModel.currentUserName,
        listState = listState,
        onSend = { viewModel.sendMessage(it) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingDetailContent(
    messages: List<Message>,
    currentUserName: String?,
    currentUserId: String?,
    listState: LazyListState,
    onSend: (String) -> Unit,
    onBack: () -> Unit
) {

    Scaffold(

        topBar = {
            MessagingTopBar(
                displayName = currentUserName,
                onBack = onBack
            )
        },

        bottomBar = {
            MessageInputBar(
                onSend = onSend
            )
        }

    ) { padding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    stringResource(R.string.messaging_conversation),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            },
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
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
                shape = RoundedCornerShape(16.dp),
                color =
                    if (isMine)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
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
            currentUserName = "Arno"
        )
    }
}