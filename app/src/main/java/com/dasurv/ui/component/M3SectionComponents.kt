package com.dasurv.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.ui.theme.DasurvTheme
import kotlinx.coroutines.launch

@Composable
fun M3SummaryTab(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { alpha.animateTo(1f, tween(250)) }
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (isActive) 1f else 0.85f))
            .then(if (isActive) Modifier.background(Color.White.copy(alpha = 0.15f)) else Modifier)
            .clickable(onClick = onClick)
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
fun <K> DasurvChipSelectorRow(
    items: List<Pair<K, String>>,
    selectedKey: K?,
    onSelect: (K?) -> Unit,
    allLabel: String = "All",
    accentColor: Color = M3Primary,
    containerColor: Color = accentColor.copy(alpha = 0.10f),
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            val isSelected = selectedKey == null
            Surface(
                onClick = { onSelect(null) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) accentColor else containerColor,
            ) {
                Text(
                    allLabel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else accentColor,
                )
            }
        }
        items(items.size) { index ->
            val (key, label) = items[index]
            val isSelected = selectedKey == key
            Surface(
                onClick = { onSelect(key) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) accentColor else containerColor,
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else accentColor,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun DasurvSummaryStrip(
    label: String,
    value: String,
    accentColor: Color = M3Primary,
    secondaryLabel: String? = null,
    secondaryValue: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(label, fontSize = 13.sp, color = M3OnSurfaceVariant)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
        if (secondaryLabel != null && secondaryValue != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(secondaryLabel, fontSize = 13.sp, color = M3OnSurfaceVariant)
                Text(secondaryValue, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = M3OnSurface)
            }
        }
    }
}

@Composable
fun M3SectionHeader(
    title: String,
    color: Color = M3Primary,
    icon: ImageVector? = null,
    horizontalPadding: Dp = DasurvTheme.spacing.lg,
) {
    val spacing = DasurvTheme.spacing
    if (icon != null) {
        Row(
            modifier = Modifier.padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = spacing.sm,
                bottom = spacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(18.dp),
                tint = color,
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                letterSpacing = 0.5.sp,
            )
        }
    } else {
        Text(
            title,
            modifier = Modifier.padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = spacing.sm,
                bottom = spacing.xs,
            ),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
fun M3ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    val spacing = DasurvTheme.spacing
    M3ListCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = M3RedColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                message,
                fontSize = 13.sp,
                color = M3RedColor,
            )
        }
    }
}
