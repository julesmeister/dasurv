package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dasurv.ui.screen.session.SessionTemplateManagementScreen

internal fun NavGraphBuilder.sessionTemplateRoutes(navController: NavController) {
    composable(Routes.SESSION_TEMPLATES) {
        SessionTemplateManagementScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
