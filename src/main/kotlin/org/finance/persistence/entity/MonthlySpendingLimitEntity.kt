package org.finance.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal

@Entity
@Table(
    name = "monthly_spending_limits",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_monthly_spending_limit",
            columnNames = ["client_id", "month", "category"],
        ),
    ],
)
class MonthlySpendingLimitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    var client: ClientEntity,

    @Column(nullable = false, length = 7)
    var month: String,

    @Column(nullable = false, length = 32)
    var category: String,

    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 2)
    var limitAmount: BigDecimal,
)
