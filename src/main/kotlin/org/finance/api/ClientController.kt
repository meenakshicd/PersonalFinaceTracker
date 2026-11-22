package org.finance.api

import org.finance.model.ClientProfileRequest
import org.finance.model.ClientProfileResponse
import org.finance.model.ClientIdentityRequest
import org.finance.service.ClientService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/clients")
class ClientController(
    private val clientService: ClientService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createClient(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: ClientProfileRequest,
    ): ClientProfileResponse =
        clientService.createClient(jwt.subject, request)

    @GetMapping("/{clientId}")
    fun getClient(
        @PathVariable clientId: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): ClientProfileResponse =
        clientService.getClient(clientId, jwt.subject)

    @PutMapping("/{clientId}/accounts/{accountId}")
    @PreAuthorize("hasAuthority('SCOPE_finance.admin')")
    fun assignAccountToClient(
        @PathVariable clientId: Long,
        @PathVariable accountId: String,
    ): ClientProfileResponse =
        clientService.assignAccount(clientId, accountId)

    @PutMapping("/{clientId}/identity")
    @PreAuthorize("hasAuthority('SCOPE_finance.admin')")
    fun linkIdentity(
        @PathVariable clientId: Long,
        @RequestBody request: ClientIdentityRequest,
    ): ClientProfileResponse =
        clientService.linkIdentity(clientId, request)
}
