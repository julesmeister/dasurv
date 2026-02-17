package com.dasurv.ui.screen.tryon

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.Pigment
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.util.parseHexSafe

@Composable
internal fun PigmentSwatchItem(
    pigment: Pigment,
    isSelected: Boolean,
    isFavorite: Boolean,
    onTap: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val color = parseHexSafe(pigment.colorHex)

    // Spring-based scale animation on selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "swatch_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "swatch_border"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTap() }
            .padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
                    .border(borderWidth, RosePrimary, CircleShape)
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), CircleShape)
            )
            // Animated heart icon
            AnimatedContent(
                targetState = isFavorite,
                transitionSpec = { scaleIn(spring(dampingRatio = 0.4f)) togetherWith scaleOut(tween(100)) },
                label = "heart_anim"
            ) { fav ->
                Icon(
                    if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (fav) "Remove favorite" else "Add favorite",
                    tint = if (fav) Color.Red else Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .clickable { onToggleFavorite() }
                        .padding(2.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            pigment.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 64.dp)
        )
    }
}
