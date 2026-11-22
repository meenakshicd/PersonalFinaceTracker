package org.finance.api

import org.finance.model.MonthlySpendingLimitRequest
import org.finance.service.MonthlySpendingLimitService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class MonthlySpendingLimitController(
    private val monthlySpendingLimitService: MonthlySpendingLimitService,
) {
    @PutMapping("/clients/{clientId}/monthly-spending-limits")
    fun setMonthlySpendingLimit(
        @PathVariable clientId: Long,
        @RequestBody request: MonthlySpendingLimitRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<MonthlySpendingLimitRequest> =
        ResponseEntity.ok(
            monthlySpendingLimitService.setLimit(clientId, request, jwt.subject),
        )
}
