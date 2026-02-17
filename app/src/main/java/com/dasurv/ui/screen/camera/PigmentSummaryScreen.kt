package com.dasurv.ui.screen.camera

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.model.Pigment
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary
import com.dasurv.ui.util.parseHexSafe
import com.dasurv.util.HealingGuide

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigmentSummaryScreen(
    photoId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PigmentSummaryViewModel = hiltViewModel()
) {
    val photoUri by viewModel.photoUri.collectAsStateWithLifecycle()
    val dualAnalysis by viewModel.dualAnalysis.collectAsStateWithLifecycle()
    val upperPigment by viewModel.upperPigment.collectAsStateWithLifecycle()
    val lowerPigment by viewModel.lowerPigment.collectAsStateWithLifecycle()
    val upperPrediction by viewModel.upperPrediction.collectAsStateWithLifecycle()
    val lowerPrediction by viewModel.lowerPrediction.collectAsStateWithLifecycle()
    val blendedUpperHex by viewModel.blendedUpperHex.collectAsStateWithLifecycle()
    val blendedLowerHex by viewModel.blendedLowerHex.collectAsStateWithLifecycle()
    val captureType by viewModel.captureType.collectAsStateWithLifecycle()
    val followUpInterval by viewModel.followUpInterval.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(photoId) { viewModel.loadPhoto(photoId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pigment Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RoseTertiary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Photo Preview
                photoUri?.let { uri ->
                    val bmp = remember(uri) { BitmapFactory.decodeFile(uri) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Lip photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

                // 2. Treatment Phase
                TreatmentPhaseCard(captureType, followUpInterval)

                // 3. Natural Lip Analysis
                dualAnalysis?.let { dual ->
                    SummaryCard(title = "Natural Lip Analysis") {
                        dual.upperLip?.let { upper ->
                            LipAnalysisRow("Upper Lip", upper.dominantColorHex, upper.category.displayName)
                        }
                        dual.lowerLip?.let { lower ->
                            LipAnalysisRow("Lower Lip", lower.dominantColorHex, lower.category.displayName)
                        }
                    }
                }

                // 4. Selected Pigments
                if (upperPigment != null || lowerPigment != null) {
                    SummaryCard(title = "Selected Pigments") {
                        upperPigment?.let { PigmentInfoRow("Upper Lip", it) }
                        if (upperPigment != null && lowerPigment != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                        lowerPigment?.let { PigmentInfoRow("Lower Lip", it) }
                    }
                }

                // 5. Predicted Result
                if (blendedUpperHex != null || blendedLowerHex != null) {
                    SummaryCard(title = "Predicted Result") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Fresh column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fresh", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                blendedUpperHex?.let {
                                    ColorSwatch(it, "Upper")
                                    Spacer(Modifier.height(4.dp))
                                }
                                blendedLowerHex?.let {
                                    ColorSwatch(it, "Lower")
                                }
                            }
                            // After Healing column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("After Healing", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                upperPrediction?.let {
                                    ColorSwatch(it.healedColorHex, "Upper")
                                    Spacer(Modifier.height(4.dp))
                                }
                                lowerPrediction?.let {
                                    ColorSwatch(it.healedColorHex, "Lower")
                                }
                            }
                        }
                    }
                }

                // 6. Healing Details
                if (upperPrediction != null || lowerPrediction != null) {
                    SummaryCard(title = "Healing Details") {
                        upperPrediction?.let { pred ->
                            HealingDetailRow("Upper Lip", pred)
                        }
                        if (upperPrediction != null && lowerPrediction != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                        lowerPrediction?.let { pred ->
                            HealingDetailRow("Lower Lip", pred)
                        }
                    }
                }

                // 7. Category Advice
                dualAnalysis?.let { dual ->
                    val category = dual.lowerLip?.category ?: dual.upperLip?.category
                    category?.let { cat ->
                        val adviceText = lowerPrediction?.categoryAdvice
                            ?: upperPrediction?.categoryAdvice ?: ""
                        if (adviceText.isNotBlank()) {
                            SummaryCard(title = "Advice for ${cat.displayName} Lips") {
                                Text(
                                    adviceText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 8. Healing Timeline
                val timeline = upperPrediction?.timeline ?: lowerPrediction?.timeline
                if (timeline != null) {
                    SummaryCard(title = "Healing Timeline") {
                        timeline.forEachIndexed { index, step ->
                            TimelineStepRow(step, isLast = index == timeline.lastIndex)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun TreatmentPhaseCard(captureType: CaptureType, followUpInterval: String?) {
    val (label, color) = when (captureType) {
        CaptureType.BEFORE -> "Before Treatment" to Color(0xFF2196F3)
        CaptureType.AFTER -> "After Treatment" to Color(0xFF4CAF50)
        CaptureType.FOLLOW_UP -> {
            val suffix = if (!followUpInterval.isNullOrBlank()) " ($followUpInterval)" else ""
            "Follow-up$suffix" to Color(0xFFFF9800)
        }
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun LipAnalysisRow(label: String, colorHex: String, categoryName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(parseHexSafe(colorHex))
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "$categoryName  $colorHex",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PigmentInfoRow(zone: String, pigment: Pigment) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(zone, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(parseHexSafe(pigment.colorHex))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(pigment.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    pigment.brand.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (pigment.undertone.isNotBlank()) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(pigment.undertone.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(28.dp)
                )
            }
            if (pigment.intensity.isNotBlank()) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(pigment.intensity.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(hex: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(parseHexSafe(hex))
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HealingDetailRow(zone: String, prediction: HealingGuide.HealingPrediction) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(zone, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        DetailItem("Retention", "~${prediction.retentionPercent}% retained after healing")
        DetailItem("Color shift", prediction.colorShiftDescription)
        if (prediction.pigmentNotes.isNotBlank()) {
            DetailItem("Note", prediction.pigmentNotes)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimelineStepRow(step: HealingGuide.TimelineStep, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(RosePrimary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(RosePrimary.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                step.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
