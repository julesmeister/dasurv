package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.pigment.ColorRecommendationScreen
import com.dasurv.ui.screen.pigment.PigmentCatalogueScreen
import com.dasurv.ui.screen.pigmentinventory.AddEditPigmentBottleScreen
import com.dasurv.ui.screen.pigmentinventory.AddEditPigmentStockScreen
import com.dasurv.ui.screen.pigmentinventory.PigmentInventoryScreen

internal fun NavGraphBuilder.pigmentRoutes(navController: NavController) {
    composable(Routes.PIGMENT_CATALOGUE) {
        PigmentCatalogueScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRecommendation = { navController.navigate(Routes.COLOR_RECOMMENDATION) },
            onNavigateToPigmentInventory = { navController.navigate(Routes.PIGMENT_INVENTORY) },
            onNavigateToAddStock = { name, brand, colorHex ->
                val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                val encodedBrand = java.net.URLEncoder.encode(brand, "UTF-8")
                val encodedColor = java.net.URLEncoder.encode(colorHex, "UTF-8")
                navController.navigate("pigment-inventory/add-stock?name=$encodedName&brand=$encodedBrand&colorHex=$encodedColor")
            }
        )
    }

    composable(Routes.COLOR_RECOMMENDATION) {
        ColorRecommendationScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Routes.PIGMENT_INVENTORY) {
        PigmentInventoryScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddStock = { navController.navigate(Routes.ADD_PIGMENT_STOCK) },
            onNavigateToEditStock = { id -> navController.navigate("pigment-inventory/edit-stock/$id") },
            onNavigateToEditBottle = { id -> navController.navigate("pigment-inventory/edit/$id") },
            onNavigateToCatalogue = { navController.navigate(Routes.PIGMENT_CATALOGUE) }
        )
    }

    composable(Routes.ADD_PIGMENT_STOCK) {
        AddEditPigmentStockScreen(
            equipmentId = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.ADD_PIGMENT_STOCK_FROM_CATALOG,
        arguments = listOf(
            navArgument("name") { type = NavType.StringType; defaultValue = "" },
            navArgument("brand") { type = NavType.StringType; defaultValue = "" },
            navArgument("colorHex") { type = NavType.StringType; defaultValue = "" }
        )
    ) { backStackEntry ->
        val name = backStackEntry.arguments?.getString("name")?.let {
            java.net.URLDecoder.decode(it, "UTF-8")
        } ?: ""
        val brand = backStackEntry.arguments?.getString("brand")?.let {
            java.net.URLDecoder.decode(it, "UTF-8")
        } ?: ""
        val colorHex = backStackEntry.arguments?.getString("colorHex")?.let {
            java.net.URLDecoder.decode(it, "UTF-8")
        } ?: ""
        AddEditPigmentStockScreen(
            equipmentId = null,
            onNavigateBack = { navController.popBackStack() },
            prefillName = name.ifBlank { null },
            prefillBrand = brand.ifBlank { null },
            prefillColorHex = colorHex.ifBlank { null }
        )
    }

    composable(
        Routes.EDIT_PIGMENT_STOCK,
        arguments = listOf(navArgument("equipmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val equipmentId = backStackEntry.arguments?.getLong("equipmentId")
        AddEditPigmentStockScreen(
            equipmentId = equipmentId,
            onNavigateBack = { navController.popBackStack() }
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
