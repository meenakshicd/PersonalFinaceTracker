package org.finance.model

import java.math.BigDecimal
import java.time.YearMonth

data class MonthlySpendingLimitRequest(
    val month: YearMonth,
    val category: String,
    val limitAmount: BigDecimal,
)
