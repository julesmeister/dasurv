package com.dasurv.util

/**
 * Formats a duration in seconds to a timer string (MM:SS or HH:MM:SS).
 */
fun formatDurationTimer(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
