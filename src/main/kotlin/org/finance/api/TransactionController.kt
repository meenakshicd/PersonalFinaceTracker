package org.finance.api

import org.finance.model.PageResponse
import org.finance.model.TransactionCategoryRequest
import org.finance.model.TransactionResponse
import org.finance.service.FinanceService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(
    private val financeService: FinanceService,
) {
    @GetMapping
    fun getTransactions(
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) minAmount: BigDecimal?,
        @RequestParam(required = false) maxAmount: BigDecimal?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal jwt: Jwt,
    ): PageResponse<TransactionResponse> =
        financeService.getTransactions(type, startDate, endDate, minAmount, maxAmount, page, size, jwt.subject)

    @GetMapping("/{id}")
    fun getTransaction(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): TransactionResponse =
        financeService.getTransaction(id, jwt.subject)

    @PatchMapping("/{id}/category")
    fun categorizeTransaction(
        @PathVariable id: Long,
        @RequestBody request: TransactionCategoryRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): TransactionResponse =
        financeService.categorizeTransaction(id, request.category, jwt.subject)
}
