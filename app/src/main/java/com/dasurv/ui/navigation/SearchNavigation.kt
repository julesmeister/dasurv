package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.search.SearchScreen

internal fun NavGraphBuilder.searchRoutes(navController: NavController) {
    composable(Routes.SEARCH) {
        SearchScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToClient = { id -> navController.navigate(Routes.clientDetail(id)) },
            onNavigateToAppointment = { id -> navController.navigate(Routes.appointmentDetail(id)) },
            onNavigateToSession = { id -> navController.navigate(Routes.sessionDetail(id)) }
        )
    }
}
