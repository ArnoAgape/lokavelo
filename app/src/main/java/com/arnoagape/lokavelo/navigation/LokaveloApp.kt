package com.arnoagape.lokavelo.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
import com.arnoagape.lokavelo.ui.screen.main.contact.ContactScreen
import com.arnoagape.lokavelo.ui.screen.main.detail.DetailPublicBikeScreen
import com.arnoagape.lokavelo.ui.screen.main.detail.DetailPublicBikeViewModel
import com.arnoagape.lokavelo.ui.screen.messaging.detail.MessagingDetailScreen
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.addBike.AddBikeViewModel
import com.arnoagape.lokavelo.ui.screen.owner.detail.DetailBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.detail.DetailBikeViewModel
import com.arnoagape.lokavelo.ui.screen.owner.editBike.EditBikeScreen
import com.arnoagape.lokavelo.ui.screen.owner.editBike.EditBikeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LokaveloApp() {

    val navController = rememberNavController()

    val loginViewModel: LoginViewModel = hiltViewModel()
    val isSignedIn by loginViewModel.isSignedIn.collectAsStateWithLifecycle()

    var lastBackPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val resources = LocalResources.current

    BackHandler {

        val isOnRoot = navController.previousBackStackEntry == null

        if (!isOnRoot) {
            navController.popBackStack()
        } else {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastBackPressedTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressedTime = currentTime
                Toast.makeText(
                    context,
                    resources.getString(R.string.press_again_exit),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun navigateProtected(route: String) {

        if (isSignedIn) {
            navController.navigate(route)
        } else {
            navController.navigate(
                Screen.Login.createRoute(route)
            )
        }
    }

    fun navigateProtected(screen: Screen) {
        navigateProtected(screen.route)
    }

    NavHost(
        navController = navController,
        startDestination = "main_graph"
    ) {

        // ---------------- MAIN GRAPH (with bottom bar) ----------------

        composable("main_graph") {
            MainScreen(
                rootNavController = navController,
                navigateProtected = { screen ->
                    navigateProtected(screen)
                }
            )
        }

        // ---------------- OWNER ----------------

        composable(Screen.Owner.AddBike.route) {

            val vm: AddBikeViewModel = hiltViewModel()

            AddBikeScreen(
                viewModel = vm,
                onSaveClick = { navController.popBackStack() },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Owner.DetailBike.route,
            arguments = listOf(
                navArgument("bikeId") { type = NavType.StringType }
            )
        ) { entry ->

            val bikeId = entry.stringArg("bikeId") ?: return@composable

            val vm: DetailBikeViewModel = hiltViewModel()

            DetailBikeScreen(
                bikeId = bikeId,
                viewModel = vm,
                onBikeDeleted = { navController.popBackStack() },
                onEditClick = {
                    navController.navigate(
                        Screen.Owner.EditBike.createRoute(bikeId)
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Owner.EditBike.route,
            arguments = listOf(
                navArgument("bikeId") { type = NavType.StringType }
            )
        ) { entry ->

            val bikeId = entry.stringArg("bikeId") ?: return@composable

            val vm: EditBikeViewModel = hiltViewModel()

            EditBikeScreen(
                bikeId = bikeId,
                viewModel = vm,
                onSaveClick = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- MAIN ----------------

        composable(
            route = Screen.Main.DetailPublicBike.route,
            arguments = listOf(
                navArgument("bikeId") { type = NavType.StringType },
                navArgument("start") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("end") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->

            val bikeId = entry.stringArg("bikeId") ?: return@composable
            val start = entry.localDateArg("start") ?: return@composable
            val end = entry.localDateArg("end") ?: return@composable

            val vm: DetailPublicBikeViewModel = hiltViewModel()

            DetailPublicBikeScreen(
                bikeId = bikeId,
                viewModel = vm,
                startDate = start,
                endDate = end,
                onBack = { navController.popBackStack() },
                onContactClick = { bikeId, start, end ->

                    navigateProtected(
                        Screen.Main.Contact.createRoute(
                            bikeId,
                            start,
                            end
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.Main.Contact.route,
            arguments = listOf(
                navArgument("bikeId") { type = NavType.StringType },
                navArgument("start") { type = NavType.LongType },
                navArgument("end") { type = NavType.LongType }
            )
        ) { entry ->

            val bikeId = entry.stringArg("bikeId") ?: return@composable
            val start = entry.localDateArg("start") ?: return@composable
            val end = entry.localDateArg("end") ?: return@composable

            ContactScreen(
                bikeId = bikeId,
                startDate = start,
                endDate = end,
                onConversationCreated = { conversationId ->
                    navController.navigate(Screen.Messaging.Detail.createRoute(conversationId))
                    {
                        popUpTo(Screen.Main.Contact.route) {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- MESSAGING ----------------

        composable(
            route = Screen.Messaging.Detail.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val conversationId =
                backStackEntry.arguments?.getString("conversationId")
                    ?: return@composable

            MessagingDetailScreen(
                conversationId = conversationId,
                onBack = { navigateProtected(Screen.Messaging.Home.route) }
            )
        }

        // ---------------- LOGIN ----------------

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}

fun screenFromRoute(route: String?): Screen? {

    val baseRoute = route?.substringBefore("/") ?: return null

    return when (baseRoute) {

        Screen.Owner.HomeBike.route -> Screen.Owner.HomeBike
        Screen.Main.Map.route -> Screen.Main.Map
        Screen.Messaging.Home.route -> Screen.Messaging.Home
        Screen.Account.AccountHome.route -> Screen.Account.AccountHome

        else -> null
    }
}