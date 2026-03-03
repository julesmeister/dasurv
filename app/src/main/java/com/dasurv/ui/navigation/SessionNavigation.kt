package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.session.SessionDetailScreen
import com.dasurv.ui.screen.session.SessionTimerViewModel

internal fun NavGraphBuilder.sessionRoutes(
    navController: NavController,
    timerViewModel: SessionTimerViewModel
) {
    composable(
        Routes.SESSION_DETAIL,
        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
        SessionDetailScreen(
            sessionId = sessionId,
            onNavigateBack = { navController.popBackStack() },
            timerViewModel = timerViewModel
        )
    }
}
