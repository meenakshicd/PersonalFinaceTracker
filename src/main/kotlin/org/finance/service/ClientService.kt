package org.finance.service

import org.finance.exception.AccountNotFoundException
import org.finance.exception.ClientAlreadyExistsException
import org.finance.exception.ClientNotFoundException
import org.finance.exception.InvalidClientIdentityException
import org.finance.model.AccountResponse
import org.finance.model.ClientIdentityRequest
import org.finance.model.ClientProfileRequest
import org.finance.model.ClientProfileResponse
import org.finance.persistence.entity.AccountEntity
import org.finance.persistence.entity.ClientEntity
import org.finance.persistence.repository.AccountRepository
import org.finance.persistence.repository.ClientRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val accountRepository: AccountRepository,
) {
    @Transactional
    fun createClient(authSubject: String, request: ClientProfileRequest): ClientProfileResponse {
        if (clientRepository.findByAuthSubject(authSubject) != null) {
            throw ClientAlreadyExistsException()
        }
        val client = clientRepository.save(
            ClientEntity(
                name = request.name.trim(),
                phoneNumber = request.phoneNumber.trim(),
                email = request.email.trim(),
                authSubject = authSubject,
            ),
        )
        return client.toResponse(emptyList())
    }

    @Transactional(readOnly = true)
    fun getClient(clientId: Long, authSubject: String): ClientProfileResponse {
        val client = clientRepository.findByIdAndAuthSubject(clientId, authSubject)
            ?: throw ClientNotFoundException(clientId)
        return client.toResponse(accountRepository.findAllByClient_Id(clientId).map { it.toResponse() })
    }

    @Transactional
    fun assignAccount(clientId: Long, accountId: String): ClientProfileResponse {
        val client = clientRepository.findById(clientId)
            .orElseThrow { ClientNotFoundException(clientId) }
        val account = accountRepository.findById(accountId)
            .orElseThrow { AccountNotFoundException(accountId) }
        account.client = client
        accountRepository.save(account)
        return client.toResponse(accountRepository.findAllByClient_Id(clientId).map { it.toResponse() })
    }

    @Transactional
    fun linkIdentity(clientId: Long, request: ClientIdentityRequest): ClientProfileResponse {
        val client = clientRepository.findById(clientId)
            .orElseThrow { ClientNotFoundException(clientId) }
        val subject = request.authSubject.trim()
        if (subject.isEmpty()) {
            throw InvalidClientIdentityException()
        }
        if (clientRepository.findByAuthSubject(subject)?.id?.let { it != clientId } == true) {
            throw ClientAlreadyExistsException()
        }
        client.authSubject = subject
        clientRepository.save(client)
        return client.toResponse(accountRepository.findAllByClient_Id(clientId).map { it.toResponse() })
    }

    private fun ClientEntity.toResponse(accounts: List<AccountResponse>) =
        ClientProfileResponse(
            id = id ?: error("Persisted client is missing its ID"),
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            accounts = accounts,
        )

    private fun AccountEntity.toResponse() =
        AccountResponse(id, name, balance, currency)
}
