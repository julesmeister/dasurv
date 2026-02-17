package com.dasurv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    CHARGE, PAYMENT, DEPOSIT, TIP, REFUND
}

enum class PaymentMethod {
    CASH, CARD, E_TRANSFER, OTHER
}

@Entity(
    tableName = "client_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["sessionId"]),
        Index(value = ["date"])
    ]
)
data class ClientTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val sessionId: Long? = null,
    val type: TransactionType,
    val amount: Double,
    val paymentMethod: PaymentMethod? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
