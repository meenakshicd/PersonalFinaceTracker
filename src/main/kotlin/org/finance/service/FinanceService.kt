package org.finance.service

import org.finance.exception.AccountNotFoundException
import org.finance.exception.ClientNotFoundException
import org.finance.exception.InvalidTransactionFilterException
import org.finance.exception.TransactionNotFoundException
import org.finance.model.AccountResponse
import org.finance.model.MonthlyExpenseResponse
import org.finance.model.MonthlySavingsResponse
import org.finance.model.PageResponse
import org.finance.model.SpendingSummaryResponse
import org.finance.model.StatementImportResponse
import org.finance.model.SummaryResponse
import org.finance.model.TransactionResponse
import org.finance.model.TransactionType
import org.finance.model.UnusualActivity
import org.finance.model.UnusualActivitiesResponse
import org.finance.persistence.entity.AccountEntity
import org.finance.persistence.entity.ClientEntity
import org.finance.persistence.entity.TransactionEntity
import org.finance.persistence.repository.AccountRepository
import org.finance.persistence.repository.ClientRepository
import org.finance.persistence.repository.TransactionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Service
class FinanceService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val clientRepository: ClientRepository,
) {
    @Transactional(readOnly = true)
    fun getAccounts(authSubject: String): List<AccountResponse> {
        val client = requireClient(authSubject)
        return accountRepository.findAllByClient_Id(client.id!!)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getTransactions(
        type: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
        page: Int,
        size: Int,
        authSubject: String,
    ): PageResponse<TransactionResponse> {
        val client = requireClient(authSubject)
        val normalizedType = type?.uppercase()?.let {
            try {
                TransactionType.valueOf(it)
            } catch (_: IllegalArgumentException) {
                throw InvalidTransactionFilterException("type must be INCOME, EXPENSE, or TRANSFER")
            }
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw InvalidTransactionFilterException("startDate must be on or before endDate")
        }
        if (minAmount != null && minAmount < BigDecimal.ZERO ||
            maxAmount != null && maxAmount < BigDecimal.ZERO
        ) {
            throw InvalidTransactionFilterException("Amount filters must be non-negative")
        }
        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            throw InvalidTransactionFilterException("minAmount must be less than or equal to maxAmount")
        }
        if (page < 0 || size !in 1..100) {
            throw InvalidTransactionFilterException("page must be non-negative and size must be between 1 and 100")
        }

        val filters = Specification<TransactionEntity> { transaction, _, criteria ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()
            normalizedType?.let {
                predicates += criteria.equal(transaction.get<TransactionType>("type"), it)
            }
            predicates += criteria.equal(
                transaction.get<AccountEntity>("account")
                    .get<ClientEntity>("client")
                    .get<Long>("id"),
                client.id,
            )
            startDate?.let {
                predicates += criteria.greaterThanOrEqualTo(transaction.get("date"), it)
            }
            endDate?.let {
                predicates += criteria.lessThanOrEqualTo(transaction.get("date"), it)
            }
            minAmount?.let {
                predicates += criteria.greaterThanOrEqualTo(
                    criteria.abs(transaction.get("amount")),
                    it,
                )
            }
            maxAmount?.let {
                predicates += criteria.lessThanOrEqualTo(
                    criteria.abs(transaction.get("amount")),
                    it,
                )
            }
            criteria.and(*predicates.toTypedArray())
        }
        val result = transactionRepository.findAll(
            filters,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "id")),
        )
        return PageResponse(
            content = result.content.map { it.toResponse() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional(readOnly = true)
    fun getUnusualActivities(
        accountId: String,
        month: YearMonth,
        authSubject: String,
    ): UnusualActivitiesResponse {
        requireOwnedAccount(accountId, authSubject)

        val baselineStart = month.minusMonths(3).atDay(1)
        val currentStart = month.atDay(1)
        val transactions = transactionRepository
            .findAllByAccount_IdAndTypeAndDateBetween(
                accountId,
                TransactionType.EXPENSE,
                baselineStart,
                month.atEndOfMonth(),
            )
            .filter { it.category in MONITORED_CATEGORIES }

        val activities = MONITORED_CATEGORIES.mapNotNull { category ->
            val categorized = transactions.filter { it.category == category }
            val current = categorized.filter { it.date >= currentStart }
            val baseline = categorized.filter { it.date < currentStart }
            val baselineAmount = baseline.fold(BigDecimal.ZERO) { total, transaction ->
                total + transaction.amount.abs()
            }
            val averageAmount = baselineAmount.divide(BigDecimal(3), 2, RoundingMode.HALF_UP)
            val baselineCount = baseline.size
            val averageCount = baselineCount.toDouble() / 3
            val currentAmount = current.fold(BigDecimal.ZERO) { total, transaction ->
                total + transaction.amount.abs()
            }
            val currentCount = current.size
            val amountUnusual = currentAmount * BigDecimal(3) > baselineAmount * BigDecimal(2)
            val frequencyUnusual = currentCount * 3 > baselineCount * 2

            if (amountUnusual || frequencyUnusual) {
                UnusualActivity(
                    category = category,
                    amount = currentAmount,
                    transactionCount = currentCount,
                    averageMonthlyAmount = averageAmount,
                    averageMonthlyTransactionCount = averageCount,
                )
            } else {
                null
            }
        }

        return UnusualActivitiesResponse(month.toString(), activities)
    }

    @Transactional(readOnly = true)
    fun getMonthlyExpenses(
        clientId: Long,
        month: YearMonth,
        authSubject: String,
    ): MonthlyExpenseResponse {
        requireClient(clientId, authSubject)
        return MonthlyExpenseResponse(
            month.toString(),
            monthlyTotal(clientId, month, TransactionType.EXPENSE),
        )
    }

    @Transactional(readOnly = true)
    fun getMonthlySavings(
        clientId: Long,
        month: YearMonth,
        authSubject: String,
    ): MonthlySavingsResponse {
        requireClient(clientId, authSubject)
        val expenses = monthlyTotal(clientId, month, TransactionType.EXPENSE)
        val income = monthlyTotal(clientId, month, TransactionType.INCOME)
        return MonthlySavingsResponse(month.toString(), income - expenses)
    }

    @Transactional(readOnly = true)
    fun getTransaction(id: Long, authSubject: String): TransactionResponse {
        val client = requireClient(authSubject)
        val transaction = transactionRepository.findById(id)
            .orElseThrow { TransactionNotFoundException(id) }
        if (transaction.account.client?.id != client.id) {
            throw TransactionNotFoundException(id)
        }
        return transaction.toResponse()
    }

    @Transactional
    fun categorizeTransaction(id: Long, category: String, authSubject: String): TransactionResponse {
        val client = requireClient(authSubject)
        val transaction = transactionRepository.findById(id)
            .orElseThrow { TransactionNotFoundException(id) }
        if (transaction.account.client?.id != client.id) {
            throw TransactionNotFoundException(id)
        }
        val normalizedCategory = category.trim().uppercase()
        if (normalizedCategory.isEmpty() || normalizedCategory.length > 32) {
            throw InvalidTransactionFilterException("category must contain between 1 and 32 characters")
        }
        transaction.category = normalizedCategory
        return transactionRepository.save(transaction).toResponse()
    }

    @Transactional(readOnly = true)
    fun getSpendingSummary(
        clientId: Long,
        month: YearMonth,
        authSubject: String,
    ): SpendingSummaryResponse {
        requireClient(clientId, authSubject)
        val byCategory = transactionRepository.getSpendingByCategory(
            clientId,
            TransactionType.EXPENSE,
            month.atDay(1),
            month.atEndOfMonth(),
        )
        val totalSpend = byCategory.fold(BigDecimal.ZERO) { total, category ->
            total + category.totalSpend
        }
        return SpendingSummaryResponse(month.toString(), totalSpend, byCategory)
    }

    @Transactional(readOnly = true)
    fun getSummary(authSubject: String): SummaryResponse {
        val client = requireClient(authSubject)
        val accounts = accountRepository.findAllByClient_Id(client.id!!)
        return SummaryResponse(
            accountCount = accounts.size,
            transactionCount = transactionRepository.countByAccount_Client_Id(client.id!!).toInt(),
            totalBalance = accounts.fold(BigDecimal.ZERO) { total, account -> total + account.balance },
            currency = accounts.firstOrNull()?.currency ?: "USD",
        )
    }

    @Transactional
    fun importTransactions(
        accountId: String,
        transactions: List<StatementTransaction>,
        authSubject: String,
    ): StatementImportResponse {
        val account = requireOwnedAccount(accountId, authSubject)
        val imported = transactionRepository.saveAll(
            transactions.map { transaction ->
                TransactionEntity(
                    account = account,
                    description = transaction.description,
                    amount = transaction.amount,
                    date = transaction.date,
                    type = transaction.type,
                    category = transaction.category,
                )
            },
        )
        return StatementImportResponse(
            importedCount = imported.size,
            transactions = imported.map { it.toResponse() },
        )
    }

    private fun AccountEntity.toResponse() =
        AccountResponse(id, name, balance, currency)

    private fun TransactionEntity.toResponse() =
        TransactionResponse(
            id ?: error("Persisted transaction is missing its ID"),
            account.id,
            description,
            amount,
            date.toString(),
            type,
            category,
        )

    private fun requireClient(authSubject: String): ClientEntity =
        clientRepository.findByAuthSubject(authSubject)
            ?: throw ClientNotFoundException()

    private fun requireClient(clientId: Long, authSubject: String): ClientEntity =
        clientRepository.findByIdAndAuthSubject(clientId, authSubject)
            ?: throw ClientNotFoundException(clientId)

    @Transactional(readOnly = true)
    fun assertAccountOwnership(accountId: String, authSubject: String) {
        requireOwnedAccount(accountId, authSubject)
    }

    private fun requireOwnedAccount(accountId: String, authSubject: String): AccountEntity {
        val account = accountRepository.findById(accountId)
            .orElseThrow { AccountNotFoundException(accountId) }
        if (account.client?.authSubject != authSubject) {
            throw AccountNotFoundException(accountId)
        }
        return account
    }

    private fun monthlyTotal(clientId: Long, month: YearMonth, type: TransactionType): BigDecimal =
        transactionRepository.sumAbsoluteAmountByClientAndTypeAndDateBetween(
            clientId,
            type,
            month.atDay(1),
            month.atEndOfMonth(),
        ) ?: BigDecimal.ZERO

    companion object {
        private val MONITORED_CATEGORIES = listOf("NIGHTLIFE", "VIDEO_GAMES")
    }
}
