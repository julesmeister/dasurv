package com.dasurv.ui.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.repository.TransactionRepository
import com.dasurv.ui.screen.client.FinancialSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _clientId = MutableStateFlow(0L)

    val transactions: StateFlow<List<ClientTransaction>> = _clientId.flatMapLatest { id ->
        if (id == 0L) flowOf(emptyList())
        else transactionRepository.getTransactionsForClient(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<FinancialSummary> = _clientId.flatMapLatest { id ->
        if (id == 0L) flowOf(FinancialSummary())
        else combine(
            transactionRepository.getTotalChargedForClient(id),
            transactionRepository.getTotalPaidForClient(id),
            transactionRepository.getBalanceForClient(id)
        ) { charged, paid, balance ->
            FinancialSummary(totalCharged = charged, totalPaid = paid, balance = balance)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun loadClient(clientId: Long) {
        _clientId.value = clientId
    }

    fun addTransaction(transaction: ClientTransaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: ClientTransaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}
