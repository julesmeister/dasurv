package com.dasurv.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// M3 Expressive shared palette
val M3Primary = Color(0xFF009688)
val M3PrimaryContainer = Color(0xFFB2DFDB)
val M3OnSurface = Color(0xFF1E293B)
val M3OnSurfaceVariant = Color(0xFF64748B)
val M3SurfaceContainer = Color(0xFFF8FAFC)
val M3GreenColor = Color(0xFF10B981)
val M3GreenContainer = Color(0xFFD1FAE5)
val M3RedColor = Color(0xFFEF4444)
val M3RedContainer = Color(0xFFFEE2E2)
val M3AmberColor = Color(0xFFF59E0B)
val M3AmberContainer = Color(0xFFFEF3C7)
val M3Outline = Color(0xFFE2E8F0)
val M3CyanColor = Color(0xFF06B6D4)
val M3CyanContainer = Color(0xFFCFFAFE)
val M3FieldBg = Color(0xFFF1F5F9)
val M3DialogBg = Color(0xFFF8FAFC)
val M3DialogSurfaceBg = Color(0xFFFCFCFF)
val M3ButtonBarBg = Color(0xFFF1F5F9)
val M3PinkAccent = Color(0xFFEC4899)

private val BackButtonBg = Color(0xFFF3F4F6)

@Composable
fun DasurvBackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(start = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BackButtonBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = M3OnSurface)
        }
    }
}

@Composable
fun M3DateBadge(
    month: String,
    day: String,
    modifier: Modifier = Modifier,
    accentColor: Color = M3OnSurfaceVariant,
    bgColor: Color = M3FieldBg,
) {
    Column(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(top = 5.dp, bottom = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(month, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = accentColor, lineHeight = 12.sp)
        Text(day, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = M3OnSurface, lineHeight = 20.sp)
    }
}

@Composable
fun DasurvAddFab(
    onClick: () -> Unit,
    contentDescription: String = "Add",
    containerColor: Color = M3PrimaryContainer,
    contentColor: Color = M3Primary,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.shadow(
            elevation = 3.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.15f),
            spotColor = Color.Black.copy(alpha = 0.15f),
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription = contentDescription)
    }
}

@Composable
fun DasurvLoadingBox(message: String = "Loading...") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = M3Primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = M3OnSurfaceVariant, fontSize = 16.sp)
        }
    }
}

@Composable
fun DasurvEmptyState(
    icon: ImageVector,
    message: String,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(64.dp), tint = Color(0xFFCBD5E1))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = M3OnSurfaceVariant, fontSize = 16.sp)
        }
    }
}

@Composable
fun DasurvTopAppBarTitle(
    title: String,
    subtitle: String = "",
) {
    Column {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = M3OnSurface)
        if (subtitle.isNotBlank()) {
            Text(subtitle, fontSize = 13.sp, color = M3OnSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

@Composable
fun M3SnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Text(
                data.visuals.message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun DasurvActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun DasurvAnimatedActionRow(
    startPadding: Dp = 70.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val buttonsVisible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = buttonsVisible,
        enter = fadeIn(tween(250, delayMillis = 100)) +
                expandVertically(tween(250, delayMillis = 100)),
    ) {
        Row(
            modifier = Modifier.padding(start = startPadding, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
