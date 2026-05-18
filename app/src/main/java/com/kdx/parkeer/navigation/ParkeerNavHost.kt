package com.kdx.parkeer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kdx.parkeer.feature.gate.GateScreen
import com.kdx.parkeer.feature.scout.ScoutScreen
import com.kdx.parkeer.feature.station.StationScreen
import com.kdx.parkeer.feature.terminal.TerminalScreen

object Routes {
    const val ROLE_SELECTOR = "role_selector"
    const val STATION = "station"
    const val GATE = "gate"
    const val TERMINAL = "terminal"
    const val SCOUT = "scout"
}

@Composable
fun ParkeerNavHost() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.ROLE_SELECTOR) {
        composable(Routes.ROLE_SELECTOR) {
            RoleSelectorScreen(
                onStationClick = { navController.navigate(Routes.STATION) },
                onGateClick = { navController.navigate(Routes.GATE) },
                onTerminalClick = { navController.navigate(Routes.TERMINAL) },
                onScoutClick = { navController.navigate(Routes.SCOUT) },
            )
        }
        composable(Routes.STATION) {
            StationScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.GATE) {
            GateScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TERMINAL) {
            TerminalScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SCOUT) {
            ScoutScreen(onBack = { navController.popBackStack() })
        }
    }
}
