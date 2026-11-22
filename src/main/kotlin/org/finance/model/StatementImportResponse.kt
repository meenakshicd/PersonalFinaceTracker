package org.finance.model

data class StatementImportResponse(
    val importedCount: Int,
    val transactions: List<TransactionResponse>,
)
