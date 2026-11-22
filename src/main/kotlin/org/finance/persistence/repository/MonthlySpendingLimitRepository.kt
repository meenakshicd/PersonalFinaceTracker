package org.finance.persistence.repository

import org.finance.persistence.entity.MonthlySpendingLimitEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MonthlySpendingLimitRepository : JpaRepository<MonthlySpendingLimitEntity, Long> {
    fun findByClient_IdAndMonthAndCategory(
        clientId: Long,
        month: String,
        category: String,
    ): MonthlySpendingLimitEntity?
}
