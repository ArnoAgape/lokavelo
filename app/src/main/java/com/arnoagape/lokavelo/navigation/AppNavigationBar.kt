package com.arnoagape.lokavelo.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.NavigationBarItem
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

@Composable
fun AppNavigationBar(
    currentScreen: Screen,
    unreadMessages: Int,
    pendingLocations: Int,
    onItemSelected: (Screen) -> Unit
) {

    Box {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AnimatedNavItem(
                selected = currentScreen is Screen.Owner.HomeBike,
                onClick = { onItemSelected(Screen.Owner.HomeBike) },
                icon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null) },
                label = { Text(stringResource(R.string.garage)) }
            )

            AnimatedNavItem(
                selected = currentScreen is Screen.Rental.HomeRental,
                onClick = { onItemSelected(Screen.Rental.HomeRental) },
                icon = {
                    BadgedBox(badge = {
                        if (pendingLocations > 0) Badge { Text(pendingLocations.toString()) }
                    }) {
                        Icon(Icons.Default.CalendarMonth, null)
                    }
                },
                label = { Text(stringResource(R.string.rentals)) }
            )

            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Box(Modifier.size(72.dp)) },
                label = {},
                enabled = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    disabledIconColor = Color.Transparent
                )
            )

            AnimatedNavItem(
                selected = currentScreen is Screen.Messaging.Home,
                onClick = { onItemSelected(Screen.Messaging.Home) },
                icon = {
                    BadgedBox(badge = {
                        if (unreadMessages > 0) Badge { Text(unreadMessages.toString()) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Message, null)
                    }
                },
                label = { Text(stringResource(R.string.messaging)) }
            )

            AnimatedNavItem(
                selected = currentScreen is Screen.Account.AccountHome,
                onClick = { onItemSelected(Screen.Account.AccountHome) },
                icon = { Icon(Icons.Default.Person, null) },
                label = { Text(stringResource(R.string.account)) }
            )
        }

        val searchScale by animateFloatAsState(
            targetValue = if (currentScreen is Screen.Main.Map) 1.1f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "searchScale"
        )

        Box(
            modifier = Modifier
                .size(66.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
                .graphicsLayer {
                    scaleX = searchScale
                    scaleY = searchScale
                }
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable { onItemSelected(Screen.Main.Map) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun RowScope.AnimatedNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navItemScale"
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }) {
                icon()
            }
        },
        label = label,
        colors = NavigationBarItemDefaults.colors(
            selectedTextColor = MaterialTheme.colorScheme.primary
        )
    )
}

@PreviewLightDark
@Composable
private fun AppNavigationBarPreview() {
    LokaveloTheme {
        AppNavigationBar(
            currentScreen = Screen.Owner.HomeBike,
            unreadMessages = 3,
            pendingLocations = 1,
            onItemSelected = {}
        )
    }
}