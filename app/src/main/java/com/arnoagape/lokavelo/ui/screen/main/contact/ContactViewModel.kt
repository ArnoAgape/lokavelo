package com.arnoagape.lokavelo.ui.screen.main.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.ConversationRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val conversationRepository: ConversationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _bikeId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val bike: StateFlow<Bike?> =
        _bikeId
            .filterNotNull()
            .flatMapLatest { bikeRepository.observeBike(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    fun setBikeId(id: String) {
        _bikeId.value = id
    }

    private val _openConversation = MutableSharedFlow<String>()
    val openConversation = _openConversation.asSharedFlow()

    fun sendMessage(
        text: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {

        val bike = bike.value ?: return
        val renterId = auth.currentUser?.uid ?: return

        if (bike.ownerId == renterId) return

        viewModelScope.launch {

            val conversation = conversationRepository.getOrCreateConversation(
                bikeId = bike.id,
                ownerId = bike.ownerId,
                renterId = renterId,
                startDate = startDate,
                endDate = endDate
            )

            conversationRepository.sendMessage(
                conversationId = conversation.id,
                message = Message(
                    conversationId = conversation.id,
                    senderId = renterId,
                    text = text,
                    createdAt = System.currentTimeMillis()
                )
            )

            _openConversation.emit(conversation.id)
        }
    }
}