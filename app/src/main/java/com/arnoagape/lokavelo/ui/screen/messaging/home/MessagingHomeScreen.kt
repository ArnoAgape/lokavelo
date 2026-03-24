package com.arnoagape.lokavelo.ui.screen.messaging.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.common.components.RentalDatesLayout
import com.arnoagape.lokavelo.ui.preview.PreviewData
import com.arnoagape.lokavelo.ui.screen.bikes.bikeItem.BikeImage
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.toDayLabel
import com.arnoagape.lokavelo.ui.utils.toLocalDateFromEpochDay

@Composable
fun MessagingHomeScreen(
    viewModel: MessagingHomeViewModel,
    onConversationClick: (String) -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    MessagingHomeContent(
        state = state,
        onConversationClick = onConversationClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingHomeContent(
    state: MessagingHomeUiState,
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

        when (state) {

            is MessagingHomeUiState.Loading -> {
                LoadingOverlay()
            }

            is MessagingHomeUiState.Empty -> {
                ErrorOverlay(
                    type = ErrorType.EMPTY_MESSAGES
                )
            }

            is MessagingHomeUiState.Success -> {
                val conversations = state.conversations
                LazyColumn(
                    contentPadding = PaddingValues(top = 10.dp),
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
                            displayName = item.displayName,
                            unreadCount = item.unreadCount,
                            onClick = { onConversationClick(item.conversation.id) }
                        )
                    }
                }
            }

            is MessagingHomeUiState.Error -> {
                ErrorOverlay(
                    type = ErrorType.GENERIC
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    bike: Bike?,
    displayName: String?,
    unreadCount: Int,
    onClick: () -> Unit
) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (bike != null) BikeImage(bike, size = 70.dp)

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = displayName ?: stringResource(R.string.deleted_user),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                val isUnread = unreadCount > 0

                Text(
                    text = conversation.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        if (isUnread)
                            MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        else
                            MaterialTheme.typography.bodyMedium,
                    color =
                        if (isUnread)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                RentalDates(
                    layout = RentalDatesLayout.Inline,
                    start = conversation.startDateEpochDay.toLocalDateFromEpochDay(),
                    end = conversation.endDateEpochDay.toLocalDateFromEpochDay()
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = conversation.lastMessageTime.toDayLabel(),
                    style = MaterialTheme.typography.labelSmall
                )

                if (unreadCount > 0) {

                    Spacer(Modifier.height(6.dp))

                    Badge {
                        Text(
                            if (unreadCount > 9) "9+"
                            else unreadCount.toString()
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MessagingHomeContentPreview() {

    val fakeConversation = listOf(
        ConversationItemScreen(
            conversation = PreviewData.conversation,
            bike = PreviewData.bike,
            displayName = PreviewData.user.displayName,
            lastMessage = PreviewData.conversation.lastMessage,
            lastMessageTime = PreviewData.conversation.lastMessageTime,
            isOwner = false,
            unreadCount = 12
        )
    )

    LokaveloTheme {
        MessagingHomeContent(
            state = MessagingHomeUiState.Success(fakeConversation),
            onConversationClick = {}
        )
    }
}