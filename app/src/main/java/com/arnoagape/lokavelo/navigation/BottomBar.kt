package com.arnoagape.lokavelo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@Composable
fun BottomBar(
    currentScreen: Screen,
    unreadMessages: Int,
    pendingLocations: Int,
    onItemSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {

        NavigationBarItem(
            selected = currentScreen is Screen.Main.Map,
            onClick = { onItemSelected(Screen.Main.Map) },
            icon = { Icon(Icons.Default.Search, null) },
            label = { Text(stringResource(R.string.rent)) }
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Owner.HomeBike,
            onClick = { onItemSelected(Screen.Owner.HomeBike) },
            icon = {
                BadgedBox(
                    badge = {
                        if (pendingLocations > 0) {
                            Badge {
                                Text(pendingLocations.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsBike, null)
                }
            },
            label = { Text(stringResource(R.string.rentals)) }
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Messaging.Home,
            onClick = { onItemSelected(Screen.Messaging.Home) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadMessages > 0) {
                            Badge {
                                Text(unreadMessages.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, null)
                }
            },
            label = { Text(stringResource(R.string.messaging)) }
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Account.AccountHome,
            onClick = { onItemSelected(Screen.Account.AccountHome) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text(stringResource(R.string.account)) }
        )
    }
}

@PreviewLightDark
@Composable
private fun BottomBarPreview() {
    LokaveloTheme{
        BottomBar(
            currentScreen = Screen.Owner.HomeBike,
            unreadMessages = 3,
            pendingLocations = 1,
            onItemSelected = {}
        )
    }
}