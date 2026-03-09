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

    private fun <T> runExport(type: String, context: Context, fetcher: suspend () -> List<T>, exporter: (Context, List<T>) -> Uri) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val uri = exporter(context, fetcher())
                _exportState.value = ExportState.Done(uri, type)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportClients(context: Context) = runExport("Clients", context, { clientRepository.getAllClients().first() }, CsvExporter::exportClients)
    fun exportSessions(context: Context) = runExport("Sessions", context, { sessionRepository.getAllSessions().first() }, CsvExporter::exportSessions)
    fun exportTransactions(context: Context) = runExport("Transactions", context, { transactionRepository.getAllTransactions().first() }, CsvExporter::exportTransactions)

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}
