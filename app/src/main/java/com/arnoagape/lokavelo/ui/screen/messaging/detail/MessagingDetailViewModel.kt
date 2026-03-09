package com.arnoagape.lokavelo.ui.screen.messaging.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.ConversationRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagingDetailViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val bikeRepository: BikeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _bikeId = MutableStateFlow<String?>(null)

    val bike: StateFlow<Bike?> =
        _bikeId
            .filterNotNull()
            .flatMapLatest { bikeRepository.observeBike(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val _conversationId = MutableStateFlow<String?>(null)

    val currentUserId = auth.currentUser?.uid
    val currentUserName = auth.currentUser?.displayName

    val messages = _conversationId
        .filterNotNull()
        .flatMapLatest {
            conversationRepository.observeMessages(it)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setConversationId(id: String) {
        _conversationId.value = id
    }

    fun sendMessage(text: String) {

        val conversationId = _conversationId.value ?: return
        val senderId = auth.currentUser?.uid ?: return

        viewModelScope.launch {

            conversationRepository.sendMessage(
                conversationId = conversationId,
                message = Message(
                    conversationId = conversationId,
                    senderId = senderId,
                    text = text,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}