package com.dasurv.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
            .clip(RoundedCornerShape(50))
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
