package com.arnoagape.lokavelo.navigation

sealed interface Screen

data object Home : Screen
data object Login : Screen
data object Profile : Screen
data object AddBike : Screen
