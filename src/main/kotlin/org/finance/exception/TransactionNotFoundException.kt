package org.finance.exception

class TransactionNotFoundException(id: Long) :
    RuntimeException("Transaction $id was not found")
