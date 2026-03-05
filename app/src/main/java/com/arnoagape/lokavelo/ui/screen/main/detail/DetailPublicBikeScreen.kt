package com.arnoagape.lokavelo.ui.screen.main.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.common.components.photo.PhotoItem
import com.arnoagape.lokavelo.ui.common.components.photo.PhotosContent
import com.arnoagape.lokavelo.ui.screen.main.detail.sections.BikeSpecsCard
import com.arnoagape.lokavelo.ui.screen.main.detail.sections.DescriptionCard
import com.arnoagape.lokavelo.ui.screen.main.detail.sections.OwnerCard
import com.arnoagape.lokavelo.ui.screen.main.detail.sections.PriceTableCard
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.toPriceString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPublicBikeScreen(
    bikeId: String,
    viewModel: DetailPublicBikeViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(bikeId) {
        viewModel.setBikeId(bikeId)
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.bike?.title ?: stringResource(R.string.detail_bike),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.cd_go_back)
                        )
                    }
                }
            )
        },

        bottomBar = {

            Surface(
                tonalElevation = 4.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = state.bike?.priceInCents?.toPriceString() ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Button(
                        onClick = { /* contacter */ }
                    ) {
                        Text("Contacter")
                    }
                }
            }
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {

            when {
                state.isLoading -> {
                    LoadingOverlay(
                        text = stringResource(R.string.loading)
                    )
                }

                state.error != null -> {
                    ErrorOverlay(
                        isNetworkError = false,
                        onRetry = { viewModel.setBikeId(bikeId) }
                    )
                }

                else -> {
                    DetailPublicBikeContent(
                        modifier = Modifier.fillMaxSize(),
                        state = state
                    )
                }
            }
        }
    }
}

@Composable
fun DetailPublicBikeContent(
    modifier: Modifier = Modifier,
    state: DetailPublicBikeState
) {

    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(
            start = spacing.medium,
            end = spacing.medium
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {

        // Images
        item {

            PhotosContent(
                photos = state.bike?.photoUrls?.map { url ->
                    PhotoItem.Remote(
                        id = url,
                        url = url
                    )
                } ?: emptyList(),
                onAddPhotoClick = {},
                onRemovePhoto = {},
                onPhotoEdited = { _, _ -> },
                onMovePhoto = { _, _ -> },
                isEditable = false
            )
        }

        // Title / Price / City
        item {

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                Column(
                    Modifier.padding(16.dp)
                ) {

                    Text(
                        state.bike?.title ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "${state.bike?.location?.city} ${state.bike?.location?.postalCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "${state.bike?.priceInCents} / jour",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Owner
        item {
            state.owner?.let {
                OwnerCard(it)
            }
        }

        // Characteristics
        item {
            BikeSpecsCard(state.bike!!)
        }

        // Description
        item {
            DescriptionCard(state.bike?.description ?: "")
        }

        // Pricing
        item {
            PriceTableCard(state.bike!!)
        }
    }
}

@PreviewLightDark
@Composable
private fun DetailPublicBikeScreenPreview() {
    LokaveloTheme {
        val fakeBike =
            Bike(
                id = "1",
                title = "Vélo gravel Origine Trail Explore",
                description = "Vélo en super état avec fourche suspendue",
                category = BikeCategory.GRAVEL,
                brand = "Origine",
                condition = BikeCondition.LIKE_NEW,
                accessories = listOf(
                    BikeEquipment.HANDLEBAR_BAG, BikeEquipment.MUDGUARD, BikeEquipment.BELL,
                    BikeEquipment.REFLECTIVE_VEST
                ),
                priceInCents = 2500,
                priceHalfDayInCents = 1250,
                priceWeekInCents = 10000,
                priceMonthInCents = 25000,
                depositInCents = 50000,
                location = BikeLocation(
                    street = "4 bd Longchamp",
                    postalCode = "13001",
                    city = "Marseille"
                )
            )

        val previewState = DetailPublicBikeState(
            bike = fakeBike
        )

        DetailPublicBikeContent(
            state = previewState
        )
    }
}