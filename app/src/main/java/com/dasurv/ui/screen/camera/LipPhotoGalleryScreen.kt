package com.dasurv.ui.screen.camera

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.CaptureType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LipPhotoGalleryScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (Long) -> Unit = {},
    onNavigateToCaptureResult: (Long) -> Unit = {},
    viewModel: LipPhotoGalleryViewModel = hiltViewModel()
) {
    LaunchedEffect(clientId) { viewModel.loadPhotos(clientId) }

    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val pigmentsByPhoto by viewModel.pigmentsByPhoto.collectAsStateWithLifecycle()
    val expandedPhotoId by viewModel.expandedPhotoId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val groupedPhotos = photos.groupBy { it.captureType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lip Photos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No lip photos yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val typeOrder = listOf(CaptureType.BEFORE, CaptureType.AFTER, CaptureType.FOLLOW_UP)
                for (type in typeOrder) {
                    val typePhotos = groupedPhotos[type] ?: continue
                    item {
                        Text(
                            when (type) {
                                CaptureType.BEFORE -> "Before"
                                CaptureType.AFTER -> "After"
                                CaptureType.FOLLOW_UP -> "Follow-up"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(typePhotos, key = { it.id }) { photo ->
                        LipPhotoCard(
                            photo = photo,
                            pigments = pigmentsByPhoto[photo.id] ?: emptyList(),
                            isExpanded = expandedPhotoId == photo.id,
                            onToggleExpand = { viewModel.toggleExpanded(photo.id) },
                            onViewSummary = { onNavigateToSummary(photo.id) },
                            onOpenAnalysis = { onNavigateToCaptureResult(photo.id) },
                            onUpdateNotes = { notes -> viewModel.updateNotes(photo, notes) },
                            onUpdateCaptureType = { newType, interval ->
                                viewModel.updateCaptureType(photo, newType, interval)
                            },
                            onDelete = { viewModel.deletePhoto(photo) },
                            onShare = { viewModel.sharePhoto(context, photo) }
                        )
                    }
                }
            }
        }
    }
}
