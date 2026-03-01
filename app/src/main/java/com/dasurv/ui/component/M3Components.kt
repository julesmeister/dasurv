package com.dasurv.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// M3 Expressive shared palette
val M3Primary = Color(0xFF6366F1)
val M3PrimaryContainer = Color(0xFFE0E7FF)
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
    bgColor: Color = Color(0xFFF0F1FA),
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
fun M3ListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val translationY = remember { Animatable(24f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(300)) }
        launch { translationY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                this.alpha = alpha.value
                this.translationY = translationY.value
            }
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun M3ListRow(
    icon: ImageVector,
    iconTint: Color = M3Primary,
    iconBg: Color = M3PrimaryContainer,
    label: String,
    description: String = "",
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val alpha = remember { Animatable(0f) }
    val translationX = remember { Animatable(-12f) }
    val iconScale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(250)) }
        launch { translationX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { iconScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha.value
                this.translationX = translationX.value
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = M3OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (description.isNotBlank()) {
                    Text(
                        description,
                        fontSize = 12.sp,
                        color = M3OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        trailing()
    }
}

@Composable
fun M3ListRowInitial(
    initial: String,
    initialColor: Color = M3Primary,
    initialBg: Color = M3PrimaryContainer,
    label: String,
    description: String = "",
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val alpha = remember { Animatable(0f) }
    val translationX = remember { Animatable(-12f) }
    val initialScale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(250)) }
        launch { translationX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { initialScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha.value
                this.translationX = translationX.value
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = initialScale.value
                        scaleY = initialScale.value
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(initialBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initial.take(1).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = initialColor,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = M3OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (description.isNotBlank()) {
                    Text(
                        description,
                        fontSize = 12.sp,
                        color = M3OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        trailing()
    }
}

@Composable
fun M3StatusBadge(
    text: String,
    color: Color,
    containerColor: Color,
    icon: ImageVector? = null,
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

@Composable
fun M3ValueBadge(
    text: String,
    color: Color = M3Primary,
    containerColor: Color = M3PrimaryContainer,
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
fun M3ListDivider() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(
            expandFrom = Alignment.CenterHorizontally,
            animationSpec = tween(300),
        ) + fadeIn(tween(300)),
    ) {
        HorizontalDivider(
            color = M3Outline.copy(alpha = 0.5f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
fun M3SectionLabel(
    icon: ImageVector,
    title: String,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(300))
    }

    Row(
        modifier = Modifier
            .graphicsLayer { this.alpha = alpha.value }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = M3Primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = M3Primary,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
fun M3SummaryTab(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (isActive) 1f else 0.85f))
            .clickable(onClick = onClick)
            .then(if (isActive) Modifier.background(Color.White.copy(alpha = 0.15f)) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.White.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
        }
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

@Composable
fun DasurvAddFab(
    onClick: () -> Unit,
    contentDescription: String = "Add",
    containerColor: Color = M3Primary,
    shape: Shape = CircleShape,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = Color.White,
        shape = shape,
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
