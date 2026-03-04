package com.dasurv.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.Session
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun exportClients(context: Context, clients: List<Client>): Uri {
        val csv = buildString {
            appendLine("ID,Name,Phone,Email,Notes,Created At")
            for (c in clients) {
                appendLine("${c.id},${escape(c.name)},${escape(c.phone)},${escape(c.email)},${escape(c.notes)},${dateFormat.format(Date(c.createdAt))}")
            }
        }
        return writeToCache(context, "clients_export.csv", csv)
    }

    fun exportSessions(context: Context, sessions: List<Session>): Uri {
        val csv = buildString {
            appendLine("ID,Client ID,Date,Procedure,Notes,Total Cost,Duration (s)")
            for (s in sessions) {
                appendLine("${s.id},${s.clientId},${dateFormat.format(Date(s.date))},${escape(s.procedure)},${escape(s.notes)},${s.totalCost},${s.durationSeconds}")
            }
        }
        return writeToCache(context, "sessions_export.csv", csv)
    }

    fun exportTransactions(context: Context, transactions: List<ClientTransaction>): Uri {
        val csv = buildString {
            appendLine("ID,Client ID,Session ID,Type,Amount,Payment Method,Date,Notes")
            for (t in transactions) {
                appendLine("${t.id},${t.clientId},${t.sessionId ?: ""},${t.type},${t.amount},${t.paymentMethod ?: ""},${dateFormat.format(Date(t.date))},${escape(t.notes)}")
            }
        }
        return writeToCache(context, "transactions_export.csv", csv)
    }

    private fun escape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun writeToCache(context: Context, fileName: String, content: String): Uri {
        val exportDir = File(context.cacheDir, "exports")
        exportDir.mkdirs()
        val file = File(exportDir, fileName)
        file.writeText(content)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
