package com.dasurv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dasurv.data.local.entity.ClientTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM client_transactions WHERE clientId = :clientId ORDER BY date DESC")
    fun getTransactionsForClient(clientId: Long): Flow<List<ClientTransaction>>

    @Query("SELECT * FROM client_transactions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getTransactionForSession(sessionId: Long): ClientTransaction?

    @Insert
    suspend fun insertTransaction(transaction: ClientTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: ClientTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: ClientTransaction)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM client_transactions WHERE clientId = :clientId")
    fun getBalanceForClient(clientId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM client_transactions WHERE clientId = :clientId AND type = 'CHARGE'")
    fun getTotalChargedForClient(clientId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(ABS(amount)), 0.0) FROM client_transactions WHERE clientId = :clientId AND type IN ('PAYMENT', 'DEPOSIT', 'TIP')")
    fun getTotalPaidForClient(clientId: Long): Flow<Double>
}
