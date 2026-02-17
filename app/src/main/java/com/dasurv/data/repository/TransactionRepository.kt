package com.dasurv.data.repository

import com.dasurv.data.local.dao.TransactionDao
import com.dasurv.data.local.entity.ClientTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getTransactionsForClient(clientId: Long): Flow<List<ClientTransaction>> =
        transactionDao.getTransactionsForClient(clientId)

    suspend fun getTransactionForSession(sessionId: Long): ClientTransaction? =
        transactionDao.getTransactionForSession(sessionId)

    suspend fun insertTransaction(transaction: ClientTransaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: ClientTransaction) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: ClientTransaction) =
        transactionDao.deleteTransaction(transaction)

    fun getBalanceForClient(clientId: Long): Flow<Double> =
        transactionDao.getBalanceForClient(clientId)

    fun getTotalChargedForClient(clientId: Long): Flow<Double> =
        transactionDao.getTotalChargedForClient(clientId)

    fun getTotalPaidForClient(clientId: Long): Flow<Double> =
        transactionDao.getTotalPaidForClient(clientId)
}
