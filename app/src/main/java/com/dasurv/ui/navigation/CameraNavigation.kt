package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.camera.LipCameraScreen

internal fun NavGraphBuilder.cameraRoutes(navController: NavController) {
    composable(Routes.CAMERA) {
        LipCameraScreen(
            onNavigateBack = { navController.popBackStack() },
            preselectedClientId = null,
            onNavigateToCaptureResult = { photoId ->
                navController.navigate(Routes.captureResult(photoId))
            },
            onNavigateToDemoResult = { path ->
                navController.navigate(Routes.demoResult(path))
            }
        )
    }

    composable(
        Routes.CAMERA_WITH_CLIENT,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId")
        LipCameraScreen(
            onNavigateBack = { navController.popBackStack() },
            preselectedClientId = clientId,
            onNavigateToCaptureResult = { photoId ->
                navController.navigate(Routes.captureResult(photoId))
            },
            onNavigateToDemoResult = { path ->
                navController.navigate(Routes.demoResult(path))
            }
        )
    }
}
