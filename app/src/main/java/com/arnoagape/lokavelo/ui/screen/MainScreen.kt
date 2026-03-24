package com.arnoagape.lokavelo.ui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arnoagape.lokavelo.navigation.AppNavigationBar
import com.arnoagape.lokavelo.navigation.Screen
import com.arnoagape.lokavelo.navigation.screenFromRoute
import com.arnoagape.lokavelo.navigation.stringArg
import com.arnoagape.lokavelo.ui.screen.account.home.AccountHomeScreen
import com.arnoagape.lokavelo.ui.screen.account.profile.ProfileScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.help.HelpSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.home.HomeSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.info.InfoSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.notifications.NotificationsSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.payment.PaymentSettingsScreen
import com.arnoagape.lokavelo.ui.screen.account.settings.version.VersionSettingsScreen
import com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike.HomeBikeScreen
import com.arnoagape.lokavelo.ui.screen.bikes.owner.homeBike.HomeBikeViewModel
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.main.map.MapScreen
import com.arnoagape.lokavelo.ui.screen.main.map.MapViewModel
import com.arnoagape.lokavelo.ui.screen.messaging.home.MessagingHomeScreen
import com.arnoagape.lokavelo.ui.screen.messaging.home.MessagingHomeViewModel
import com.arnoagape.lokavelo.ui.screen.bikes.rental.HomeRentalScreen
import java.time.ZoneId

@Composable
fun MainScreen(
    rootNavController: NavController,
    navigateProtected: (Screen) -> Unit,
    isSignedIn: Boolean,
    mapViewModel: MapViewModel,
    homeBikeVm: HomeBikeViewModel,
    messagingVm: MessagingHomeViewModel
) {

    val tabNavController = rememberNavController()
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val currentRoute = tabNavController.currentBackStackEntryAsState().value?.destination?.route

    val unreadMessages by messagingVm.unreadCount.collectAsStateWithLifecycle()
    val pendingLocations by homeBikeVm.pendingCount.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            if (!isKeyboardVisible) {
                AppNavigationBar(
                    currentScreen = screenFromRoute(currentRoute) ?: Screen.Main.Map,
                    unreadMessages = unreadMessages,
                    pendingLocations = pendingLocations,
                    onItemSelected = { screen ->

                        val requiresAuth = screen is Screen.Owner.HomeBike
                                || screen is Screen.Messaging.Home
                                || screen is Screen.Account.AccountHome
                                || screen is Screen.Rental.HomeRental

                        if (requiresAuth && !isSignedIn) {

                            navigateProtected(screen)

                        } else {

                            tabNavController.navigate(screen.route) {
                                popUpTo(tabNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }

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

            composable(
                route = Screen.Login.route,
                arguments = listOf(
                    navArgument("redirect") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->

                val redirect = entry.stringArg("redirect")

                LoginScreen(
                    onLoginSuccess = {

                        if (redirect != null) {
                            rootNavController.navigate(redirect) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            rootNavController.popBackStack()
                        }
                    }
                )
            }

            // ---------------- RENTAL ----------------

            composable(Screen.Rental.HomeRental.route) {

                HomeRentalScreen(
                    viewModel = homeBikeVm
                )
            }

            // ---------------- OWNER ----------------

            composable(Screen.Owner.HomeBike.route) {

                HomeBikeScreen(
                    viewModel = homeBikeVm,
                    onBikeClick = { item ->
                        rootNavController.navigate(
                            Screen.Owner.DetailBike.createRoute(item.bike.id)
                        )
                    },
                    onAddBikeClick = { navigateProtected(Screen.Owner.AddBike) }
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

                MapScreen(
                    viewModel = mapViewModel,
                    onBikeClick = { bikeId, startDate, endDate ->

                        val start = startDate
                            ?.atStartOfDay(ZoneId.systemDefault())
                            ?.toInstant()
                            ?.toEpochMilli()

                        val end = endDate
                            ?.atStartOfDay(ZoneId.systemDefault())
                            ?.toInstant()
                            ?.toEpochMilli()

                        rootNavController.navigate(
                            Screen.Main.DetailPublicBike.createRoute(
                                bikeId,
                                start,
                                end
                            )
                        )
                    }
                )
            }

            // ---------------- MESSAGING ----------------

            composable(Screen.Messaging.Home.route) {

                MessagingHomeScreen(
                    viewModel = messagingVm,
                    onConversationClick = { conversationId ->
                        rootNavController.navigate(
                            Screen.Messaging.Detail.createRoute(conversationId)
                        )
                    }
                )
            }
        }

    }
}