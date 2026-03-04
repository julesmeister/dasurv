package com.dasurv.data.repository

import com.dasurv.data.local.dao.StaffDao
import com.dasurv.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val staffDao: StaffDao
) {
    fun getAllStaff(): Flow<List<Staff>> = staffDao.getAllStaff()

    fun getActiveStaff(): Flow<List<Staff>> = staffDao.getActiveStaff()

    suspend fun getStaffById(id: Long): Staff? = staffDao.getStaffById(id)

    suspend fun insertStaff(staff: Staff): Long = staffDao.insertStaff(staff)

    suspend fun updateStaff(staff: Staff) = staffDao.updateStaff(staff)

    suspend fun deleteStaff(staff: Staff) = staffDao.deleteStaff(staff)
}
