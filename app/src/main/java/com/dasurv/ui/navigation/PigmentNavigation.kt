package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.pigment.ColorRecommendationScreen
import com.dasurv.ui.screen.pigment.PigmentCatalogueScreen
import com.dasurv.ui.screen.pigmentinventory.AddEditPigmentBottleScreen
import com.dasurv.ui.screen.pigmentinventory.PigmentInventoryScreen

internal fun NavGraphBuilder.pigmentRoutes(navController: NavController) {
    composable(Routes.PIGMENT_CATALOGUE) {
        PigmentCatalogueScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRecommendation = { navController.navigate(Routes.COLOR_RECOMMENDATION) },
            onNavigateToPigmentInventory = { navController.navigate(Routes.PIGMENT_INVENTORY) },
        )
    }

    composable(Routes.COLOR_RECOMMENDATION) {
        ColorRecommendationScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Routes.PIGMENT_INVENTORY) {
        PigmentInventoryScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEditBottle = { id -> navController.navigate(Routes.editPigmentBottle(id)) },
            onNavigateToCatalogue = { navController.navigate(Routes.PIGMENT_CATALOGUE) }
        )
    }

    composable(
        Routes.EDIT_PIGMENT_BOTTLE,
        arguments = listOf(navArgument("bottleId") { type = NavType.LongType })
    ) { backStackEntry ->
        val bottleId = backStackEntry.arguments?.getLong("bottleId")
        AddEditPigmentBottleScreen(
            bottleId = bottleId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
