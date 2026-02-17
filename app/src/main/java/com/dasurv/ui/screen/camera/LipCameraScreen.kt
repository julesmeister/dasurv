package com.dasurv.ui.screen.camera

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.util.loadBitmapFromUri

@Composable
fun LipCameraScreen(
    onNavigateBack: () -> Unit,
    preselectedClientId: Long? = null,
    onNavigateToCaptureResult: (Long) -> Unit = {},
    onNavigateToDemoResult: (String) -> Unit = {},
    viewModel: LipCameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val dualAnalysis by viewModel.dualAnalysis.collectAsStateWithLifecycle()
    val lipAnalysis by viewModel.lipAnalysis.collectAsStateWithLifecycle()
    val detectedFace by viewModel.detectedFace.collectAsStateWithLifecycle()
    val arMode by viewModel.arMode.collectAsStateWithLifecycle()
    val captureSuccess by viewModel.captureSuccess.collectAsStateWithLifecycle()
    val galleryMode by viewModel.galleryMode.collectAsStateWithLifecycle()
    val galleryBitmap by viewModel.galleryBitmap.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageWidth by remember { mutableIntStateOf(480) }
    var imageHeight by remember { mutableIntStateOf(640) }
    var showClientPicker by remember { mutableStateOf(false) }
    var panelExpanded by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }
    var arVerticalOffset by remember { mutableFloatStateOf(0f) }
    var showCalibration by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.analyzeGalleryPhoto(bitmap)
            }
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) hasCameraPermission = true
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(preselectedClientId) {
        if (preselectedClientId != null && preselectedClientId > 0) {
            viewModel.preselectClient(preselectedClientId)
        }
    }

    // Shutter button success animation
    var showSuccessPulse by remember { mutableStateOf(false) }
    LaunchedEffect(captureSuccess) {
        if (captureSuccess) {
            showSuccessPulse = true
            kotlinx.coroutines.delay(1200)
            showSuccessPulse = false
            kotlinx.coroutines.delay(300)
            viewModel.dismissCaptureSuccess()
        }
    }

    // Navigate to result screen via event
    LaunchedEffect(navigationEvent) {
        when (val event = navigationEvent) {
            is LipCameraViewModel.NavigationEvent.ToCaptureResult -> {
                viewModel.clearNavigationEvent()
                onNavigateToCaptureResult(event.photoId)
            }
            is LipCameraViewModel.NavigationEvent.ToDemoResult -> {
                viewModel.clearNavigationEvent()
                onNavigateToDemoResult(event.path)
            }
            null -> { /* no-op */ }
        }
    }

    if (showClientPicker) {
        ClientPickerDialog(
            viewModel = viewModel,
            onDismiss = { showClientPicker = false },
            onClientSelected = { client ->
                viewModel.selectClient(client)
                showClientPicker = false
            }
        )
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required for lip detection")
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview or gallery image — fills entire screen
        if (galleryMode && galleryBitmap != null) {
            Image(
                bitmap = galleryBitmap!!.asImageBitmap(),
                contentDescription = "Gallery photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Update dimensions for overlay scaling
            LaunchedEffect(galleryBitmap) {
                galleryBitmap?.let {
                    imageWidth = it.width
                    imageHeight = it.height
                }
            }
        } else {
            CameraPreview(
                onImageDimensionsChanged = { w, h -> imageWidth = w; imageHeight = h },
                onFaceDetected = { face, bitmap -> viewModel.onFaceDetected(face, bitmap) },
                onRotationDetected = { rot, front -> viewModel.setImageRotation(rot, front) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Lip overlay with AR support
        LipOverlay(
            face = detectedFace,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            isFrontCamera = !galleryMode,
            arMode = arMode,
            upperLipPigmentHex = if (arMode) viewModel.getSelectedUpperPigmentHex() else null,
            lowerLipPigmentHex = if (arMode) viewModel.getSelectedLowerPigmentHex() else null,
            naturalUpperLipHex = dualAnalysis?.upperLip?.dominantColorHex,
            naturalLowerLipHex = dualAnalysis?.lowerLip?.dominantColorHex,
            verticalOffsetPx = arVerticalOffset
        )

        // Swipe gesture layer to toggle controls visibility
        // (must be before top bar so buttons render on top and receive touches)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (kotlin.math.abs(dragAmount) > 20f) {
                            controlsVisible = !controlsVisible
                        }
                    }
                }
        )

        // --- Transparent top bar ---
        CameraTopBar(
            onNavigateBack = onNavigateBack,
            galleryMode = galleryMode,
            onExitGallery = { viewModel.exitGalleryMode() },
            onOpenGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        // --- Floating analysis pills ---
        AnalysisPillsOverlay(
            dualAnalysis = dualAnalysis,
            lipAnalysis = lipAnalysis,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 52.dp)
        )

        // --- AR Calibration slider (floating, always accessible when visible) ---
        ArCalibrationSlider(
            visible = showCalibration,
            value = arVerticalOffset,
            onValueChange = { arVerticalOffset = it },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )

        // --- Bottom controls area ---
        CameraBottomControls(
            viewModel = viewModel,
            visible = controlsVisible,
            panelExpanded = panelExpanded,
            onToggleExpand = { panelExpanded = !panelExpanded },
            showCalibration = showCalibration,
            onToggleCalibration = { showCalibration = !showCalibration },
            showSuccessPulse = showSuccessPulse,
            onShowClientPicker = { showClientPicker = true },
            onHideControls = { controlsVisible = false },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Tap to bring controls back when hidden
        if (!controlsVisible) {
            ControlsHintOverlay(
                onShowControls = { controlsVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            )
        }

        // Capture success overlay
        CaptureSuccessOverlay(
            visible = captureSuccess,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
