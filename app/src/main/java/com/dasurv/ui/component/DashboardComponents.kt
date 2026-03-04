package com.dasurv.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency

// M3 Expressive Color System
val SkyBlue = M3Primary
val SkyBlueContainer = M3PrimaryContainer
val Green = M3GreenColor
val Red = M3RedColor
val Teal = Color(0xFF14B8A6)
val Blue = Color(0xFF0EA5E9)
val Orange = Color(0xFFF97316)
val Pink = M3PinkAccent

@Composable
fun M3SectionHeader(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
) {
    if (actions != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun M3TonalActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
    badge: @Composable (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = tint.copy(alpha = 0.12f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(22.dp), tint = tint)
            }
        }
        if (badge != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                badge()
            }
        }
    }
}

@Composable
fun M3TonalChip(
    text: String,
    bg: Color,
    modifier: Modifier = Modifier,
    textColor: Color = bg,
    icon: ImageVector? = null,
    iconTint: Color = textColor,
    trailingChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(bg.copy(alpha = 0.12f))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (trailingChevron) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun M3AmountDisplay(
    amount: String,
    color: Color,
    modifier: Modifier = Modifier,
    currencySize: Int = 20,
    amountSize: Int = 36,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "\u20B1",
            fontSize = currencySize.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(top = (amountSize / 4).dp, end = 2.dp),
        )
        Text(
            text = amount,
            fontSize = amountSize.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            lineHeight = (amountSize + 4).sp,
        )
    }
}

@Composable
fun ChargedPaidRow(charged: Double, paid: Double, modifier: Modifier = Modifier) {
    val spacing = DasurvTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = M3FieldBg
        ) {
            Column(
                modifier = Modifier.padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Charged",
                    style = MaterialTheme.typography.labelSmall,
                    color = M3OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "$${charged.formatCurrency()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface
                )
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = M3FieldBg
        ) {
            Column(
                modifier = Modifier.padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Paid",
                    style = MaterialTheme.typography.labelSmall,
                    color = M3OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "$${paid.formatCurrency()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface
                )
            }
        }
    }
}
