package com.dasurv.data.local

import androidx.room.TypeConverter
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.local.entity.PaymentMethod
import com.dasurv.data.local.entity.RecurrenceType
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.data.local.entity.UsageLipArea
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = JSONArray(list).toString()

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return (0 until array.length()).map { array.getString(it) }
    }

    @TypeConverter
    fun fromAppointmentStatus(status: AppointmentStatus): String = status.name

    @TypeConverter
    fun toAppointmentStatus(value: String): AppointmentStatus = AppointmentStatus.valueOf(value)

    @TypeConverter
    fun fromCaptureType(type: CaptureType): String = type.name

    @TypeConverter
    fun toCaptureType(value: String): CaptureType = CaptureType.valueOf(value)

    @TypeConverter
    fun fromLipZone(zone: LipZone): String = zone.name

    @TypeConverter
    fun toLipZone(value: String): LipZone = LipZone.valueOf(value)

    @TypeConverter
    fun fromUsageLipArea(area: UsageLipArea): String = area.name

    @TypeConverter
    fun toUsageLipArea(value: String): UsageLipArea = UsageLipArea.valueOf(value)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromRecurrenceType(type: RecurrenceType): String = type.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType = RecurrenceType.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let { PaymentMethod.valueOf(it) }
}
