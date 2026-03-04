package com.dasurv.ui.screen.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.TransactionRepository
import com.dasurv.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExportState {
    data object Idle : ExportState()
    data object Exporting : ExportState()
    data class Done(val uri: Uri, val type: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val sessionRepository: SessionRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState

    fun exportClients(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val clients = clientRepository.getAllClients().first()
                val uri = CsvExporter.exportClients(context, clients)
                _exportState.value = ExportState.Done(uri, "Clients")
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportSessions(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val sessions = sessionRepository.getAllSessions().first()
                val uri = CsvExporter.exportSessions(context, sessions)
                _exportState.value = ExportState.Done(uri, "Sessions")
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportTransactions(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                val uri = CsvExporter.exportTransactions(context, transactions)
                _exportState.value = ExportState.Done(uri, "Transactions")
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}
