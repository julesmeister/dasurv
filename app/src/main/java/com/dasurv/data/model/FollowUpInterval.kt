package com.dasurv.data.model

enum class FollowUpInterval(val displayName: String) {
    IMMEDIATE_AFTER("Immediate After"),
    THIRTY_DAYS("30 Days"),
    THREE_MONTHS("3 Months"),
    SIX_MONTHS("6 Months"),
    ONE_YEAR("1 Year"),
    CUSTOM("Custom")
}
