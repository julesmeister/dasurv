package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.equipment.EquipmentListScreen

internal fun NavGraphBuilder.equipmentRoutes(navController: NavController) {
    composable(Routes.EQUIPMENT_LIST) {
        EquipmentListScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
