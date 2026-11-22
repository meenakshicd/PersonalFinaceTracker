package org.finance.service

import org.finance.exception.InvalidBankStatementException
import org.finance.model.TransactionType
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class StatementTransaction(
    val date: LocalDate,
    val description: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val category: String?,
)

@Component
class TransactionCategoryClassifier(private val properties: TransactionCategoryProperties) {
    fun classify(description: String): String? {
        val normalizedDescription = normalize(description)
        for ((category, keywords) in properties.keywords) {
            val matches = keywords.split(",").any { keyword ->
                normalize(keyword) == normalizedDescription
            }
            if (matches) return category.uppercase().replace('-', '_')
        }
        return null
    }

    private fun normalize(value: String) = value.lowercase().filter(Char::isLetterOrDigit)
}

@Component
class BankStatementCsvParser(private val categoryClassifier: TransactionCategoryClassifier) {
    fun parse(contents: ByteArray): List<StatementTransaction> {
        val rows = contents.decodeToString()
            .removePrefix("\uFEFF")
            .lineSequence()
            .filter { it.isNotBlank() }
            .toList()
        val headers = rows.firstOrNull()?.trim()?.split(",")?.map(String::trim)
            ?: throw InvalidBankStatementException("The statement file is empty")
        val optionalHeaders = headers.drop(3)
        if (
            headers.size !in 3..5 ||
            headers.take(3) != listOf("date", "description", "amount") ||
            optionalHeaders.any { it !in setOf("type", "category") } ||
            optionalHeaders.distinct().size != optionalHeaders.size
        ) {
            throw InvalidBankStatementException(
                "The statement must use date,description,amount with optional type and category columns",
            )
        }

        return rows.drop(1).mapIndexed { index, line ->
            val rowNumber = index + 2
            val columns = line.split(",", limit = Int.MAX_VALUE)
            if (columns.size != headers.size) {
                throw InvalidBankStatementException("Row $rowNumber must contain ${headers.size} columns")
            }

            val dateValue = columns[0].trim()
            val descriptionValue = columns[1].trim()
            val amountValue = columns[2].trim()
            if (descriptionValue.isEmpty() || descriptionValue.any(Char::isWhitespace)) {
                throw InvalidBankStatementException("Row $rowNumber description must be a single word")
            }

            val date = try {
                LocalDate.parse(dateValue)
            } catch (_: DateTimeParseException) {
                throw InvalidBankStatementException("Row $rowNumber has an invalid date: $dateValue")
            }
            val amount = try {
                BigDecimal(amountValue)
            } catch (_: NumberFormatException) {
                throw InvalidBankStatementException("Row $rowNumber has an invalid amount: $amountValue")
            }
            if (
                amount.precision() > 19 ||
                amount.stripTrailingZeros().scale() > 2 ||
                amount.abs() >= BigDecimal("100000000000000000")
            ) {
                throw InvalidBankStatementException("Row $rowNumber amount must fit NUMERIC(19, 2)")
            }

            val typeIndex = headers.indexOf("type")
            val type = columns.getOrNull(typeIndex)?.trim()?.takeIf(String::isNotEmpty)?.let { value ->
                try {
                    TransactionType.valueOf(value.uppercase())
                } catch (_: IllegalArgumentException) {
                    throw InvalidBankStatementException(
                        "Row $rowNumber type must be INCOME, EXPENSE, or TRANSFER",
                    )
                }
            } ?: if (amount > BigDecimal.ZERO) TransactionType.INCOME else TransactionType.EXPENSE
            val categoryIndex = headers.indexOf("category")
            val explicitCategory = columns.getOrNull(categoryIndex)?.trim()?.takeIf(String::isNotEmpty)
            if (explicitCategory != null && explicitCategory.length > 32) {
                throw InvalidBankStatementException("Row $rowNumber category must not exceed 32 characters")
            }

            StatementTransaction(
                date,
                descriptionValue,
                amount,
                type,
                explicitCategory?.uppercase() ?: categoryClassifier.classify(descriptionValue),
            )
        }
    }
}
