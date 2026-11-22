package org.finance.persistence.repository

import org.finance.persistence.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, String> {
    fun findAllByClient_Id(clientId: Long): List<AccountEntity>
}
