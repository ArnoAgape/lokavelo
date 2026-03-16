package com.arnoagape.lokavelo.ui.screen.messaging.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
                textStyle = MaterialTheme.typography.bodyMedium,
                layout = RentalDatesLayout.Inline,
                start = rental.startDate.toLocalDate(),
                end = rental.endDate.toLocalDate()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = stringResource(
                    R.string.rental_owner_receives,
                    rental.basePriceInCents.toPriceString()
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onAcceptClick
                ) {
                    Text(stringResource(R.string.accept))
                }

                Button(
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onDeclineClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.decline))
                }

                OutlinedButton(
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onMakeOfferClick
                ) {
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

    RentalBannerSurface {

        Text(
            text = stringResource(R.string.rental_request_sent),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        RentalDates(
            textStyle = MaterialTheme.typography.bodyMedium,
            layout = RentalDatesLayout.Inline,
            start = rental.startDate.toLocalDate(),
            end = rental.endDate.toLocalDate()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(
                R.string.rental_price,
                rental.priceTotalInCents.toPriceString()
            )
        )
    }
}

@Composable
fun RenterCounterOfferBanner(
    rental: Rental,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {

    RentalBannerSurface {

        Text(
            text = stringResource(R.string.rental_new_offer),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        RentalDates(
            textStyle = MaterialTheme.typography.bodyMedium,
            layout = RentalDatesLayout.Inline,
            start = rental.startDate.toLocalDate(),
            end = rental.endDate.toLocalDate()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(
                R.string.rental_price_offered,
                rental.priceTotalInCents.toPriceString()
            )
        )

        Spacer(Modifier.height(12.dp))

        RentalActionButtons(
            onAcceptClick = onAcceptClick,
            onDeclineClick = onDeclineClick
        )
    }
}

@Composable
fun OwnerRentalRequestCompactBanner(
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    onMakeOfferClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        RentalActionButtons(
            showOffer = true,
            onAcceptClick = onAcceptClick,
            onDeclineClick = onDeclineClick,
            onMakeOfferClick = onMakeOfferClick
        )
    }
}

@Composable
fun RenterCounterOfferCompactBanner(
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        RentalActionButtons(
            onAcceptClick = onAcceptClick,
            onDeclineClick = onDeclineClick
        )
    }
}

@Composable
private fun RentalBannerSurface(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun RentalActionButtons(
    showOffer: Boolean = false,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    onMakeOfferClick: (() -> Unit)? = null
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Button(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            onClick = onAcceptClick
        ) {
            Text(stringResource(R.string.accept))
        }

        Button(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            onClick = onDeclineClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(stringResource(R.string.decline))
        }

        if (showOffer && onMakeOfferClick != null) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                onClick = onMakeOfferClick
            ) {
                Text(stringResource(R.string.make_offer))
            }
        }
    }
}