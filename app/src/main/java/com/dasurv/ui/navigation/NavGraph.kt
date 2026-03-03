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
    const val PIGMENT_CATALOGUE = "pigments"
    const val COLOR_RECOMMENDATION = "pigments/recommend"
    const val EQUIPMENT_LIST = "equipment"
    const val SESSION_DETAIL = "session/{sessionId}"
    const val SCHEDULE = "schedule"
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
    const val CLIENT_SESSIONS = "client/{clientId}/sessions"
    const val CLIENT_TRANSACTIONS = "client/{clientId}/transactions"

    // Builder functions for parameterized routes
    fun cameraWithClient(clientId: Long) = "camera/$clientId"
    fun clientDetail(clientId: Long) = "client/$clientId"
    fun sessionDetail(sessionId: Long) = "session/$sessionId"
    fun appointmentDetail(appointmentId: Long) = "appointment/$appointmentId"
    fun lipPhotoGallery(clientId: Long) = "client/$clientId/lip-photos"
    fun clientTryOn(clientId: Long) = "client/$clientId/try-on"
    fun captureResult(photoId: Long) = "capture-result/$photoId"
    fun demoResult(photoPath: String) = "demo-result?path=${java.net.URLEncoder.encode(photoPath, "UTF-8")}"
    fun editPigmentStock(equipmentId: Long) = "pigment-inventory/edit-stock/$equipmentId"
    fun editPigmentBottle(bottleId: Long) = "pigment-inventory/edit/$bottleId"
    fun pigmentSummary(photoId: Long) = "pigment-summary/$photoId"
    fun clientSessions(clientId: Long) = "client/$clientId/sessions"
    fun clientTransactions(clientId: Long) = "client/$clientId/transactions"
    fun addPigmentStockFromCatalog(name: String, brand: String, colorHex: String): String {
        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
        val encodedBrand = java.net.URLEncoder.encode(brand, "UTF-8")
        val encodedColor = java.net.URLEncoder.encode(colorHex, "UTF-8")
        return "pigment-inventory/add-stock?name=$encodedName&brand=$encodedBrand&colorHex=$encodedColor"
    }
}

private const val TRANSITION_DURATION = 300

@Composable
fun NavGraph(appointmentId: Long? = null) {
    val navController = rememberNavController()
    val timerViewModel: SessionTimerViewModel = hiltViewModel()
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(appointmentId) {
        if (appointmentId != null) {
            navController.navigate(Routes.appointmentDetail(appointmentId)) {
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
