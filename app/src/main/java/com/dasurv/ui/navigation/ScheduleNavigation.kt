package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.schedule.AppointmentDetailScreen
import com.dasurv.ui.screen.schedule.ScheduleScreen

internal fun NavGraphBuilder.scheduleRoutes(navController: NavController) {
    composable(Routes.SCHEDULE) {
        ScheduleScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAppointmentDetail = { id -> navController.navigate(Routes.appointmentDetail(id)) }
        )
    }

    composable(
        Routes.APPOINTMENT_DETAIL,
        arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: return@composable
        AppointmentDetailScreen(
            appointmentId = appointmentId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSession = { id -> navController.navigate(Routes.sessionDetail(id)) }
        )
    }
}
