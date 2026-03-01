package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.equipment.AddEditEquipmentScreen
import com.dasurv.ui.screen.equipment.EquipmentListScreen

internal fun NavGraphBuilder.equipmentRoutes(navController: NavController) {
    composable(Routes.EQUIPMENT_LIST) {
        EquipmentListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddEquipment = { navController.navigate(Routes.ADD_EQUIPMENT) },
            onNavigateToEditEquipment = { id -> navController.navigate(Routes.editEquipment(id)) }
        )
    }

    composable(Routes.ADD_EQUIPMENT) {
        AddEditEquipmentScreen(
            equipmentId = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.EDIT_EQUIPMENT,
        arguments = listOf(navArgument("equipmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val equipmentId = backStackEntry.arguments?.getLong("equipmentId")
        AddEditEquipmentScreen(
            equipmentId = equipmentId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
