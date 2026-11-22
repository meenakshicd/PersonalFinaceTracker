package org.finance.api

import org.finance.model.AccountResponse
import org.finance.model.StatementImportResponse
import org.finance.model.UnusualActivitiesResponse
import org.finance.service.BankStatementCsvParser
import org.finance.service.FinanceService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.YearMonth

@RestController
@RequestMapping("/api/v1")
class AccountController(
    private val financeService: FinanceService,
    private val bankStatementCsvParser: BankStatementCsvParser,
) {
    @GetMapping("/accounts")
    fun getAccounts(@AuthenticationPrincipal jwt: Jwt): List<AccountResponse> =
        financeService.getAccounts(jwt.subject)

    @GetMapping("/accounts/{accountId}/unusual-activities")
    fun getUnusualActivities(
        @PathVariable accountId: String,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") month: YearMonth,
        @AuthenticationPrincipal jwt: Jwt,
    ): UnusualActivitiesResponse =
        financeService.getUnusualActivities(accountId, month, jwt.subject)

    @PostMapping("/accounts/{accountId}/transactions/import")
    @ResponseStatus(HttpStatus.CREATED)
    fun importTransactions(
        @PathVariable accountId: String,
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal jwt: Jwt,
    ): StatementImportResponse =
        financeService.importTransactions(accountId, bankStatementCsvParser.parse(file.bytes), jwt.subject)

    @PostMapping(
        "/accounts/{accountId}/transactions/import/pdf",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun importPdfStatement(
        @PathVariable accountId: String,
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ErrorResponse> {
        financeService.assertAccountOwnership(accountId, jwt.subject)
        // TODO: Parse the PDF statement and import its transactions.
        return ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ErrorResponse("PDF statement imports are not implemented yet"))
    }
}
