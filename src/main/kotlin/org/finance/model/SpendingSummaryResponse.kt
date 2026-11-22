package org.finance.model

import java.math.BigDecimal

data class SpendingSummaryResponse(
    val month: String,
    val totalSpend: BigDecimal,
    val byCategory: List<CategorySpending>,
)

data class CategorySpending(
    val category: String,
    val totalSpend: BigDecimal,
    val transactionCount: Long,
)
