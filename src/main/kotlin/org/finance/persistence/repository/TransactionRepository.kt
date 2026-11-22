package org.finance.persistence.repository

import org.finance.model.TransactionType
import org.finance.persistence.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDate

interface TransactionRepository :
    JpaRepository<TransactionEntity, Long>,
    JpaSpecificationExecutor<TransactionEntity> {
    @Query(
        """
        SELECT SUM(ABS(t.amount))
        FROM TransactionEntity t
        JOIN t.account a
        WHERE a.client.id = :clientId
          AND t.type = :type
          AND t.date BETWEEN :startDate AND :endDate
        """,
    )
    fun sumAbsoluteAmountByClientAndTypeAndDateBetween(
        @Param("clientId") clientId: Long,
        @Param("type") type: TransactionType,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): BigDecimal?

    fun findAllByAccount_IdAndTypeAndDateBetween(
        accountId: String,
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<TransactionEntity>

    fun countByAccount_Client_Id(clientId: Long): Long
}
