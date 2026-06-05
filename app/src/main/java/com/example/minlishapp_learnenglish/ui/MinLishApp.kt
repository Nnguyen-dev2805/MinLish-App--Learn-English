package com.example.minlishapp_learnenglish.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.minlishapp_learnenglish.core.AppContainer
import com.example.minlishapp_learnenglish.navigation.AppNavGraph
import com.example.minlishapp_learnenglish.navigation.Routes
import com.example.minlishapp_learnenglish.navigation.mainDestinations
import com.example.minlishapp_learnenglish.navigation.requestHomeRefresh
import com.example.minlishapp_learnenglish.ui.components.MinLishBottomBar

@Composable
fun MinLishApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomBarRoute = if (currentRoute == Routes.LearnDeck) Routes.Learn else currentRoute
    val showBottomBar = mainDestinations.any { it.route == bottomBarRoute }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    MinLishBottomBar(
                        destinations = mainDestinations,
                        currentRoute = bottomBarRoute,
                        onDestinationClick = { destination ->
                            if (destination.route == Routes.Home && bottomBarRoute == Routes.Learn) {
                                navController.requestHomeRefresh()
                            }
                            navController.navigate(
                                route = destination.route,
                                navOptions = navOptions {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                appContainer = appContainer,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
