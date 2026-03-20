package com.arnoagape.lokavelo.ui.screen.main.contact

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.data.repository.BikeRepository
import com.arnoagape.lokavelo.data.repository.ConversationRepository
import com.arnoagape.lokavelo.data.repository.RentalRepository
import com.arnoagape.lokavelo.data.repository.UserRepository
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.Conversation
import com.arnoagape.lokavelo.domain.model.Message
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.utils.AppConstants.SERVICE_FEE_RATE
import com.arnoagape.lokavelo.ui.utils.NetworkUtils
import com.arnoagape.lokavelo.ui.utils.calculateRentalPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContactViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val conversationRepository: ConversationRepository,
    private val rentalRepository: RentalRepository,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()
    private val _bikeId = MutableStateFlow<String?>(null)
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

        viewModelScope.launch {

            if (text.isBlank()) return@launch

            if (!networkUtils.isNetworkAvailable()) {
                _events.send(
                    Event.ShowMessage(R.string.error_no_network)
                )
                return@launch
            }

            val bike = bike.value ?: return@launch
            val renter = userRepository.observeCurrentUser().first() ?: return@launch

            if (bike.ownerId == renter.id) return@launch

            val conversation = conversationRepository.getOrCreateConversation(
                bikeId = bike.id,
                ownerId = bike.ownerId,
                ownerName = bike.ownerName,
                renterId = renter.id,
                renterName = renter.displayName ?: "Utilisateur",
                startDate = startDate,
                endDate = endDate
            )

            val days = maxOf(
                1,
                ChronoUnit.DAYS.between(startDate, endDate).toInt()
            )
            val basePrice = calculateRentalPrice(
                dayPrice = bike.priceInCents,
                twoDaysPrice = bike.priceTwoDaysInCents,
                weekPrice = bike.priceWeekInCents,
                monthPrice = bike.priceMonthInCents,
                days = days
            )

            val serviceFee = (basePrice * SERVICE_FEE_RATE).toLong()

            val totalPrice = basePrice + serviceFee

            rentalRepository.createRental(
                Rental(
                    id = conversation.id,
                    bikeId = bike.id,
                    ownerId = bike.ownerId,
                    renterId = renter.id,
                    startDate = startDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                    endDate = endDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                    basePriceInCents = basePrice,
                    serviceFeeInCents = serviceFee,
                    priceTotalInCents = totalPrice,
                    status = RentalStatus.PENDING
                )
            )

            userRepository.incrementPendingRentalsUnread(bike.ownerId)

            conversationRepository.sendMessage(
                conversationId = conversation.id,
                message = Message(
                    conversationId = conversation.id,
                    senderId = renter.id,
                    text = text,
                    createdAt = System.currentTimeMillis()
                ),
                receiverId = bike.ownerId
            )

            _events.trySend(
                Event.ShowSuccessMessage(R.string.success_message_sent)
            )

            _openConversation.emit(conversation.id)
        }
    }
}