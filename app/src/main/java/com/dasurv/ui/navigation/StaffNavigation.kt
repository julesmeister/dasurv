package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.staff.StaffListScreen

internal fun NavGraphBuilder.staffRoutes(navController: NavController) {
    composable(Routes.STAFF_LIST) {
        StaffListScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
