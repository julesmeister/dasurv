package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.client.AddEditClientScreen
import com.dasurv.ui.screen.client.ClientDetailScreen
import com.dasurv.ui.screen.client.ClientListScreen
import com.dasurv.ui.screen.transaction.AddTransactionScreen
import com.dasurv.ui.screen.transaction.TransactionListScreen

internal fun NavGraphBuilder.clientRoutes(navController: NavController) {
    composable(Routes.CLIENT_LIST) {
        ClientListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToClient = { id -> navController.navigate("client/$id") },
            onNavigateToAddClient = { navController.navigate(Routes.ADD_CLIENT) }
        )
    }

    composable(Routes.ADD_CLIENT) {
        AddEditClientScreen(
            clientId = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.EDIT_CLIENT,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId")
        AddEditClientScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() }
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
            onNavigateToEditClient = { id -> navController.navigate("client/edit/$id") },
            onNavigateToSession = { id -> navController.navigate("session/$id") },
            onNavigateToNewSession = { id -> navController.navigate("session/new/$id") },
            onNavigateToBookAppointment = { id -> navController.navigate("appointment/add/client/$id") },
            onNavigateToAppointmentDetail = { id -> navController.navigate("appointment/$id") },
            onNavigateToLipCamera = { id -> navController.navigate("camera/$id") },
            onNavigateToLipPhotoGallery = { id -> navController.navigate("client/$id/lip-photos") },
            onNavigateToTryOn = { id -> navController.navigate("client/$id/try-on") },
            onNavigateToTransactions = { id -> navController.navigate("client/$id/transactions") }
        )
    }

    composable(
        Routes.CLIENT_TRANSACTIONS,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        TransactionListScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddTransaction = { id -> navController.navigate("client/$id/transactions/add") }
        )
    }

    composable(
        Routes.ADD_TRANSACTION,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        AddTransactionScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
