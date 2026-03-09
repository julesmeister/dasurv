package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.home.HomeScreen
import com.dasurv.ui.screen.transaction.AllTransactionsScreen

internal fun NavGraphBuilder.homeRoutes(navController: NavController) {
    composable(Routes.HOME) {
        HomeScreen(
            onNavigateToCamera = { navController.navigate(Routes.CAMERA) },
            onNavigateToClients = { navController.navigate(Routes.CLIENT_LIST) },
            onNavigateToPigments = { navController.navigate(Routes.PIGMENT_CATALOGUE) },
            onNavigateToEquipment = { navController.navigate(Routes.EQUIPMENT_LIST) },
            onNavigateToSchedule = { navController.navigate(Routes.SCHEDULE) },
            onNavigateToAppointmentDetail = { id -> navController.navigate(Routes.appointmentDetail(id)) },
            onNavigateToPigmentInventory = { navController.navigate(Routes.PIGMENT_INVENTORY) },
            onNavigateToStaff = { navController.navigate(Routes.STAFF_LIST) },
            onNavigateToExport = { navController.navigate(Routes.EXPORT) },
            onNavigateToClient = { id -> navController.navigate(Routes.clientDetail(id)) },
            onNavigateToSession = { id -> navController.navigate(Routes.sessionDetail(id)) },
            onNavigateToTransactions = { navController.navigate(Routes.ALL_TRANSACTIONS) }
        )
    }

    composable(Routes.ALL_TRANSACTIONS) {
        AllTransactionsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
