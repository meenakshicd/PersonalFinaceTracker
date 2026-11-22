package org.finance.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    @Column(length = 64, nullable = false)
    var id: String,

    @Column(nullable = false, length = 120)
    var name: String,

    @Column(nullable = false, precision = 19, scale = 2)
    var balance: BigDecimal,

    @Column(nullable = false, length = 3)
    var currency: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    var client: ClientEntity? = null,
)
