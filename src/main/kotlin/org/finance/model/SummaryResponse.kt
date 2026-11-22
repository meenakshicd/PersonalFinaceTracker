package org.finance.model

import java.math.BigDecimal

data class SummaryResponse(
    val accountCount: Int,
    val transactionCount: Int,
    val totalBalance: BigDecimal,
    val currency: String,
)
