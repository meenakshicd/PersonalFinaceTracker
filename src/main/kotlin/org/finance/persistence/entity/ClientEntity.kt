package org.finance.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "clients")
class ClientEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 120)
    var name: String,

    @Column(name = "phone_number", nullable = false, length = 32)
    var phoneNumber: String,

    @Column(nullable = false, length = 254)
    var email: String,

    @Column(name = "auth_subject", length = 255, unique = true)
    var authSubject: String? = null,
)
