package com.dasurv.ui.screen.equipment

import androidx.compose.ui.graphics.Color
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3AmberContainer
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3GreenContainer
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3RedContainer

internal fun stockBadgeText(item: Equipment): String {
    return when {
        item.stockQuantity == 0 -> "Out"
        item.stockQuantity <= 5 -> "${item.stockQuantity} pcs"
        else -> "${item.stockQuantity} pcs"
    }
}

internal fun stockBadgeColor(item: Equipment): Color {
    return when {
        item.stockQuantity == 0 -> M3RedColor
        item.stockQuantity <= 5 -> M3AmberColor
        else -> M3GreenColor
    }
}

internal fun stockBadgeContainer(item: Equipment): Color {
    return when {
        item.stockQuantity == 0 -> M3RedContainer
        item.stockQuantity <= 5 -> M3AmberContainer
        else -> M3GreenContainer
    }
}
