package com.dasurv.data.repository

import com.dasurv.data.local.dao.ClientUpdateDao
import com.dasurv.data.local.entity.ClientUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientUpdateRepository @Inject constructor(
    private val clientUpdateDao: ClientUpdateDao
) {
    fun getUpdatesForClient(clientId: Long) = clientUpdateDao.getUpdatesForClient(clientId)

    fun getUpdatesForSession(sessionId: Long) = clientUpdateDao.getUpdatesForSession(sessionId)

    suspend fun getUpdateById(id: Long) = clientUpdateDao.getUpdateById(id)

    suspend fun insertUpdate(update: ClientUpdate): Long = clientUpdateDao.insertUpdate(update)

    suspend fun updateUpdate(update: ClientUpdate) = clientUpdateDao.updateUpdate(update)

    suspend fun deleteUpdate(update: ClientUpdate) = clientUpdateDao.deleteUpdate(update)
}
