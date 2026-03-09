package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.equipment.EquipmentListScreen
import com.dasurv.ui.screen.equipment.EquipmentPurchaseHistoryScreen

internal fun NavGraphBuilder.equipmentRoutes(navController: NavController) {
    composable(Routes.EQUIPMENT_LIST) {
        EquipmentListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPurchaseHistory = { navController.navigate(Routes.EQUIPMENT_PURCHASE_HISTORY) }
        )
    }

    composable(Routes.EQUIPMENT_PURCHASE_HISTORY) {
        EquipmentPurchaseHistoryScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
