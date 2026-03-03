package com.arnoagape.lokavelo.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arnoagape.lokavelo.ui.screen.account.home.AccountHomeScreen
import com.arnoagape.lokavelo.ui.screen.account.profile.ProfileScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.help.HelpSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.home.HomeSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.info.InfoSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.notifications.NotificationsSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.payment.PaymentSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.version.VersionSettingsScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.main.contact.ContactScreen
import com.arnoagape.lokavelo.ui.screen.main.detail.DetailPublicBikeScreen
import com.arnoagape.lokavelo.ui.screen.main.map.MapScreen
import com.arnoagape.lokavelo.ui.screen.main.map.MapViewModel
import com.arnoagape.lokavelo.ui.screen.main.profile.PublicProfileScreen
import com.arnoagape.lokavelo.ui.screen.messaging.detail.MessagingDetailScreen
import com.arnoagape.lokavelo.ui.screen.messaging.home.MessagingHomeScreen
import com.arnoagape.lokavelo.ui.screen.owner.home.HomeBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.home.HomeBikeViewModel

@Composable
fun MainScreen(
    rootNavController: NavController,
    navigateProtected: (String) -> Unit
) {

    val tabNavController = rememberNavController()
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (!isKeyboardVisible) {
                BottomBar(
                    currentScreen = screenFromRoute(currentRoute) ?: Screen.Owner.HomeBike,
                    onItemSelected = { screen ->
                        tabNavController.navigate(screen.route) {
                            popUpTo(tabNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = tabNavController,
            startDestination = Screen.Main.Map.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ---------------- LOGIN ----------------

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        tabNavController.navigate(Screen.Owner.HomeBike.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ---------------- OWNER ----------------

            composable(Screen.Owner.HomeBike.route) {

                val vm: HomeBikeViewModel = hiltViewModel()

                HomeBikeScreen(
                    viewModel = vm,
                    onBikeClick = { bike ->
                        rootNavController.navigate(
                            Screen.Owner.DetailBike.createRoute(bike.id)
                        )
                    },
                    onAddBikeClick = { navigateProtected(Screen.Owner.AddBike.route) }
                )
            }

            // ---------------- ACCOUNT ----------------

            composable(Screen.Account.AccountHome.route) {
                AccountHomeScreen()
                LoginScreen(
                    onLoginSuccess = {
                        tabNavController.navigate(Screen.Owner.HomeBike.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Account.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.Account.Settings.HelpSettings.route) {
                HelpSettingsScreen()
            }

            composable(Screen.Account.Settings.HomeSettings.route) {
                HomeSettingsScreen()
            }

            composable(Screen.Account.Settings.InfoSettings.route) {
                InfoSettingsScreen()
            }

            composable(Screen.Account.Settings.NotificationsSettings.route) {
                NotificationsSettingsScreen()
            }

            composable(Screen.Account.Settings.PaymentSettings.route) {
                PaymentSettingsScreen()
            }

            composable(Screen.Account.Settings.VersionSettings.route) {
                VersionSettingsScreen()
            }

            // ---------------- MAIN ----------------

            composable(Screen.Main.Map.route) {

                val vm: MapViewModel = hiltViewModel()
                MapScreen(
                    viewModel = vm
                )
            }

            composable(Screen.Main.Contact.route) {
                ContactScreen()
            }

            composable(Screen.Main.DetailPublicBike.route) {
                DetailPublicBikeScreen()
            }

            composable(Screen.Main.PublicProfile.route) {
                PublicProfileScreen()
            }

            // ---------------- MESSAGING ----------------

            composable(Screen.Messaging.MessagingHome.route) {
                MessagingHomeScreen()
            }

            composable(Screen.Messaging.MessagingDetail.route) {
                MessagingDetailScreen()
            }
        }

    }
}