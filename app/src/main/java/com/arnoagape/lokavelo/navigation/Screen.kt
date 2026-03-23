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

        data object Map : Main {
            override val route = "main_map"
        }

        data object DetailPublicBike : Main {
            override val route =
                "main_detail_public/{bikeId}?start={start}&end={end}"

            fun createRoute(
                bikeId: String,
                start: Long?,
                end: Long?
            ): String {
                val startParam = start ?: -1
                val endParam = end ?: -1

                return "main_detail_public/$bikeId?start=$startParam&end=$endParam"
            }
        }

        data object PublicProfile : Main {
            override val route = "main_public_profile"
        }

        data object Contact : Main {
            override val route = "contact/{bikeId}?start={start}&end={end}"
            fun createRoute(
                bikeId: String,
                start: Long,
                end: Long
            ) = "contact/$bikeId?start=$start&end=$end"
        }
    }

    // ---------------- LOGIN ----------------
    data object Login : Screen {

        override val route = "login?redirect={redirect}"

        fun createRoute(redirect: String) =
            "login?redirect=$redirect"
    }

    // ---------------- MESSAGING ----------------
    sealed interface Messaging : Screen {

        data object Home : Messaging {
            override val route = "messaging_home"
        }

        data object Detail : Messaging {
            override val route = "messaging_detail/{conversationId}"

            fun createRoute(conversationId: String) =
                "messaging_detail/$conversationId"
        }
    }

    // ---------------- RENTAL ----------------
    sealed interface Rental : Screen {

        data object HomeRental : Rental {
            override val route = "rental_home"
        }

        data object DetailRental : Rental {
            override val route = "rental_detail/{bikeId}"

            fun createRoute(bikeId: String): String =
                "rental_detail/$bikeId"
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
}