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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.login.LoginScreen
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
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

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    var lastBackPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val resources = LocalResources.current

    BackHandler {

        val currentRoute =
            currentBackStackEntry?.destination?.route?.substringBefore("/")

        val isOnRoot =
            currentRoute == "main_graph"

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
            navController.navigate(Screen.Login.route)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "main_graph"
    ) {

        // ---------------- MAIN GRAPH (avec bottom bar) ----------------

        composable("main_graph") {
            MainScreen(
                rootNavController = navController,
                navigateProtected = { route ->
                    navigateProtected(route)
                }
            )
        }

        // ---------------- PLEIN ÉCRAN ----------------

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
        ) { backStackEntry ->

            val bikeId =
                backStackEntry.arguments?.getString("bikeId")!!

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
        ) { backStackEntry ->

            val bikeId =
                backStackEntry.arguments?.getString("bikeId")!!

            val vm: EditBikeViewModel = hiltViewModel()

            EditBikeScreen(
                bikeId = bikeId,
                viewModel = vm,
                onSaveClick = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

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
    return when (route?.substringBefore("/")) {

        Screen.Owner.HomeBike.route -> Screen.Owner.HomeBike
        Screen.Account.AccountHome.route -> Screen.Account.AccountHome
        Screen.Messaging.MessagingHome.route -> Screen.Messaging.MessagingHome
        Screen.Login.route -> Screen.Login
        Screen.Main.Map.route -> Screen.Main.Map

        Screen.Owner.AddBike.route -> Screen.Owner.AddBike

        "owner_detail" -> Screen.Owner.DetailBike
        "owner_edit" -> Screen.Owner.EditBike

        else -> null
    }
}