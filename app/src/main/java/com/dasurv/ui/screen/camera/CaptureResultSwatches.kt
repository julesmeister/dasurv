package com.dasurv.ui.screen.camera

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.dasurv.ui.util.parseHexSafe
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.Pigment
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.util.ColorMatcher

@Composable
internal fun RecommendedPigmentSwatch(
    rec: ColorMatcher.PigmentRecommendation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "rec_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "rec_border"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(parseHexSafe(rec.pigment.colorHex))
                .border(borderWidth, RosePrimary, CircleShape)
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), CircleShape)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            rec.pigment.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 64.dp)
        )
        Text(
            "${(rec.matchScore * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = RosePrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun AllPigmentSwatch(
    pigment: Pigment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "all_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "all_border"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(parseHexSafe(pigment.colorHex))
                .border(borderWidth, RosePrimary, CircleShape)
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), CircleShape)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            pigment.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 56.dp)
        )
    }
}
