package org.finance.model

import java.math.BigDecimal

data class MonthlyExpenseResponse(
    val month: String,
    val totalExpenses: BigDecimal,
)

data class MonthlySavingsResponse(
    val month: String,
    val totalSavings: BigDecimal,
)
