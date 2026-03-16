package com.arnoagape.lokavelo.ui.screen.messaging.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.ConversationRepository
import com.arnoagape.lokavelo.data.repository.RentalRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagingDetailViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val rentalRepository: RentalRepository,
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

    val conversation: StateFlow<Conversation?> =
        _conversationId
            .filterNotNull()
            .flatMapLatest {
                conversationRepository.observeConversation(it)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val currentUserId = requireNotNull(auth.currentUser?.uid)
    val otherUserName: StateFlow<String?> =
        conversation
            .map { conv ->

                if (conv == null) return@map null

                if (conv.ownerId == currentUserId)
                    conv.renterName
                else
                    conv.ownerName
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

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

    val rental: StateFlow<Rental?> =
        _conversationId
            .filterNotNull()
            .flatMapLatest { rentalRepository.observeRental(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val rentalState: StateFlow<RentalStateUi> =
        rental
            .map { rental ->

                if (rental == null) return@map RentalStateUi.None

                val isOwner = rental.ownerId == currentUserId

                when {

                    rental.status == RentalStatus.PENDING && isOwner ->
                        RentalStateUi.OwnerRequest(rental)

                    rental.status == RentalStatus.PENDING && !isOwner ->
                        RentalStateUi.RenterWaiting(rental)

                    rental.status == RentalStatus.COUNTER_OFFER && !isOwner ->
                        RentalStateUi.RenterCounterOffer(rental)

                    else -> RentalStateUi.None
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                RentalStateUi.None
            )

    fun acceptRental() {

        val id = rental.value?.id ?: return

        viewModelScope.launch {
            rentalRepository.updateRentalStatus(
                id,
                RentalStatus.ACCEPTED
            )
        }
    }

    fun declineRental() {

        val id = rental.value?.id ?: return

        viewModelScope.launch {
            rentalRepository.updateRentalStatus(
                id,
                RentalStatus.DECLINED
            )
        }
    }

    fun makeOffer(priceInCents: Long) {
        viewModelScope.launch {
            rentalRepository.makeOffer(
                rentalId = rental.value!!.id,
                newPrice = priceInCents
            )
        }
    }

    fun setConversationId(id: String) {
        _conversationId.value = id
    }

    fun sendMessage(text: String) {

        val conversationId = _conversationId.value ?: return
        val senderId = auth.currentUser?.uid ?: return
        val receiverId = conversation.value?.participants
            ?.firstOrNull { it != senderId } ?: return

        viewModelScope.launch {

            conversationRepository.sendMessage(
                conversationId = conversationId,
                message = Message(
                    conversationId = conversationId,
                    senderId = senderId,
                    text = text,
                    createdAt = System.currentTimeMillis()
                ),
                receiverId = receiverId
            )
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.markConversationAsRead(conversationId, currentUserId)
        }
    }

    fun setConversationActive(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.setConversationActive(conversationId, currentUserId)
        }
    }

    fun clearConversationActive() {
        viewModelScope.launch {
            conversationRepository.clearConversationActive(currentUserId)
        }
    }
}