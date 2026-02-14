package com.arnoagape.lokavelo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arnoagape.lokavelo.navigation.Screen
import com.arnoagape.lokavelo.ui.screen.account.profile.ProfileScreen
import com.arnoagape.lokavelo.ui.screen.home.home.HomeScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
import com.arnoagape.lokavelo.ui.screen.login.launchers.emailSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeScreen
import com.arnoagape.lokavelo.ui.screen.login.launchers.googleSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.login.launchers.phoneSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeViewModel

@Composable
fun LokaveloApp() {

    var backStack by remember { mutableStateOf(listOf<Screen>(Screen.Login)) }

    val currentScreen = backStack.last()

    // Sign-in launchers
    val emailSignUpLauncher = emailSignUpLauncher(hiltViewModel<LoginViewModel>())
    val googleSignUpLauncher = googleSignUpLauncher(hiltViewModel<LoginViewModel>())
    val phoneSignUpLauncher = phoneSignUpLauncher(hiltViewModel<LoginViewModel>())

    // navigate to
    fun navigate(screen: Screen) {
        backStack = backStack + screen
    }

    // go back
    fun popBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

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
            onLoginSuccess = { navigate(Screen.Owner.AddBike)}
        )

        // MESSAGING
        is Screen.Messaging.MessagingDetail -> TODO()
        is Screen.Messaging.MessagingHome -> TODO()

        // OWNER
        is Screen.Owner.AddBike ->
            AddBikeScreen(
                viewModel = hiltViewModel<AddBikeViewModel>(),
                onBackClick = { popBack() },
                onSaveClick = { navigate(Screen.Owner.HomeBike) }
            )
        is Screen.Owner.DetailBike -> TODO()
        is Screen.Owner.EditBike -> TODO()
        is Screen.Owner.HomeBike -> TODO()

        // RENT
        is Screen.Rent -> TODO()
    }
}