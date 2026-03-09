package com.dasurv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dasurv.data.local.entity.ClientUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientUpdateDao {

    @Query("SELECT * FROM client_updates WHERE clientId = :clientId ORDER BY date DESC")
    fun getUpdatesForClient(clientId: Long): Flow<List<ClientUpdate>>

    @Query("SELECT * FROM client_updates WHERE sessionId = :sessionId ORDER BY date DESC")
    fun getUpdatesForSession(sessionId: Long): Flow<List<ClientUpdate>>

    @Query("SELECT * FROM client_updates WHERE id = :id")
    suspend fun getUpdateById(id: Long): ClientUpdate?

    @Insert
    suspend fun insertUpdate(update: ClientUpdate): Long

    @Update
    suspend fun updateUpdate(update: ClientUpdate)

    @Delete
    suspend fun deleteUpdate(update: ClientUpdate)
}
