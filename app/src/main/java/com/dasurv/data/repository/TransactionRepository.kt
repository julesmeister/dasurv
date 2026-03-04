package com.dasurv.data.repository

import com.dasurv.data.local.dao.TransactionDao
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.model.FinancialSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    fun getFinancialSummary(clientId: Long): Flow<FinancialSummary> =
        combine(
            getTotalChargedForClient(clientId),
            getTotalPaidForClient(clientId),
            getBalanceForClient(clientId)
        ) { charged, paid, balance ->
            FinancialSummary(totalCharged = charged, totalPaid = paid, balance = balance)
        }

    fun getTransactionsForClientInRange(clientId: Long, startMs: Long, endMs: Long): Flow<List<ClientTransaction>> =
        transactionDao.getTransactionsForClientInRange(clientId, startMs, endMs)

    fun getTotalChargedInRange(clientId: Long, startMs: Long, endMs: Long): Flow<Double> =
        transactionDao.getTotalChargedInRange(clientId, startMs, endMs)

    fun getTotalPaidInRange(clientId: Long, startMs: Long, endMs: Long): Flow<Double> =
        transactionDao.getTotalPaidInRange(clientId, startMs, endMs)

    fun getFinancialSummaryInRange(clientId: Long, startMs: Long, endMs: Long): Flow<FinancialSummary> =
        combine(
            getTotalChargedInRange(clientId, startMs, endMs),
            getTotalPaidInRange(clientId, startMs, endMs),
        ) { charged, paid ->
            FinancialSummary(totalCharged = charged, totalPaid = paid, balance = charged - paid)
        }
}
