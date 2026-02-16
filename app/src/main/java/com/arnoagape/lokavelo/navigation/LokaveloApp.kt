package com.arnoagape.lokavelo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.account.home.AccountHomeScreen
import com.arnoagape.lokavelo.ui.screen.account.profile.ProfileScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.help.HelpSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.home.HomeSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.info.InfoSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.notifications.NotificationsSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.payment.PaymentSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.version.VersionSettingsScreen
import com.arnoagape.lokavelo.ui.screen.main.home.HomeScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeScreen
import com.arnoagape.lokavelo.ui.screen.main.contact.ContactScreen
import com.arnoagape.lokavelo.ui.screen.main.detail.DetailPublicBikeScreen
import com.arnoagape.lokavelo.ui.screen.main.profile.PublicProfileScreen
import com.arnoagape.lokavelo.ui.screen.messaging.detail.MessagingDetailScreen
import com.arnoagape.lokavelo.ui.screen.messaging.home.MessagingHomeScreen
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeEvent
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeViewModel
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PublishButton
import com.arnoagape.lokavelo.ui.screen.owner.detail.DetailBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.detail.DetailBikeViewModel
import com.arnoagape.lokavelo.ui.screen.owner.editBike.EditBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.home.HomeBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.home.HomeBikeViewModel
import com.arnoagape.lokavelo.ui.screen.rent.RentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LokaveloApp() {

    // BackStack sauvegardé en String
    var backStack by rememberSaveable {
        mutableStateOf(listOf(Screen.Login.route))
    }

    val currentRoute = backStack.last()
    val currentScreen = remember(currentRoute) {
        screenFromRoute(currentRoute)
    }

    // ViewModels
    val loginViewModel: LoginViewModel = hiltViewModel()
    val addBikeViewModel: AddBikeViewModel = hiltViewModel()
    val homeBikeViewModel: HomeBikeViewModel = hiltViewModel()
    val detailBikeViewModel: DetailBikeViewModel = hiltViewModel()

    // States
    val isSignedIn by loginViewModel.isSignedIn.collectAsStateWithLifecycle()
    val addBikeState by addBikeViewModel.state.collectAsStateWithLifecycle()
    val homeBikeScreenState by homeBikeViewModel.state.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation helpers
    fun navigate(screen: Screen) {
        if (backStack.last() != screen.route) {
            backStack = backStack + screen.route
        }
    }

    fun navigateProtected(screen: Screen) {
        if (isSignedIn) {
            navigate(screen)
        } else {
            backStack = listOf(Screen.Login.route)
        }
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            when (currentScreen) {

                is Screen.Owner.AddBike -> {
                    TopAppBar(
                        title = { Text(stringResource(R.string.add_bike)) },
                        navigationIcon = {
                            IconButton(onClick = { popBack() }) {
                                Icon(
                                    Icons.Default.Close,
                                    stringResource(R.string.close)
                                )
                            }
                        }
                    )
                }

                is Screen.Owner.HomeBike -> {
                    if (homeBikeScreenState.isSearchActive) {
                        EmbeddedSearchBar(
                            query = homeBikeScreenState.searchQuery,
                            onQueryChange = homeBikeViewModel::onSearchQueryChange,
                            onClose = homeBikeViewModel::toggleSearch,
                            modifier = Modifier
                                .padding(
                                    top = 42.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                )
                                .focusRequester(focusRequester)
                        )
                    } else {
                        TopAppBar(
                            title = { Text(stringResource(R.string.rentals)) },
                            actions = {

                                // 🔍 Search
                                if (!homeBikeScreenState.selection.isSelectionMode) {
                                    IconButton(onClick = homeBikeViewModel::toggleSearch) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = stringResource(R.string.search)
                                        )
                                    }
                                }

                                // 🗑 Delete
                                if (homeBikeScreenState.selection.isSelectionMode) {
                                    IconButton(
                                        onClick = {
                                            if (homeBikeScreenState.selection.selectedIds.isEmpty()) {
                                                homeBikeViewModel.exitSelectionMode()
                                            } else {
                                                homeBikeViewModel.requestDeleteConfirmation()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector =
                                                if (homeBikeScreenState.selection.selectedIds.isEmpty())
                                                    Icons.Default.Close
                                                else
                                                    Icons.Default.DeleteForever,
                                            contentDescription = stringResource(R.string.close)
                                        )
                                    }
                                } else {
                                    IconButton(onClick = homeBikeViewModel::enterSelectionMode) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                is Screen.Owner.DetailBike -> {
                    TopAppBar(
                        title = { Text(stringResource(R.string.detail_bike)) },
                        navigationIcon = {
                            IconButton(onClick = { popBack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    stringResource(R.string.cd_go_back)
                                )
                            }
                        }
                    )
                }

                else -> {}
            }
        },

        floatingActionButton = {
            if (currentScreen is Screen.Owner.HomeBike) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        navigateProtected(Screen.Owner.AddBike)
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        stringResource(R.string.add_bike)
                    )
                }
            }
        },

        bottomBar = {
            when (currentScreen) {

                is Screen.Owner.AddBike -> {
                    PublishButton(
                        enabled = addBikeState.isValid,
                        onClick = {
                            addBikeViewModel.onAction(AddBikeEvent.Submit)
                        }
                    )
                }

                else -> {
                    BottomBar(
                        currentScreen = currentScreen,
                        onItemSelected = { screen ->
                            backStack = listOf(screen.route)
                        }
                    )
                }
            }
        }

    ) { padding ->

        Box(
            Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {

            when (currentScreen) {
                // ACCOUNT
                is Screen.Account.AccountHome -> AccountHomeScreen()
                is Screen.Account.Profile -> ProfileScreen()

                // ACCOUNT - SETTINGS
                is Screen.Account.Settings.HelpSettings -> HelpSettingsScreen()
                is Screen.Account.Settings.HomeSettings -> HomeSettingsScreen()
                is Screen.Account.Settings.InfoSettings -> InfoSettingsScreen()
                is Screen.Account.Settings.NotificationsSettings -> NotificationsSettingsScreen()
                is Screen.Account.Settings.PaymentSettings -> PaymentSettingsScreen()
                is Screen.Account.Settings.VersionSettings -> VersionSettingsScreen()

                // HOME
                is Screen.Main.Home -> HomeScreen()
                is Screen.Main.Contact -> ContactScreen()
                is Screen.Main.DetailPublicBike -> DetailPublicBikeScreen()
                is Screen.Main.PublicProfile -> PublicProfileScreen()

                // LOGIN
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = { navigate(Screen.Owner.HomeBike) }
                )

                // MESSAGING
                is Screen.Messaging.MessagingDetail -> MessagingDetailScreen()
                is Screen.Messaging.MessagingHome -> MessagingHomeScreen()

                // OWNER
                is Screen.Owner.AddBike ->
                    AddBikeScreen(
                        viewModel = addBikeViewModel,
                        onSaveClick = { navigate(Screen.Owner.HomeBike) }
                    )

                is Screen.Owner.DetailBike ->
                    DetailBikeScreen(
                        bikeId = currentScreen.bikeId,
                        viewModel = detailBikeViewModel
                    )

                is Screen.Owner.EditBike -> EditBikeScreen()
                is Screen.Owner.HomeBike ->
                    HomeBikeScreen(
                        viewModel = homeBikeViewModel,
                        onBikeClick = { bike -> navigate(Screen.Owner.DetailBike(bike.id)) }
                    )

                // RENT
                is Screen.Rent -> RentScreen()
            }
        }
    }
}

fun screenFromRoute(route: String): Screen {
    return when {
        route == Screen.Login.route -> Screen.Login

        route == Screen.Owner.HomeBike.route -> Screen.Owner.HomeBike
        route == Screen.Owner.AddBike.route -> Screen.Owner.AddBike
        route == Screen.Owner.EditBike.route -> Screen.Owner.EditBike
        route.startsWith("owner_detail/") ->
            Screen.Owner.DetailBike.fromRoute(route)

        route == Screen.Account.AccountHome.route ->
            Screen.Account.AccountHome

        route == Screen.Account.Profile.route ->
            Screen.Account.Profile

        route == Screen.Main.Home.route ->
            Screen.Main.Home

        route == Screen.Messaging.MessagingHome.route ->
            Screen.Messaging.MessagingHome

        route == Screen.Rent.route ->
            Screen.Rent

        else -> Screen.Login
    }
}