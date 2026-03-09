package com.arnoagape.lokavelo.ui.screen.messaging.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.ConversationRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagingHomeViewModel @Inject constructor(
    conversationRepository: ConversationRepository,
    private val bikeRepository: BikeRepository,
    auth: FirebaseAuth
) : ViewModel() {

    private val userId = auth.currentUser?.uid ?: ""

    val conversationsScreen: StateFlow<List<ConversationItemScreen>> =
        conversationRepository
            .observeUserConversations(userId)
            .flatMapLatest { conversations ->

                if (conversations.isEmpty())
                    return@flatMapLatest flowOf(emptyList())

                combine(
                    conversations.map { conversation ->

                        bikeRepository
                            .observeBike(conversation.bikeId)
                            .map { bike ->

                                ConversationItemScreen(
                                    conversation = conversation,
                                    bike = bike,
                                    lastMessage = conversation.lastMessage,
                                    lastMessageTime = conversation.lastMessageTime,
                                    isOwner = conversation.ownerId == userId
                                )
                            }
                    }
                ) { it.toList() }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}

data class ConversationItemScreen(
    val conversation: Conversation,
    val bike: Bike?,
    val lastMessage: String,
    val lastMessageTime: Long,
    val isOwner: Boolean
)