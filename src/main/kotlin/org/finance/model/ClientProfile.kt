package org.finance.model

data class ClientProfileRequest(
    val name: String,
    val phoneNumber: String,
    val email: String,
)

data class ClientIdentityRequest(
    val authSubject: String,
)

data class ClientProfileResponse(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val accounts: List<AccountResponse>,
)
