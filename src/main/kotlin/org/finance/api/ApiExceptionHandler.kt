package org.finance.api

import org.finance.exception.AccountNotFoundException
import org.finance.exception.ClientNotFoundException
import org.finance.exception.ClientAlreadyExistsException
import org.finance.exception.InvalidBankStatementException
import org.finance.exception.InvalidClientIdentityException
import org.finance.exception.InvalidTransactionFilterException
import org.finance.exception.TransactionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(AccountNotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    fun handleAccountNotFound(exception: AccountNotFoundException): ErrorResponse =
        ErrorResponse(exception.message ?: "Account was not found")

    @ExceptionHandler(ClientNotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    fun handleClientNotFound(exception: ClientNotFoundException): ErrorResponse =
        ErrorResponse(exception.message ?: "Client was not found")

    @ExceptionHandler(ClientAlreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun handleClientAlreadyExists(exception: ClientAlreadyExistsException): ErrorResponse =
        ErrorResponse(exception.message ?: "A client profile already exists for this identity")

    @ExceptionHandler(InvalidClientIdentityException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidClientIdentity(exception: InvalidClientIdentityException): ErrorResponse =
        ErrorResponse(exception.message ?: "Client identity is invalid")

    @ExceptionHandler(TransactionNotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    fun handleTransactionNotFound(exception: TransactionNotFoundException): ErrorResponse =
        ErrorResponse(exception.message ?: "Transaction was not found")

    @ExceptionHandler(InvalidBankStatementException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidBankStatement(exception: InvalidBankStatementException): ErrorResponse =
        ErrorResponse(exception.message ?: "The bank statement is invalid")

    @ExceptionHandler(InvalidTransactionFilterException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidTransactionFilter(exception: InvalidTransactionFilterException): ErrorResponse =
        ErrorResponse(exception.message ?: "Transaction filters are invalid")
}

data class ErrorResponse(val message: String)
