package org.finance.exception

class ClientNotFoundException(id: Long? = null) : RuntimeException(
    id?.let { "Client with ID $it was not found" } ?: "No client profile is linked to this identity",
)
