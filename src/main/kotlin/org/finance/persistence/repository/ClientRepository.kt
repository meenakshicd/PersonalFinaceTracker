package org.finance.persistence.repository

import org.finance.persistence.entity.ClientEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ClientRepository : JpaRepository<ClientEntity, Long> {
    fun findByAuthSubject(authSubject: String): ClientEntity?

    fun findByIdAndAuthSubject(id: Long, authSubject: String): ClientEntity?
}
