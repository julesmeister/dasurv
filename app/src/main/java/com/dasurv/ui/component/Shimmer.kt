package com.dasurv.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerHost(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, content = content)
}

/** Shimmer placeholder for a card-like item */
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.medium
    )
}

/** Shimmer placeholder for a list item with icon + text */
@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(12.dp)) {
        ShimmerBox(
            modifier = Modifier.size(48.dp),
            shape = CircleShape
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.7f).height(16.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f).height(12.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/** Shimmer grid of color swatches */
@Composable
fun ShimmerSwatchGrid(
    columns: Int = 4,
    rows: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        repeat(rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(columns) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        ShimmerBox(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape
                        )
                        Spacer(Modifier.height(4.dp))
                        ShimmerBox(
                            modifier = Modifier
                                .width(48.dp)
                                .height(10.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
