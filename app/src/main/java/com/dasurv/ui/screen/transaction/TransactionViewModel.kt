package com.dasurv.ui.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.TransactionRepository
import com.dasurv.data.model.FinancialSummary
import com.dasurv.util.DefaultSubscribePolicy
import com.dasurv.util.FMT_MONTH_YEAR
import com.dasurv.util.monthRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    val clients: StateFlow<List<Client>> = clientRepository.getAllClients()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    companion object {
        const val ALL_CLIENTS = -1L
    }

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }

    private val _clientId = MutableStateFlow(0L)

    private val _selectedDate = MutableStateFlow(Date())

    private val monthRange: StateFlow<Pair<Long, Long>> = _selectedDate.map { date ->
        date.monthRange()
    }.stateIn(viewModelScope, DefaultSubscribePolicy, Date().monthRange())

    val formattedMonth: StateFlow<String> = _selectedDate.map { date ->
        SimpleDateFormat(FMT_MONTH_YEAR, Locale.getDefault()).format(date)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, SimpleDateFormat(FMT_MONTH_YEAR, Locale.getDefault()).format(Date()))

    val transactions: StateFlow<List<ClientTransaction>> = combine(_clientId, monthRange) { id, (start, end) ->
        Triple(id, start, end)
    }.flatMapLatest { (id, start, end) ->
        when (id) {
            0L -> flowOf(emptyList())
            ALL_CLIENTS -> transactionRepository.getAllTransactionsInRange(start, end)
            else -> transactionRepository.getTransactionsForClientInRange(id, start, end)
        }
    }.stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val monthSummary: StateFlow<FinancialSummary> = transactions.map { txList ->
        val charged = txList.filter { it.type == TransactionType.CHARGE }.sumOf { it.amount }
        val paid = txList.filter { it.type != TransactionType.CHARGE }.sumOf { kotlin.math.abs(it.amount) }
        FinancialSummary(totalCharged = charged, totalPaid = paid, balance = charged - paid)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, FinancialSummary())

    val allTimeSummary: StateFlow<FinancialSummary> = _clientId.flatMapLatest { id ->
        when (id) {
            0L -> flowOf(FinancialSummary())
            ALL_CLIENTS -> transactionRepository.getFinancialSummaryAll()
            else -> transactionRepository.getFinancialSummary(id)
        }
    }.stateIn(viewModelScope, DefaultSubscribePolicy, FinancialSummary())

    fun loadClient(clientId: Long) {
        _clientId.value = clientId
    }

    fun loadAllClients() {
        _clientId.value = ALL_CLIENTS
    }

    fun previousMonth() {
        _selectedDate.update { date ->
            Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, -1)
            }.time
        }
    }

    fun nextMonth() {
        _selectedDate.update { date ->
            Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, 1)
            }.time
        }
    }

    fun setDate(date: Date) {
        _selectedDate.value = date
    }

    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    fun addTransaction(transaction: ClientTransaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
            _snackbarMessage.value = "Transaction added"
        }
    }

    fun deleteTransaction(transaction: ClientTransaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
            _snackbarMessage.value = "Transaction deleted"
        }
    }

    fun goToLatestTransaction() {
        viewModelScope.launch {
            val clientId = _clientId.value
            if (clientId == 0L) return@launch
            val tx = when (clientId) {
                ALL_CLIENTS -> transactionRepository.getLatestTransaction()
                else -> transactionRepository.getLatestTransactionForClient(clientId)
            }
            if (tx == null) {
                _snackbarMessage.value = "No transactions yet"
                return@launch
            }
            _selectedDate.value = Date(tx.date)
        }
    }
}
