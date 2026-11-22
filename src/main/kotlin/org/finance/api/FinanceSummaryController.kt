package org.finance.api

import org.finance.model.MonthlyExpenseResponse
import org.finance.model.MonthlySavingsResponse
import org.finance.model.SpendingSummaryResponse
import org.finance.model.SummaryResponse
import org.finance.service.FinanceService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth

@RestController
@RequestMapping("/api/v1")
class FinanceSummaryController(
    private val financeService: FinanceService,
) {
    @GetMapping("/summary")
    fun getSummary(@AuthenticationPrincipal jwt: Jwt): SummaryResponse = financeService.getSummary(jwt.subject)

    @GetMapping("/clients/{clientId}/monthly-expenses")
    fun getMonthlyExpenses(
        @PathVariable clientId: Long,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") month: YearMonth,
        @AuthenticationPrincipal jwt: Jwt,
    ): MonthlyExpenseResponse = financeService.getMonthlyExpenses(clientId, month, jwt.subject)

    @GetMapping("/clients/{clientId}/monthly-savings")
    fun getMonthlySavings(
        @PathVariable clientId: Long,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") month: YearMonth,
        @AuthenticationPrincipal jwt: Jwt,
    ): MonthlySavingsResponse = financeService.getMonthlySavings(clientId, month, jwt.subject)

    @GetMapping("/clients/{clientId}/spending-summary")
    fun getSpendingSummary(
        @PathVariable clientId: Long,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") month: YearMonth,
        @AuthenticationPrincipal jwt: Jwt,
    ): SpendingSummaryResponse = financeService.getSpendingSummary(clientId, month, jwt.subject)
}
