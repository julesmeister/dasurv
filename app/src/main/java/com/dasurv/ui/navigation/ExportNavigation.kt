package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.export.ExportScreen

internal fun NavGraphBuilder.exportRoutes(navController: NavController) {
    composable(Routes.EXPORT) {
        ExportScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
