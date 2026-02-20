package com.arnoagape.lokavelo.navigation

sealed interface Screen {

    val route: String

    // ---------------- ACCOUNT ----------------
    sealed interface Account : Screen {

        data object AccountHome : Account {
            override val route = "account_home"
        }

        data object Profile : Account {
            override val route = "account_profile"
        }

        sealed interface Settings : Account {

            data object HelpSettings : Settings {
                override val route = "settings_help"
            }

            data object HomeSettings : Settings {
                override val route = "settings_home"
            }

            data object InfoSettings : Settings {
                override val route = "settings_info"
            }

            data object NotificationsSettings : Settings {
                override val route = "settings_notifications"
            }

            data object PaymentSettings : Settings {
                override val route = "settings_payment"
            }

            data object VersionSettings : Settings {
                override val route = "settings_version"
            }
        }
    }

    // ---------------- MAIN ----------------
    sealed interface Main : Screen {

        data object Home : Main {
            override val route = "main_home"
        }

        data object DetailPublicBike : Main {
            override val route = "main_detail_public"
        }

        data object PublicProfile : Main {
            override val route = "main_public_profile"
        }

        data object Contact : Main {
            override val route = "main_contact"
        }
    }

    // ---------------- LOGIN ----------------
    data object Login : Screen {
        override val route = "login"
    }

    // ---------------- MESSAGING ----------------
    sealed interface Messaging : Screen {

        data object MessagingHome : Messaging {
            override val route = "messaging_home"
        }

        data object MessagingDetail : Messaging {
            override val route = "messaging_detail"
        }
    }

    // ---------------- OWNER ----------------
    sealed interface Owner : Screen {

        data object AddBike : Owner {
            override val route = "owner_add"
        }

        data object HomeBike : Owner {
            override val route = "owner_home"
        }

        // 🔹 DETAIL BIKE
        data object DetailBike : Owner {
            override val route = "owner_detail/{bikeId}"

            fun createRoute(bikeId: String): String =
                "owner_detail/$bikeId"
        }

        // 🔹 EDIT BIKE
        data object EditBike : Owner {
            override val route = "owner_edit/{bikeId}"

            fun createRoute(bikeId: String): String =
                "owner_edit/$bikeId"
        }
    }

    // ---------------- RENT ----------------
    data object Rent : Screen {
        override val route = "rent"
    }
}