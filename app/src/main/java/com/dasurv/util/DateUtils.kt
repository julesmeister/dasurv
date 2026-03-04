package com.dasurv.util

import android.app.DatePickerDialog
import android.content.Context
import java.util.*

/**
 * Shows a [DatePickerDialog] initialized to [initialMillis] and calls [onDateSelected]
 * with the chosen date in millis (preserving the time-of-day from [initialMillis]).
 */
fun showDatePicker(
    context: Context,
    initialMillis: Long = System.currentTimeMillis(),
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            cal.set(year, month, day)
            onDateSelected(cal.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/**
 * Shows a [DatePickerDialog] and returns the selected date with time set to start-of-day (00:00:00.000).
 */
fun showDatePickerStartOfDay(
    context: Context,
    initialMillis: Long = System.currentTimeMillis(),
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            cal.set(year, month, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            onDateSelected(cal.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/**
 * Shows a [DatePickerDialog] and returns the selected date with time set to end-of-day (23:59:59.999).
 */
fun showDatePickerEndOfDay(
    context: Context,
    initialMillis: Long = System.currentTimeMillis(),
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            cal.set(year, month, day, 23, 59, 59)
            cal.set(Calendar.MILLISECOND, 999)
            onDateSelected(cal.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/** Returns the start-of-month (00:00:00.000 on day 1) and start-of-next-month for the given [date]. */
fun Date.monthRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        time = this@monthRange
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    return start to cal.timeInMillis
}
