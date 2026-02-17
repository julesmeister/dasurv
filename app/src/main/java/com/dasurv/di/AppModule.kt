package com.dasurv.di

import android.content.Context
import androidx.room.Room
import com.dasurv.data.local.DatabaseMigrations
import com.dasurv.data.local.DasurvDatabase
import com.dasurv.data.local.dao.AppointmentDao
import com.dasurv.data.local.dao.ClientDao
import com.dasurv.data.local.dao.EquipmentDao
import com.dasurv.data.local.dao.ClientPigmentPreferenceDao
import com.dasurv.data.local.dao.LipPhotoDao
import com.dasurv.data.local.dao.PigmentBottleDao
import com.dasurv.data.local.dao.SessionDao
import com.dasurv.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DasurvDatabase {
        return Room.databaseBuilder(
            context,
            DasurvDatabase::class.java,
            "dasurv_database"
        )
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4,
                DatabaseMigrations.MIGRATION_4_5,
                DatabaseMigrations.MIGRATION_5_6,
                DatabaseMigrations.MIGRATION_6_7,
                DatabaseMigrations.MIGRATION_8_9,
                DatabaseMigrations.MIGRATION_9_10
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideClientDao(database: DasurvDatabase): ClientDao = database.clientDao()

    @Provides
    fun provideSessionDao(database: DasurvDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideEquipmentDao(database: DasurvDatabase): EquipmentDao = database.equipmentDao()

    @Provides
    fun provideAppointmentDao(database: DasurvDatabase): AppointmentDao = database.appointmentDao()

    @Provides
    fun provideLipPhotoDao(database: DasurvDatabase): LipPhotoDao = database.lipPhotoDao()

    @Provides
    fun provideClientPigmentPreferenceDao(database: DasurvDatabase): ClientPigmentPreferenceDao =
        database.clientPigmentPreferenceDao()

    @Provides
    fun providePigmentBottleDao(database: DasurvDatabase): PigmentBottleDao =
        database.pigmentBottleDao()

    @Provides
    fun provideTransactionDao(database: DasurvDatabase): TransactionDao =
        database.transactionDao()
}
