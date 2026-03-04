package com.dasurv.ui.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.ClientTransaction
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
    private val transactionRepository: TransactionRepository
) : ViewModel() {

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
        if (id == 0L) flowOf(emptyList())
        else transactionRepository.getTransactionsForClientInRange(id, start, end)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val monthSummary: StateFlow<FinancialSummary> = combine(_clientId, monthRange) { id, (start, end) ->
        Triple(id, start, end)
    }.flatMapLatest { (id, start, end) ->
        if (id == 0L) flowOf(FinancialSummary())
        else transactionRepository.getFinancialSummaryInRange(id, start, end)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, FinancialSummary())

    val allTimeSummary: StateFlow<FinancialSummary> = _clientId.flatMapLatest { id ->
        if (id == 0L) flowOf(FinancialSummary())
        else transactionRepository.getFinancialSummary(id)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, FinancialSummary())

    fun loadClient(clientId: Long) {
        _clientId.value = clientId
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
        }
    }

    fun deleteTransaction(transaction: ClientTransaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}
