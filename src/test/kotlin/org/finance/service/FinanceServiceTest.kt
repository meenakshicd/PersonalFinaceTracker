package org.finance.service

import org.finance.model.TransactionType
import org.finance.persistence.entity.AccountEntity
import org.finance.persistence.entity.ClientEntity
import org.finance.persistence.entity.TransactionEntity
import org.finance.persistence.repository.AccountRepository
import org.finance.persistence.repository.ClientRepository
import org.finance.persistence.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Optional

class FinanceServiceTest {
    @Test
    fun `flags monitored categories when monthly spend exceeds twice the baseline`() {
        val accountRepository = mock(AccountRepository::class.java)
        val transactionRepository = mock(TransactionRepository::class.java)
        val clientRepository = mock(ClientRepository::class.java)
        val client = ClientEntity(
            id = 7,
            name = "Alex",
            phoneNumber = "123",
            email = "alex@example.com",
            authSubject = "subject-1",
        )
        val account = AccountEntity("acct-1", "Checking", BigDecimal.ZERO, "USD", client)
        `when`(clientRepository.findByAuthSubject("subject-1")).thenReturn(client)
        `when`(accountRepository.findById("acct-1")).thenReturn(Optional.of(account))
        `when`(
            transactionRepository.findAllByAccount_IdAndTypeAndDateBetween(
                "acct-1",
                TransactionType.EXPENSE,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-04-30"),
            ),
        ).thenReturn(
            listOf(
                expense(account, "2026-01-10", "VIDEO_GAMES", "-10.00"),
                expense(account, "2026-02-10", "VIDEO_GAMES", "-10.00"),
                expense(account, "2026-03-10", "VIDEO_GAMES", "-10.00"),
                expense(account, "2026-04-10", "VIDEO_GAMES", "-21.00"),
                expense(account, "2026-04-11", "TRANSIT", "-100.00"),
            ),
        )

        val result = FinanceService(accountRepository, transactionRepository, clientRepository)
            .getUnusualActivities("acct-1", YearMonth.of(2026, 4), "subject-1")

        assertEquals("2026-04", result.month)
        assertEquals(1, result.activities.size)
        assertEquals("VIDEO_GAMES", result.activities.single().category)
        assertEquals(BigDecimal("21.00"), result.activities.single().amount)
        assertEquals(1.0, result.activities.single().averageMonthlyTransactionCount)
    }

    @Test
    fun `returns monthly expense total and savings excluding transfers`() {
        val transactionRepository = mock(TransactionRepository::class.java)
        val clientRepository = mock(ClientRepository::class.java)
        val month = YearMonth.of(2026, 11)
        val client = ClientEntity(
            id = 7,
            name = "Alex",
            phoneNumber = "123",
            email = "alex@example.com",
            authSubject = "subject-1",
        )
        `when`(clientRepository.findByIdAndAuthSubject(7, "subject-1")).thenReturn(client)
        `when`(
            transactionRepository.sumAbsoluteAmountByClientAndTypeAndDateBetween(
                7,
                TransactionType.EXPENSE,
                month.atDay(1),
                month.atEndOfMonth(),
            ),
        ).thenReturn(BigDecimal("800.00"))
        `when`(
            transactionRepository.sumAbsoluteAmountByClientAndTypeAndDateBetween(
                7,
                TransactionType.INCOME,
                month.atDay(1),
                month.atEndOfMonth(),
            ),
        ).thenReturn(BigDecimal("3000.00"))

        val service = FinanceService(mock(AccountRepository::class.java), transactionRepository, clientRepository)

        assertEquals(BigDecimal("800.00"), service.getMonthlyExpenses(7, month, "subject-1").totalExpenses)
        assertEquals(BigDecimal("2200.00"), service.getMonthlySavings(7, month, "subject-1").totalSavings)
        verify(transactionRepository, times(2)).sumAbsoluteAmountByClientAndTypeAndDateBetween(
            7,
            TransactionType.EXPENSE,
            month.atDay(1),
            month.atEndOfMonth(),
        )
        verify(transactionRepository).sumAbsoluteAmountByClientAndTypeAndDateBetween(
            7,
            TransactionType.INCOME,
            month.atDay(1),
            month.atEndOfMonth(),
        )
    }

    @Test
    fun `does not allow a client to request another clients monthly totals`() {
        val clientRepository = mock(ClientRepository::class.java)
        val service = FinanceService(
            mock(AccountRepository::class.java),
            mock(TransactionRepository::class.java),
            clientRepository,
        )

        org.junit.jupiter.api.Assertions.assertThrows(
            org.finance.exception.ClientNotFoundException::class.java,
        ) {
            service.getMonthlyExpenses(99, YearMonth.of(2026, 11), "subject-1")
        }
    }

    @Test
    fun `does not allow a client to access another clients transaction`() {
        val clientRepository = mock(ClientRepository::class.java)
        val transactionRepository = mock(TransactionRepository::class.java)
        val currentClient = ClientEntity(
            id = 7,
            name = "Alex",
            phoneNumber = "123",
            email = "alex@example.com",
            authSubject = "subject-1",
        )
        val otherClient = ClientEntity(
            id = 8,
            name = "Sam",
            phoneNumber = "456",
            email = "sam@example.com",
            authSubject = "subject-2",
        )
        val otherAccount = AccountEntity(
            id = "other-account",
            name = "Other Checking",
            balance = BigDecimal.ZERO,
            currency = "USD",
            client = otherClient,
        )
        `when`(clientRepository.findByAuthSubject("subject-1")).thenReturn(currentClient)
        `when`(transactionRepository.findById(99)).thenReturn(
            Optional.of(
                TransactionEntity(
                    id = 99,
                    account = otherAccount,
                    description = "Private transaction",
                    amount = BigDecimal("-12.00"),
                    date = LocalDate.parse("2026-11-01"),
                    type = TransactionType.EXPENSE,
                ),
            ),
        )
        val service = FinanceService(
            mock(AccountRepository::class.java),
            transactionRepository,
            clientRepository,
        )

        org.junit.jupiter.api.Assertions.assertThrows(
            org.finance.exception.TransactionNotFoundException::class.java,
        ) {
            service.getTransaction(99, "subject-1")
        }
    }

    private fun expense(
        account: AccountEntity,
        date: String,
        category: String,
        amount: String,
    ) = TransactionEntity(
        account = account,
        description = "Purchase",
        amount = BigDecimal(amount),
        date = LocalDate.parse(date),
        type = TransactionType.EXPENSE,
        category = category,
    )

}
