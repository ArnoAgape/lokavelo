package com.arnoagape.lokavelo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.account.profile.ProfileScreen
import com.arnoagape.lokavelo.ui.screen.home.home.HomeScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
import com.arnoagape.lokavelo.ui.screen.login.launchers.emailSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeScreen
import com.arnoagape.lokavelo.ui.screen.login.launchers.googleSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.login.launchers.phoneSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeEvent
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeViewModel
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PublishButton
import com.arnoagape.lokavelo.ui.screen.owner.home.HomeBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.home.HomeBikeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LokaveloApp() {

    var backStack by remember { mutableStateOf(listOf<Screen>(Screen.Login)) }

    val currentScreen = backStack.last()

    // viewModels
    val loginViewModel: LoginViewModel = hiltViewModel()
    val addBikeViewModel: AddBikeViewModel = hiltViewModel()

    // Sign-in launchers
    val emailSignUpLauncher = emailSignUpLauncher(loginViewModel)
    val googleSignUpLauncher = googleSignUpLauncher(loginViewModel)
    val phoneSignUpLauncher = phoneSignUpLauncher(loginViewModel)

    // states
    val isSignedIn by loginViewModel.isSignedIn.collectAsStateWithLifecycle()
    val addBikeState by addBikeViewModel.state.collectAsStateWithLifecycle()

    // navigate to
    fun navigate(screen: Screen) {
        if (backStack.last() != screen) {
            backStack = backStack + screen
        }
    }

    // checks if user signed in
    fun navigateProtected(screen: Screen) {
        if (isSignedIn) {
            navigate(screen)
        } else {
            backStack = listOf(Screen.Login)
        }
    }

    // go back
    fun popBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    Scaffold(
        topBar = {
            when (currentScreen) {

                is Screen.Owner.AddBike -> {
                    TopAppBar(
                        title = { Text(stringResource(R.string.add_bike)) },
                        navigationIcon = {
                            IconButton(onClick = { popBack() }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    )
                }

                is Screen.Owner.HomeBike -> {
                    TopAppBar(
                        title = { Text(stringResource(R.string.rentals)) }
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
                        onClick = { addBikeViewModel.onAction(AddBikeEvent.Submit) }
                    )
                }

                else -> {
                    BottomBar(
                        currentScreen = currentScreen,
                        onItemSelected = { screen ->
                            backStack = listOf(screen)
                        }
                    )
                }
            }
        }
    )
    { padding ->

        Box(
            Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {

        when (currentScreen) {
                // ACCOUNT
                is Screen.Account.AccountHome -> TODO()
                is Screen.Account.Profile -> ProfileScreen()

                // ACCOUNT - SETTINGS
                is Screen.Account.Settings.HelpSettings -> TODO()
                is Screen.Account.Settings.HomeSettings -> TODO()
                is Screen.Account.Settings.InfoSettings -> TODO()
                is Screen.Account.Settings.NotificationsSettings -> TODO()
                is Screen.Account.Settings.PaymentSettings -> TODO()
                is Screen.Account.Settings.VersionSettings -> TODO()

                // HOME
                is Screen.Main.Home -> HomeScreen()
                is Screen.Main.Contact -> TODO()
                is Screen.Main.DetailPublicBike -> TODO()
                is Screen.Main.PublicProfile -> TODO()

                // LOGIN
                is Screen.Login -> LoginScreen(
                    onGoogleSignInClick = { googleSignUpLauncher() },
                    onEmailSignInClick = { emailSignUpLauncher() },
                    onPhoneSignInClick = { phoneSignUpLauncher() },
                    onLoginSuccess = { navigate(Screen.Owner.HomeBike) }
                )

                // MESSAGING
                is Screen.Messaging.MessagingDetail -> TODO()
                is Screen.Messaging.MessagingHome -> TODO()

                // OWNER
                is Screen.Owner.AddBike ->
                    AddBikeScreen(
                        viewModel = hiltViewModel<AddBikeViewModel>(),
                        onSaveClick = { navigate(Screen.Owner.HomeBike) }
                    )

                is Screen.Owner.DetailBike -> TODO()
                is Screen.Owner.EditBike -> TODO()
                is Screen.Owner.HomeBike ->
                    HomeBikeScreen(
                        viewModel = hiltViewModel<HomeBikeViewModel>(),
                        onBikeClick = { bike -> navigate(Screen.Owner.DetailBike(bike.id)) }
                    )

                // RENT
                is Screen.Rent -> TODO()
            }
        }
    }
}