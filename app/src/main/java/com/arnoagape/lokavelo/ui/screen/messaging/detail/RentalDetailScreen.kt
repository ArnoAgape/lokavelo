package com.arnoagape.lokavelo.ui.screen.messaging.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Rental
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.common.components.RentalDatesLayout
import com.arnoagape.lokavelo.ui.utils.toLocalDate
import com.arnoagape.lokavelo.ui.utils.toPriceString


@Composable
fun OwnerRentalRequestBanner(
    rental: Rental,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    onMakeOfferClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {

        Column(Modifier.padding(16.dp)) {

            Text(
                text = stringResource(R.string.rental_request),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            RentalDates(
                textStyle = MaterialTheme.typography.titleMedium,
                layout = RentalDatesLayout.Inline,
                start = rental.startDate.toLocalDate(),
                end = rental.endDate.toLocalDate()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.rental_owner_receives,
                    rental.basePriceInCents.toPriceString()
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(onClick = onAcceptClick) {
                    Text(stringResource(R.string.accept))
                }

                OutlinedButton(onClick = onDeclineClick) {
                    Text(stringResource(R.string.decline))
                }

                OutlinedButton(onClick = onMakeOfferClick) {
                    Text(stringResource(R.string.make_offer))
                }
            }
        }
    }
}

@Composable
fun RenterWaitingBanner(
    rental: Rental
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {

        Column(Modifier.padding(16.dp)) {

            Text(
                text = stringResource(R.string.rental_request_sent),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            RentalDates(
                layout = RentalDatesLayout.Inline,
                start = rental.startDate.toLocalDate(),
                end = rental.endDate.toLocalDate()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.rental_price,
                    rental.priceTotalInCents.toPriceString()
                )
            )
        }
    }
}

@Composable
fun RenterCounterOfferBanner(
    rental: Rental,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {

        Column(Modifier.padding(16.dp)) {

            Text(
                text = stringResource(R.string.rental_new_offer),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            RentalDates(
                layout = RentalDatesLayout.Inline,
                start = rental.startDate.toLocalDate(),
                end = rental.endDate.toLocalDate()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.rental_price_offered,
                    rental.priceTotalInCents.toPriceString()
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(onClick = onAcceptClick) {
                    Text(stringResource(R.string.accept))
                }

                OutlinedButton(onClick = onDeclineClick) {
                    Text(stringResource(R.string.decline))
                }
            }
        }
    }
}