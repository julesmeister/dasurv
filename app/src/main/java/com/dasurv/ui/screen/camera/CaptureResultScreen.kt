package com.dasurv.ui.screen.camera

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.LipZone
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary
import com.dasurv.ui.util.parseHexSafe
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureResultScreen(
    photoId: Long? = null,
    demoPhotoPath: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (Long) -> Unit = {},
    viewModel: CaptureResultViewModel = hiltViewModel()
) {
    val bitmap by viewModel.bitmap.collectAsStateWithLifecycle()
    val photo by viewModel.photo.collectAsStateWithLifecycle()
    val dualAnalysis by viewModel.dualAnalysis.collectAsStateWithLifecycle()
    val upperRecs by viewModel.upperRecommendations.collectAsStateWithLifecycle()
    val lowerRecs by viewModel.lowerRecommendations.collectAsStateWithLifecycle()
    val selectedUpperPigment by viewModel.selectedUpperPigment.collectAsStateWithLifecycle()
    val selectedLowerPigment by viewModel.selectedLowerPigment.collectAsStateWithLifecycle()
    val activeZone by viewModel.activeZone.collectAsStateWithLifecycle()
    val upperIntensity by viewModel.upperIntensity.collectAsStateWithLifecycle()
    val lowerIntensity by viewModel.lowerIntensity.collectAsStateWithLifecycle()
    val blendedUpperHex by viewModel.blendedUpperHex.collectAsStateWithLifecycle()
    val blendedLowerHex by viewModel.blendedLowerHex.collectAsStateWithLifecycle()
    val detectedFace by viewModel.detectedFace.collectAsStateWithLifecycle()
    val filteredPigments by viewModel.filteredPigments.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val upperLipScale by viewModel.upperLipScale.collectAsStateWithLifecycle()
    val lowerLipScale by viewModel.lowerLipScale.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val arOverlayVisible by viewModel.arOverlayVisible.collectAsStateWithLifecycle()
    val isCropMode by viewModel.isCropMode.collectAsStateWithLifecycle()
    val isColorPickerMode by viewModel.isColorPickerMode.collectAsStateWithLifecycle()

    // Derived state
    val activeZonePigment = if (activeZone == LipZone.UPPER) selectedUpperPigment else selectedLowerPigment
    val activeZoneIntensity = if (activeZone == LipZone.UPPER) upperIntensity else lowerIntensity
    val anyPigmentSelected = selectedUpperPigment != null || selectedLowerPigment != null

    // Image vertical panning
    var imageOffsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current

    LaunchedEffect(photoId, demoPhotoPath) {
        if (photoId != null && photoId > 0) {
            viewModel.loadPhoto(photoId)
        } else if (demoPhotoPath != null) {
            viewModel.loadDemoPhoto(demoPhotoPath)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val collapsedHeightPx = screenHeightPx * 0.35f
        val expandedHeightPx = screenHeightPx * 0.75f

        val panelHeightPx = remember { Animatable(collapsedHeightPx) }

        // Full-screen photo background
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Captured photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = imageOffsetY }
                    .pointerInput(isCropMode) {
                        if (!isCropMode) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val maxOffset = screenHeightPx * 0.3f
                                imageOffsetY = (imageOffsetY + dragAmount).coerceIn(-maxOffset, maxOffset)
                            }
                        }
                    }
            )

            // Lip overlay
            if (arOverlayVisible && !isCropMode) {
                LipOverlay(
                    face = detectedFace,
                    imageWidth = bitmap!!.width,
                    imageHeight = bitmap!!.height,
                    isFrontCamera = false,
                    arMode = anyPigmentSelected,
                    upperLipPigmentHex = if (selectedUpperPigment != null) (blendedUpperHex ?: selectedUpperPigment?.colorHex) else null,
                    lowerLipPigmentHex = if (selectedLowerPigment != null) (blendedLowerHex ?: selectedLowerPigment?.colorHex) else null,
                    fullCoverage = true,
                    verticalOffsetPx = imageOffsetY,
                    upperHorizontalScale = upperLipScale,
                    lowerHorizontalScale = lowerLipScale
                )
            }

            // Crop overlay — key resets crop bounds after rotation
            if (isCropMode) {
                key(bitmap!!.width, bitmap!!.height) {
                    CropOverlay(
                        bitmapWidth = bitmap!!.width,
                        bitmapHeight = bitmap!!.height,
                        screenHeightPx = screenHeightPx,
                        screenWidthPx = with(density) { maxWidth.toPx() },
                        onApply = { cropRect -> viewModel.applyCrop(cropRect) },
                        onCancel = { viewModel.exitCropMode() },
                        onRotateLeft = { viewModel.rotateBitmap90(clockwise = false) },
                        onRotateRight = { viewModel.rotateBitmap90(clockwise = true) }
                    )
                }
            }

            // Color picker overlay
            if (isColorPickerMode) {
                ColorPickerOverlay(
                    bitmap = bitmap!!,
                    imageOffsetY = imageOffsetY,
                    onColorPicked = { viewModel.applyPickedColor(it) },
                    onDismiss = { viewModel.exitColorPickerMode() }
                )
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RoseTertiary)
            }
        }

        // Top bar overlay
        if (!isCropMode && !isColorPickerMode) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Analysis Result", color = Color.White,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                // Save & navigate to summary
                if (anyPigmentSelected) {
                    IconButton(
                        onClick = {
                            if (photoId != null && photoId > 0) {
                                // Client photo: save pigments to DB, then navigate
                                viewModel.saveSelectedPigments()
                                onNavigateToSummary(photoId)
                            } else {
                                // Demo photo: populate transient data, navigate with photoId=0
                                PigmentSummaryData.populate(
                                    photoUri = demoPhotoPath,
                                    dualAnalysis = dualAnalysis,
                                    upperPigment = selectedUpperPigment,
                                    lowerPigment = selectedLowerPigment,
                                    upperIntensity = upperIntensity,
                                    lowerIntensity = lowerIntensity,
                                    captureType = photo?.captureType
                                        ?: com.dasurv.data.local.entity.CaptureType.BEFORE,
                                    followUpInterval = photo?.followUpInterval
                                )
                                onNavigateToSummary(0L)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            "View summary",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = { viewModel.enterCropMode() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Crop, "Crop", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { viewModel.enterColorPickerMode() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Colorize, "Pick color", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                if (detectedFace != null) {
                    IconButton(onClick = { viewModel.toggleArOverlay() }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (arOverlayVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            if (arOverlayVisible) "Hide overlay" else "Show overlay",
                            tint = Color.White, modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (anyPigmentSelected) {
                    TextButton(onClick = { viewModel.clearAllPigments() }) { Text("Clear", color = Color.White) }
                }
            }
        }
        // Face detection status badge
        if (!isLoading && detectedFace == null && bitmap != null && !isCropMode && !isColorPickerMode) {
            Surface(
                shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 56.dp)
            ) {
                Text("No face detected \u2014 lip overlay unavailable",
                    color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }

        // Bottom panel
        if (!isCropMode && !isColorPickerMode) {
            val panelHeightDp = with(density) { panelHeightPx.value.toDp() }
            val panelCoroutineScope = rememberCoroutineScope()
            val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            val midPoint = (collapsedHeightPx + expandedHeightPx) / 2f
            val snapSpring = spring<Float>(dampingRatio = 0.8f, stiffness = 300f)

            fun snapTarget(): Float =
                if (panelHeightPx.value > midPoint) expandedHeightPx else collapsedHeightPx

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(panelHeightDp)
                    .background(
                        Brush.verticalGradient(listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface
                        )),
                        panelShape
                    )
                    .clip(panelShape)
                    .pointerInput(Unit) {
                        val velocityTracker = VelocityTracker()
                        detectVerticalDragGestures(
                            onDragStart = { velocityTracker.resetTracking() },
                            onVerticalDrag = { change, dragAmount ->
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                val newHeight = (panelHeightPx.value - dragAmount)
                                    .coerceIn(collapsedHeightPx, expandedHeightPx)
                                panelCoroutineScope.launch { panelHeightPx.snapTo(newHeight) }
                            },
                            onDragEnd = {
                                val vy = velocityTracker.calculateVelocity().y
                                val target = when {
                                    vy < -500f -> expandedHeightPx
                                    vy > 500f -> collapsedHeightPx
                                    else -> snapTarget()
                                }
                                panelCoroutineScope.launch { panelHeightPx.animateTo(target, snapSpring) }
                            }
                        )
                    }
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            panelCoroutineScope.launch { panelHeightPx.animateTo(snapTarget(), snapSpring) }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                }
                CaptureResultPanelContent(
                    dualAnalysis = dualAnalysis,
                    activeZone = activeZone,
                    selectedUpperPigment = selectedUpperPigment,
                    selectedLowerPigment = selectedLowerPigment,
                    activeZonePigment = activeZonePigment,
                    activeZoneIntensity = activeZoneIntensity,
                    anyPigmentSelected = anyPigmentSelected,
                    detectedFace = detectedFace,
                    upperLipScale = upperLipScale,
                    lowerLipScale = lowerLipScale,
                    blendedUpperHex = blendedUpperHex,
                    blendedLowerHex = blendedLowerHex,
                    upperRecs = upperRecs,
                    lowerRecs = lowerRecs,
                    filteredPigments = filteredPigments,
                    selectedBrand = selectedBrand,
                    onSetActiveZone = { viewModel.setActiveZone(it) },
                    onSetLipScale = { zone, scale -> viewModel.setLipScale(zone, scale) },
                    onSetIntensity = { viewModel.setIntensity(it) },
                    onSelectPigment = { viewModel.selectPigment(it) },
                    onSelectBrand = { viewModel.selectBrand(it) }
                )
            }
        }
    }
}
