package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: Long): Staff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Update
    suspend fun updateStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)
}
