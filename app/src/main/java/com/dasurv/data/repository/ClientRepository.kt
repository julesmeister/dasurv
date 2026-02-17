package com.dasurv.data.repository

import com.dasurv.data.local.dao.ClientDao
import com.dasurv.data.local.entity.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao
) {
    fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()

    suspend fun getClientById(id: Long): Client? = clientDao.getClientById(id)

    fun searchClients(query: String): Flow<List<Client>> = clientDao.searchClients(query)

    suspend fun insertClient(client: Client): Long = clientDao.insertClient(client)

    suspend fun updateClient(client: Client) = clientDao.updateClient(client)

    suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)
}
