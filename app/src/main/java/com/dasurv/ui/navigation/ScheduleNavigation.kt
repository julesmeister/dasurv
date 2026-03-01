package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.schedule.AddEditAppointmentScreen
import com.dasurv.ui.screen.schedule.AppointmentDetailScreen
import com.dasurv.ui.screen.schedule.ScheduleScreen

internal fun NavGraphBuilder.scheduleRoutes(navController: NavController) {
    composable(Routes.SCHEDULE) {
        ScheduleScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddAppointment = { navController.navigate(Routes.ADD_APPOINTMENT) },
            onNavigateToAddAppointmentForDay = { dateTime ->
                navController.navigate(Routes.addAppointmentForDay(dateTime))
            },
            onNavigateToAppointmentDetail = { id -> navController.navigate(Routes.appointmentDetail(id)) }
        )
    }

    composable(Routes.ADD_APPOINTMENT) {
        AddEditAppointmentScreen(
            appointmentId = null,
            preselectedClientId = null,
            preselectedDateTime = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.ADD_APPOINTMENT_FOR_CLIENT,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        AddEditAppointmentScreen(
            appointmentId = null,
            preselectedClientId = clientId,
            preselectedDateTime = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.ADD_APPOINTMENT_FOR_DAY,
        arguments = listOf(navArgument("dateTime") { type = NavType.LongType })
    ) { backStackEntry ->
        val dateTime = backStackEntry.arguments?.getLong("dateTime") ?: return@composable
        AddEditAppointmentScreen(
            appointmentId = null,
            preselectedClientId = null,
            preselectedDateTime = dateTime,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.EDIT_APPOINTMENT,
        arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: return@composable
        AddEditAppointmentScreen(
            appointmentId = appointmentId,
            preselectedClientId = null,
            preselectedDateTime = null,
            onNavigateBack = { navController.popBackStack() }
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
            onNavigateToEdit = { id -> navController.navigate(Routes.editAppointment(id)) },
            onNavigateToSession = { id -> navController.navigate(Routes.sessionDetail(id)) }
        )
    }
}
