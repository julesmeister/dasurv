package com.dasurv.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ══════════════════════════════════════════════════════════════════════════════
// Detail Screen Components — Lotel-style rich detail view primitives
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Section header with icon + colored title for detail screens.
 * Use above an M3ListCard to label each section.
 */
@Composable
fun DetailSectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color = M3Primary,
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = accentColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            letterSpacing = 0.5.sp,
        )
    }
}

/**
 * Row with icon box + label + value pill badge.
 * Perfect for displaying key-value pairs in detail cards.
 */
@Composable
fun DetailValueRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color = M3Primary,
    iconBg: Color = M3Primary.copy(alpha = 0.10f),
    valueBg: Color = M3Primary.copy(alpha = 0.08f),
    valueColor: Color = M3Primary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurface,
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = valueBg,
        ) {
            Text(
                value,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
            )
        }
    }
}

/**
 * Subtle divider for separating rows within a detail card.
 */
@Composable
fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = M3Outline.copy(alpha = 0.5f),
    )
}

/**
 * Reusable action button with icon, loading state, and customizable colors.
 * Use inside Row with Modifier.weight(1f) for side-by-side actions.
 */
@Composable
fun DetailActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    isLoading: Boolean = false,
    loadingLabel: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color,
        enabled = !isLoading,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = textColor,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(icon, null, Modifier.size(20.dp), tint = textColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isLoading && loadingLabel != null) loadingLabel else label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
        }
    }
}
