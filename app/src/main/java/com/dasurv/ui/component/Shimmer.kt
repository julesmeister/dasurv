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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dasurv.ui.theme.Gray100
import com.dasurv.ui.theme.Gray200

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    widthFraction: Float = 1f,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    val brush = Brush.linearGradient(
        colors = listOf(Gray200, Gray100, Gray200),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(brush),
    )
}

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    return Brush.linearGradient(
        colors = listOf(Gray200, Gray100, Gray200),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 300f, 0f),
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush()),
    )
}

@Composable
fun ShimmerHost(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, content = content)
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(12.dp)) {
        ShimmerBox(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.7f).height(16.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Spacer(Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f).height(12.dp),
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

@Composable
fun ShimmerSwatchGrid(
    columns: Int = 4,
    rows: Int = 3,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(8.dp)) {
        repeat(rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(columns) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ShimmerBox(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                        )
                        Spacer(Modifier.height(4.dp))
                        ShimmerBox(
                            modifier = Modifier
                                .width(48.dp)
                                .height(10.dp),
                            shape = RoundedCornerShape(4.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
