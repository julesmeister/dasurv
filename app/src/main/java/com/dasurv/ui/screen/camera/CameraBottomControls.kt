package com.dasurv.ui.screen.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.ColorSwatch
import com.dasurv.ui.theme.GlassBrush
import com.dasurv.ui.theme.RoseTertiary

@Composable
internal fun CameraBottomControls(
    viewModel: LipCameraViewModel,
    visible: Boolean,
    panelExpanded: Boolean,
    onToggleExpand: () -> Unit,
    showCalibration: Boolean,
    onToggleCalibration: () -> Unit,
    showSuccessPulse: Boolean,
    onShowClientPicker: () -> Unit,
    onHideControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val selectedClient by viewModel.selectedClient.collectAsStateWithLifecycle()
    val captureType by viewModel.captureType.collectAsStateWithLifecycle()
    val followUpInterval by viewModel.followUpInterval.collectAsStateWithLifecycle()
    val customInterval by viewModel.customInterval.collectAsStateWithLifecycle()
    val arMode by viewModel.arMode.collectAsStateWithLifecycle()
    val selectedLipZone by viewModel.selectedLipZone.collectAsStateWithLifecycle()
    val upperRecs by viewModel.upperLipRecommendations.collectAsStateWithLifecycle()
    val lowerRecs by viewModel.lowerLipRecommendations.collectAsStateWithLifecycle()
    val selectedUpperIdx by viewModel.selectedUpperPigmentIndex.collectAsStateWithLifecycle()
    val selectedLowerIdx by viewModel.selectedLowerPigmentIndex.collectAsStateWithLifecycle()
    val captureInProgress by viewModel.captureInProgress.collectAsStateWithLifecycle()
    val lipAnalysis by viewModel.lipAnalysis.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Bottom),
        exit = fadeOut(tween(200)) + shrinkVertically(shrinkTowards = Alignment.Bottom),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Non-AR recommendations row
            if (!arMode && recommendations.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(GlassBrush, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Recommended Pigments",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recommendations.take(6)) { rec ->
                                ColorSwatch(colorHex = rec.pigment.colorHex, label = rec.pigment.name)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // AR Pigment Card
            if (arMode) {
                ArPigmentCard(
                    selectedLipZone = selectedLipZone,
                    upperRecs = upperRecs,
                    lowerRecs = lowerRecs,
                    selectedUpperIdx = selectedUpperIdx,
                    selectedLowerIdx = selectedLowerIdx,
                    onZoneSelected = { viewModel.setSelectedLipZone(it) },
                    onPrevious = { viewModel.swipeToPreviousPigment() },
                    onNext = { viewModel.swipeToNextPigment() }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Settings panel
            SettingsPanel(
                expanded = panelExpanded,
                onToggleExpand = onToggleExpand,
                selectedClientName = selectedClient?.name,
                onClientClick = onShowClientPicker,
                captureType = captureType,
                onCaptureTypeChange = { viewModel.setCaptureType(it) },
                followUpInterval = followUpInterval,
                onFollowUpIntervalChange = { viewModel.setFollowUpInterval(it) },
                customInterval = customInterval,
                onCustomIntervalChange = { viewModel.setCustomInterval(it) },
                arMode = arMode,
                onToggleAr = { viewModel.toggleArMode() }
            )

            Spacer(Modifier.height(12.dp))

            // Shutter button row with calibration toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onToggleCalibration,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Calibrate AR position",
                        tint = if (showCalibration) RoseTertiary else Color.White.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.width(16.dp))

                ShutterButton(
                    enabled = !captureInProgress && lipAnalysis != null,
                    isCapturing = captureInProgress,
                    showSuccess = showSuccessPulse,
                    onClick = {
                        if (selectedClient != null) {
                            viewModel.capturePhoto()
                        } else {
                            viewModel.captureDemoPhoto()
                        }
                    }
                )

                Spacer(Modifier.width(16.dp))

                IconButton(
                    onClick = onHideControls,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = "Hide controls",
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Demo mode hint
            if (selectedClient == null) {
                Text(
                    "Demo mode \u2014 photo won't be saved",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
