package com.dasurv.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dasurv.ui.screen.camera.CaptureResultScreen
import com.dasurv.ui.screen.camera.LipPhotoGalleryScreen
import com.dasurv.ui.screen.camera.PigmentSummaryScreen
import com.dasurv.ui.screen.tryon.ClientTryOnScreen

internal fun NavGraphBuilder.mediaRoutes(navController: NavController) {
    composable(
        Routes.LIP_PHOTO_GALLERY,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        LipPhotoGalleryScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSummary = { photoId ->
                navController.navigate("pigment-summary/$photoId")
            },
            onNavigateToCaptureResult = { photoId ->
                navController.navigate("capture-result/$photoId")
            }
        )
    }

    composable(
        Routes.CLIENT_TRY_ON,
        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
    ) { backStackEntry ->
        val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
        ClientTryOnScreen(
            clientId = clientId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        Routes.CAPTURE_RESULT,
        arguments = listOf(navArgument("photoId") { type = NavType.LongType })
    ) { backStackEntry ->
        val photoId = backStackEntry.arguments?.getLong("photoId") ?: return@composable
        CaptureResultScreen(
            photoId = photoId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSummary = { id ->
                navController.navigate("pigment-summary/$id")
            }
        )
    }

    composable(
        Routes.DEMO_RESULT,
        arguments = listOf(navArgument("photoPath") { type = NavType.StringType })
    ) { backStackEntry ->
        val photoPath = backStackEntry.arguments?.getString("photoPath")
        val decodedPath = if (photoPath != null) java.net.URLDecoder.decode(photoPath, "UTF-8") else null
        CaptureResultScreen(
            demoPhotoPath = decodedPath,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSummary = { id ->
                navController.navigate("pigment-summary/$id")
            }
        )
    }

    composable(
        Routes.PIGMENT_SUMMARY,
        arguments = listOf(navArgument("photoId") { type = NavType.LongType })
    ) { backStackEntry ->
        val photoId = backStackEntry.arguments?.getLong("photoId") ?: return@composable
        PigmentSummaryScreen(
            photoId = photoId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
