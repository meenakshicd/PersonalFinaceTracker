package org.finance.service

import org.finance.exception.InvalidBankStatementException
import org.finance.model.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class BankStatementCsvParserTest {
    private val parser = BankStatementCsvParser(
        TransactionCategoryClassifier(
            TransactionCategoryProperties().apply {
                keywords = mapOf(
                    "nightlife" to "nightlife,bar,pub,club,nightclub,drinks",
                    "video-games" to "game,games,gaming,videogame,videogames,steam,playstation,xbox,nintendo",
                    "groceries" to "grocery,groceries,supermarket",
                    "transit" to "bus,mtr,train,subway,transit",
                )
            },
        ),
    )

    @Test
    fun `parses single word descriptions`() {
        val csv = "date,description,amount\n2026-01-03,Coffee,-12.50"

        assertEquals(
            listOf(
                StatementTransaction(
                    LocalDate.parse("2026-01-03"),
                    "Coffee",
                    BigDecimal("-12.50"),
                    TransactionType.EXPENSE,
                    null,
                ),
            ),
            parser.parse(csv.toByteArray()),
        )
    }

    @Test
    fun `rejects descriptions with multiple words`() {
        val csv = "date,description,amount\n2026-01-03,Grocery Store,-12.50"

        assertThrows(InvalidBankStatementException::class.java) {
            parser.parse(csv.toByteArray())
        }
    }

    @Test
    fun `rejects invalid rows`() {
        val csv = "date,description,amount\n2026-01-03,Coffee,100000000000000000"

        assertThrows(InvalidBankStatementException::class.java) {
            parser.parse(csv.toByteArray())
        }
    }

    @Test
    fun `parses sample bank statement`() {
        val sample = requireNotNull(javaClass.getResourceAsStream("/sample-bank-statement.csv"))
            .use { it.readBytes() }

        val transactions = parser.parse(sample)
        assertEquals(10, transactions.size)
        assertEquals(TransactionType.TRANSFER, transactions.single { it.description == "CardPayment" }.type)
        assertEquals("VIDEO_GAMES", transactions.single { it.description == "VideoGames" }.category)
        assertEquals("NIGHTLIFE", transactions.single { it.description == "Nightlife" }.category)
        assertEquals("GROCERIES", transactions.single { it.description == "Grocery" }.category)
        assertEquals("TRANSIT", transactions.single { it.description == "Bus" }.category)
        assertEquals("SELF_CARE", transactions.single { it.description == "Spa" }.category)
    }

    @Test
    fun `parses explicit transfer type without requiring category`() {
        val csv = "date,description,amount,type\n2026-01-03,CardPayment,-50.00,TRANSFER"

        assertEquals(
            TransactionType.TRANSFER,
            parser.parse(csv.toByteArray()).single().type,
        )
    }

    @Test
    fun `uses explicit category from csv instead of classifying description`() {
        val csv = "date,description,amount,type,category\n2026-01-03,Coffee,-12.50,EXPENSE,  dining out "

        assertEquals(
            "DINING OUT",
            parser.parse(csv.toByteArray()).single().category,
        )
    }

    @Test
    fun `classifies description when csv category is blank`() {
        val csv = "date,description,amount,category\n2026-01-03,Grocery,-12.50,"

        assertEquals(
            "GROCERIES",
            parser.parse(csv.toByteArray()).single().category,
        )
    }

    @Test
    fun `rejects category longer than database limit`() {
        val csv = "date,description,amount,category\n2026-01-03,Coffee,-12.50,${"a".repeat(33)}"

        assertThrows(InvalidBankStatementException::class.java) {
            parser.parse(csv.toByteArray())
        }
    }

    @Test
    fun `leaves unknown descriptions uncategorized`() {
        val csv = "date,description,amount\n2026-01-03,Spa,-12.50"

        assertEquals(null, parser.parse(csv.toByteArray()).single().category)
    }
}
