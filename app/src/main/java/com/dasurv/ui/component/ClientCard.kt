package com.dasurv.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Client

enum class CardPosition { First, Middle, Last, Only }

@Composable
fun ClientCard(
    client: Client,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Only
) {
    val largeRadius = 24.dp
    val smallRadius = 6.dp

    val shape = when (position) {
        CardPosition.Only -> RoundedCornerShape(largeRadius)
        CardPosition.First -> RoundedCornerShape(
            topStart = largeRadius, topEnd = largeRadius,
            bottomStart = smallRadius, bottomEnd = smallRadius
        )
        CardPosition.Middle -> RoundedCornerShape(smallRadius)
        CardPosition.Last -> RoundedCornerShape(
            topStart = smallRadius, topEnd = smallRadius,
            bottomStart = largeRadius, bottomEnd = largeRadius
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E0DC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF8D7B74)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (client.phone.isNotBlank()) {
                    Text(
                        text = client.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
