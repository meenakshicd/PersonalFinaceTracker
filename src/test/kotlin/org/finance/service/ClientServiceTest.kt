package org.finance.service

import org.finance.persistence.entity.AccountEntity
import org.finance.persistence.entity.ClientEntity
import org.finance.persistence.repository.AccountRepository
import org.finance.persistence.repository.ClientRepository
import org.finance.model.ClientProfileRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.math.BigDecimal
import java.util.Optional

class ClientServiceTest {
    @Test
    fun `creates a client profile linked to the authenticated subject`() {
        val clientRepository = mock(ClientRepository::class.java)
        val accountRepository = mock(AccountRepository::class.java)
        val client = ClientEntity(
            id = 7,
            name = "Alex Doe",
            phoneNumber = "+1-555-0100",
            email = "alex@example.com",
            authSubject = "identity-subject-7",
        )
        `when`(clientRepository.findByAuthSubject("identity-subject-7")).thenReturn(null)
        `when`(clientRepository.save(org.mockito.ArgumentMatchers.any(ClientEntity::class.java)))
            .thenReturn(client)

        val result = ClientService(clientRepository, accountRepository).createClient(
            "identity-subject-7",
            ClientProfileRequest("Alex Doe", "+1-555-0100", "alex@example.com"),
        )

        assertEquals(7, result.id)
        assertEquals("Alex Doe", result.name)
        verify(clientRepository).save(
            org.mockito.ArgumentMatchers.argThat<ClientEntity> { saved ->
                saved.authSubject == "identity-subject-7"
            },
        )
    }

    @Test
    fun `assigns an account to a client and returns the mapped account`() {
        val clientRepository = mock(ClientRepository::class.java)
        val accountRepository = mock(AccountRepository::class.java)
        val client = ClientEntity(
            id = 7,
            name = "Alex Doe",
            phoneNumber = "+1-555-0100",
            email = "alex@example.com",
        )
        val account = AccountEntity(
            id = "checking-001",
            name = "Everyday Checking",
            balance = BigDecimal("100.00"),
            currency = "USD",
        )
        `when`(clientRepository.findById(7)).thenReturn(Optional.of(client))
        `when`(accountRepository.findById("checking-001")).thenReturn(Optional.of(account))
        `when`(accountRepository.findAllByClient_Id(7)).thenReturn(listOf(account))

        val result = ClientService(clientRepository, accountRepository)
            .assignAccount(7, "checking-001")

        assertEquals(7, result.id)
        assertEquals("Alex Doe", result.name)
        assertEquals(listOf("checking-001"), result.accounts.map { it.id })
        verify(accountRepository).save(account)
        assertEquals(client, account.client)
    }
}
