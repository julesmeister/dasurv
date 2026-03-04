package com.dasurv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dasurv.data.local.dao.AppointmentDao
import com.dasurv.data.local.dao.ClientDao
import com.dasurv.data.local.dao.EquipmentDao
import com.dasurv.data.local.dao.ClientPigmentPreferenceDao
import com.dasurv.data.local.dao.LipPhotoDao
import com.dasurv.data.local.dao.PigmentBottleDao
import com.dasurv.data.local.dao.SessionDao
import com.dasurv.data.local.dao.TransactionDao
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.ClientPigmentPreference
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentUsage
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.EquipmentPurchase
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.SessionPigment

@Database(
    entities = [
        Client::class,
        Session::class,
        SessionPigment::class,
        Equipment::class,
        Appointment::class,
        SessionEquipment::class,
        EquipmentUsage::class,
        LipPhoto::class,
        LipPhotoPigment::class,
        ClientPigmentPreference::class,
        PigmentBottle::class,
        PigmentBottleUsage::class,
        ClientTransaction::class,
        EquipmentPurchase::class
    ],
    version = 13,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DasurvDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun sessionDao(): SessionDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun lipPhotoDao(): LipPhotoDao
    abstract fun clientPigmentPreferenceDao(): ClientPigmentPreferenceDao
    abstract fun pigmentBottleDao(): PigmentBottleDao
    abstract fun transactionDao(): TransactionDao
}
