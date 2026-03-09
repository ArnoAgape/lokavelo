package com.arnoagape.lokavelo.ui.screen.messaging.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.toHourMinute

@Composable
fun MessagingHomeScreen(
    onConversationClick: (String) -> Unit
) {

    val viewModel: MessagingHomeViewModel = hiltViewModel()
    val state by viewModel.conversationsScreen.collectAsStateWithLifecycle()

    MessagingHomeContent(
        conversations = state,
        onConversationClick = onConversationClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingHomeContent(
    conversations: List<ConversationItemScreen>,
    onConversationClick: (String) -> Unit
) {

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(stringResource(R.string.messaging)) }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            items(
                items = conversations,
                key = { it.conversation.id }
            ) { item ->

                ConversationItem(
                    conversation = item.conversation,
                    bike = item.bike,
                    onClick = { onConversationClick(item.conversation.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    bike: Bike?,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = bike?.photoUrls?.firstOrNull(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = bike?.title ?: stringResource(R.string.conversation),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = conversation.lastMessage,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = conversation.lastMessageTime.toHourMinute(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@PreviewLightDark
@Composable
private fun MessagingHomeContentPreview() {

    val fakeConversation = listOf(
        ConversationItemScreen(
            conversation = PreviewData.conversation,
            bike = PreviewData.bike,
            lastMessage = PreviewData.conversation.lastMessage,
            lastMessageTime = PreviewData.conversation.lastMessageTime,
            isOwner = false
        )
    )

    LokaveloTheme {
        MessagingHomeContent(
            conversations = fakeConversation,
            onConversationClick = {}
        )
    }
}