package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.client.AddUpdateScreen
import com.dasurv.ui.screen.client.ClientDetailScreen
import com.dasurv.ui.screen.client.ClientListScreen
import com.dasurv.ui.screen.session.SessionListScreen
import com.dasurv.ui.screen.transaction.TransactionListScreen

internal fun NavGraphBuilder.clientRoutes(navController: NavController) {
    composable(Routes.CLIENT_LIST) {
        ClientListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToClient = { id -> navController.navigate(Routes.clientDetail(id)) }
        )
    }

    composable(
        Routes.CLIENT_DETAIL,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        ClientDetailScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSession = { id -> navController.navigate(Routes.sessionDetail(id)) },
            onNavigateToAppointmentDetail = { id -> navController.navigate(Routes.appointmentDetail(id)) },
            onNavigateToLipCamera = { id -> navController.navigate(Routes.cameraWithClient(id)) },
            onNavigateToLipPhotoGallery = { id -> navController.navigate(Routes.lipPhotoGallery(id)) },
            onNavigateToTryOn = { id -> navController.navigate(Routes.clientTryOn(id)) },
            onNavigateToSessions = { id -> navController.navigate(Routes.clientSessions(id)) },
            onNavigateToTransactions = { id -> navController.navigate(Routes.clientTransactions(id)) },
            onNavigateToAddUpdate = { id -> navController.navigate(Routes.addClientUpdate(id)) },
            onNavigateToEditUpdate = { cId, uId -> navController.navigate(Routes.editClientUpdate(cId, uId)) },
        )
    }

    composable(
        Routes.CLIENT_SESSIONS,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        SessionListScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSession = { id -> navController.navigate(Routes.sessionDetail(id)) }
        )
    }

    composable(
        Routes.CLIENT_TRANSACTIONS,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        TransactionListScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.ADD_CLIENT_UPDATE,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        AddUpdateScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() },
        )
    }

    composable(
        Routes.EDIT_CLIENT_UPDATE,
        arguments = listOf(
            navArgument("clientId") { type = NavType.LongType },
            navArgument("updateId") { type = NavType.LongType },
        )
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        val updateId = backStackEntry.arguments?.getLong("updateId") ?: return@composable
        AddUpdateScreen(
            clientId = clientId,
            updateId = updateId,
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
