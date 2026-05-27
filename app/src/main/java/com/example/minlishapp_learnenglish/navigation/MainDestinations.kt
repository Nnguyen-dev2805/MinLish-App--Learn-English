package com.example.minlishapp_learnenglish.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Style
import androidx.compose.ui.graphics.vector.ImageVector

data class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val mainDestinations = listOf(
    MainDestination(Routes.Home, "Home", Icons.Outlined.Home),
    MainDestination(Routes.Decks, "Decks", Icons.Outlined.Style),
    MainDestination(Routes.Learn, "Learn", Icons.Outlined.School),
    MainDestination(Routes.Progress, "Progress", Icons.Outlined.Leaderboard),
    MainDestination(Routes.Profile, "Profile", Icons.Outlined.Person)
)
