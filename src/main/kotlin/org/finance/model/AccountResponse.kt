package org.finance.model

import java.math.BigDecimal

data class AccountResponse(
    val id: String,
    val name: String,
    val balance: BigDecimal,
    val currency: String,
)
