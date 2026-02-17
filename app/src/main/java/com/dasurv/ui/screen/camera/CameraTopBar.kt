package com.dasurv.ui.screen.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun CameraTopBar(
    onNavigateBack: () -> Unit,
    galleryMode: Boolean,
    onExitGallery: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            "Lip Camera",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        if (galleryMode) {
            IconButton(onClick = onExitGallery) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Switch to camera",
                    tint = Color.White
                )
            }
        } else {
            IconButton(onClick = onOpenGallery) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = "Open gallery",
                    tint = Color.White
                )
            }
        }
    }
}
