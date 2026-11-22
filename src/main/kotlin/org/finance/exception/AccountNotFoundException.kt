package org.finance.exception

class AccountNotFoundException(accountId: String) :
    RuntimeException("Account $accountId was not found")
