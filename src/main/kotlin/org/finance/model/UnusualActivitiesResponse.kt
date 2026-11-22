package org.finance.model

import java.math.BigDecimal

data class UnusualActivitiesResponse(
    val month: String,
    val activities: List<UnusualActivity>,
)

data class UnusualActivity(
    val category: String,
    val amount: BigDecimal,
    val transactionCount: Int,
    val averageMonthlyAmount: BigDecimal,
    val averageMonthlyTransactionCount: Double,
)
