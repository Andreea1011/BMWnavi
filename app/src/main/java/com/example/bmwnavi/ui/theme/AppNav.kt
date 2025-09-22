package com.example.bmwnavi.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.bmwnavi.ui.screen.HomeScreen
import com.example.bmwnavi.ui.screen.GeneralStatsScreen

sealed interface Route {
    data object Home : Route
    data object General : Route
}
private fun Route.name() = when(this){
    Route.Home -> "home"
    Route.General -> "general"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Home.name()) {
        composable(Route.Home.name()) {
            HomeScreen(
                onGeneralStats = { nav.navigate(Route.General.name()) }
            )
        }
        composable(Route.General.name()) {
            GeneralStatsScreen(onBack = { nav.popBackStack() })
        }
    }
}
