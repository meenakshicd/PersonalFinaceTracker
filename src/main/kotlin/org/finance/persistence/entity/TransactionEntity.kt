package org.finance.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.finance.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    var account: AccountEntity,

    @Column(nullable = false, length = 240)
    var description: String,

    @Column(nullable = false, precision = 19, scale = 2)
    var amount: BigDecimal,

    @Column(nullable = false)
    var date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 8)
    var type: TransactionType,

    @Column(length = 32)
    var category: String? = null,
)
