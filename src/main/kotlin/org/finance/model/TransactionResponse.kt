package org.finance.model

import java.math.BigDecimal

data class TransactionResponse(
    val id: Long,
    val accountId: String,
    val description: String,
    val amount: BigDecimal,
    val date: String,
    val type: TransactionType,
    val category: String?,
)
