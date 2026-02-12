package com.arnoagape.lokavelo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arnoagape.lokavelo.navigation.AddBike
import com.arnoagape.lokavelo.navigation.Home
import com.arnoagape.lokavelo.navigation.Login
import com.arnoagape.lokavelo.navigation.Profile
import com.arnoagape.lokavelo.navigation.Screen
import com.arnoagape.lokavelo.ui.screen.account.profile.ProfileScreen
import com.arnoagape.lokavelo.ui.screen.home.home.HomeScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
import com.arnoagape.lokavelo.ui.screen.login.launchers.rememberEmailSignUpLauncher
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeScreen
import com.arnoagape.lokavelo.ui.screen.login.launchers.rememberGoogleSignUpLauncher

@Composable
fun LokaveloApp() {

    var backStack by remember { mutableStateOf(listOf<Screen>(Login)) }

    val currentScreen = backStack.last()

    // Sign-in launchers
    val emailSignUpLauncher = rememberEmailSignUpLauncher(hiltViewModel<LoginViewModel>())
    val googleSignUpLauncher = rememberGoogleSignUpLauncher(hiltViewModel<LoginViewModel>())

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
        is Home -> HomeScreen()

        is Login -> LoginScreen(
            onGoogleSignInClick = { googleSignUpLauncher() },
            onEmailSignInClick = { emailSignUpLauncher() },
            onLoginSuccess = { }
        )

        is Profile -> ProfileScreen()

        is AddBike -> AddBikeScreen()
    }
}