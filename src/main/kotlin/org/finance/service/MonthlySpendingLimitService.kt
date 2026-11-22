package org.finance.service

import org.finance.exception.ClientNotFoundException
import org.finance.exception.InvalidTransactionFilterException
import org.finance.model.MonthlySpendingLimitRequest
import org.finance.persistence.entity.MonthlySpendingLimitEntity
import org.finance.persistence.repository.ClientRepository
import org.finance.persistence.repository.MonthlySpendingLimitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class MonthlySpendingLimitService(
    private val clientRepository: ClientRepository,
    private val limitRepository: MonthlySpendingLimitRepository,
) {
    @Transactional
    fun setLimit(
        clientId: Long,
        request: MonthlySpendingLimitRequest,
        authSubject: String,
    ): MonthlySpendingLimitRequest {
        val client = clientRepository.findByIdAndAuthSubject(clientId, authSubject)
            ?: throw ClientNotFoundException(clientId)
        val category = request.category.trim().uppercase()
        if (category.isEmpty()) {
            throw InvalidTransactionFilterException("category must not be blank")
        }
        if (request.limitAmount < BigDecimal.ZERO) {
            throw InvalidTransactionFilterException("limitAmount must be non-negative")
        }

        val existing = limitRepository.findByClient_IdAndMonthAndCategory(
            clientId,
            request.month.toString(),
            category,
        )
        val limit = existing ?: MonthlySpendingLimitEntity(
            client = client,
            month = request.month.toString(),
            category = category,
            limitAmount = request.limitAmount,
        )
        limit.limitAmount = request.limitAmount
        limitRepository.save(limit)
        return MonthlySpendingLimitRequest(request.month, category, request.limitAmount)
    }
}
