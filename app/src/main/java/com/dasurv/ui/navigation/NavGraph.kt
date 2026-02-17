package com.dasurv.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dasurv.ui.component.FloatingSessionTimer
import com.dasurv.ui.screen.session.SessionTimerViewModel

object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val CAMERA_WITH_CLIENT = "camera/{clientId}"
    const val CLIENT_LIST = "clients"
    const val CLIENT_DETAIL = "client/{clientId}"
    const val ADD_CLIENT = "client/add"
    const val EDIT_CLIENT = "client/edit/{clientId}"
    const val PIGMENT_CATALOGUE = "pigments"
    const val COLOR_RECOMMENDATION = "pigments/recommend"
    const val EQUIPMENT_LIST = "equipment"
    const val ADD_EQUIPMENT = "equipment/add"
    const val EDIT_EQUIPMENT = "equipment/edit/{equipmentId}"
    const val NEW_SESSION = "session/new/{clientId}"
    const val SESSION_DETAIL = "session/{sessionId}"
    const val SCHEDULE = "schedule"
    const val ADD_APPOINTMENT = "appointment/add"
    const val ADD_APPOINTMENT_FOR_CLIENT = "appointment/add/client/{clientId}"
    const val ADD_APPOINTMENT_FOR_DAY = "appointment/add/day/{dateTime}"
    const val EDIT_APPOINTMENT = "appointment/edit/{appointmentId}"
    const val APPOINTMENT_DETAIL = "appointment/{appointmentId}"
    const val LIP_PHOTO_GALLERY = "client/{clientId}/lip-photos"
    const val CLIENT_TRY_ON = "client/{clientId}/try-on"
    const val CAPTURE_RESULT = "capture-result/{photoId}"
    const val DEMO_RESULT = "demo-result?path={photoPath}"
    const val PIGMENT_INVENTORY = "pigment-inventory"
    const val ADD_PIGMENT_STOCK = "pigment-inventory/add-stock"
    const val ADD_PIGMENT_STOCK_FROM_CATALOG = "pigment-inventory/add-stock?name={name}&brand={brand}&colorHex={colorHex}"
    const val EDIT_PIGMENT_STOCK = "pigment-inventory/edit-stock/{equipmentId}"
    const val EDIT_PIGMENT_BOTTLE = "pigment-inventory/edit/{bottleId}"
    const val PIGMENT_SUMMARY = "pigment-summary/{photoId}"
    const val CLIENT_TRANSACTIONS = "client/{clientId}/transactions"
    const val ADD_TRANSACTION = "client/{clientId}/transactions/add"
}

private const val TRANSITION_DURATION = 300

@Composable
fun NavGraph(appointmentId: Long? = null) {
    val navController = rememberNavController()
    val timerViewModel: SessionTimerViewModel = hiltViewModel()
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(appointmentId) {
        if (appointmentId != null) {
            navController.navigate("appointment/$appointmentId") {
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            fadeIn(tween(TRANSITION_DURATION)) + slideInHorizontally(
                spring(stiffness = Spring.StiffnessMediumLow)
            ) { it / 5 }
        },
        exitTransition = {
            fadeOut(tween(TRANSITION_DURATION)) + slideOutHorizontally(
                spring(stiffness = Spring.StiffnessMediumLow)
            ) { -it / 5 }
        },
        popEnterTransition = {
            fadeIn(tween(TRANSITION_DURATION)) + slideInHorizontally(
                spring(stiffness = Spring.StiffnessMediumLow)
            ) { -it / 5 }
        },
        popExitTransition = {
            fadeOut(tween(TRANSITION_DURATION)) + slideOutHorizontally(
                spring(stiffness = Spring.StiffnessMediumLow)
            ) { it / 5 }
        }
    ) {
        homeRoutes(navController)
        cameraRoutes(navController)
        clientRoutes(navController)
        pigmentRoutes(navController)
        equipmentRoutes(navController)
        sessionRoutes(navController, timerViewModel)
        scheduleRoutes(navController)
        mediaRoutes(navController)
    }

    // Floating timer overlay
    FloatingSessionTimer(
        state = timerState,
        onToggleExpanded = timerViewModel::toggleExpanded,
        onToggleUpper = timerViewModel::toggleUpperZone,
        onToggleLower = timerViewModel::toggleLowerZone,
        onPauseResume = timerViewModel::pauseResumeTimer,
        onRequestStop = timerViewModel::requestStop,
        onCancelStop = timerViewModel::cancelStop,
        onConfirmDiscard = timerViewModel::confirmDiscard,
        modifier = Modifier.align(Alignment.TopEnd)
    )
    } // end Box
}
