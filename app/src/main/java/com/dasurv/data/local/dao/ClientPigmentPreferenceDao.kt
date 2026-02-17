package com.dasurv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dasurv.data.local.entity.ClientPigmentPreference
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientPigmentPreferenceDao {

    @Query("SELECT * FROM client_pigment_preferences WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getPreferencesForClient(clientId: Long): Flow<List<ClientPigmentPreference>>

    @Query("SELECT * FROM client_pigment_preferences WHERE clientId = :clientId AND pigmentName = :name AND pigmentBrand = :brand LIMIT 1")
    suspend fun getPreference(clientId: Long, name: String, brand: String): ClientPigmentPreference?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPreference(preference: ClientPigmentPreference): Long

    @Delete
    suspend fun deletePreference(preference: ClientPigmentPreference)

    @Query("DELETE FROM client_pigment_preferences WHERE clientId = :clientId AND pigmentName = :name AND pigmentBrand = :brand")
    suspend fun deleteByPigment(clientId: Long, name: String, brand: String)
}
