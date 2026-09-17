package com.example

import com.example.data.parser.SmsParserEngine
import com.example.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest {
    private val engine = SmsParserEngine()

    @Test
    fun testEvcPlusExpense() {
        val sms = "[-$12.50] Waad u dirtay ALI YARRE OMAR(615123456). Haraagaagu waa $120.00. Ref:7X8B9Q2. Taariikh: 16/09/26 14:32"
        val result = engine.parse("Hormuud", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals("EVC Plus", result.provider)
        assertEquals(TransactionType.EXPENSE, result.transactionType)
        assertEquals(12.50, result.amount!!, 0.001)
        assertEquals(120.00, result.balance!!, 0.001)
        assertEquals("7X8B9Q2", result.referenceId)
    }

    @Test
    fun testEvcPlusIncome() {
        val sms = "[+$50.00] Waa laguu soo diray. Waxaana soo diray MOHAMED AHMED(612987654). Haraagaagu waa $170.00. Ref:A9B8C7D. Taariikh: 16/09/26 15:45"
        val result = engine.parse("EVCPlus", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals("EVC Plus", result.provider)
        assertEquals(TransactionType.INCOME, result.transactionType)
        assertEquals(50.00, result.amount!!, 0.001)
        assertEquals(170.00, result.balance!!, 0.001)
        assertEquals("A9B8C7D", result.referenceId)
    }

    @Test
    fun testEDahabExpense() {
        val sms = "Waxaad u dirtay $25.00 625123456. Haraagaagu waa $75.00. Tarjumaha:ED998811"
        val result = engine.parse("Somtel", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals("E-Dahab", result.provider)
        assertEquals(TransactionType.EXPENSE, result.transactionType)
        assertEquals(25.00, result.amount!!, 0.001)
        assertEquals(75.00, result.balance!!, 0.001)
        assertEquals("ED998811", result.referenceId)
    }

    @Test
    fun testRealUserEvc192Message1() {
        val sms = "[-EVCPLUS-] waxaad $0.1 ka heshay 0613362057, Tar: 16/09/26 18:34:50 haraagagu waa $0.43. La soo deg App-ka WAAFI http://onelink.to/waafi"
        val result = engine.parse("192", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals("EVC Plus", result.provider)
        assertEquals(TransactionType.INCOME, result.transactionType)
        assertEquals(0.1, result.amount!!, 0.001)
        assertEquals(0.43, result.balance!!, 0.001)
        assertEquals("0613362057", result.senderNumber)
    }

    @Test
    fun testRealUserEvc192Message2() {
        val sms = "[-EVCPLUS-] waxaad $1 ka heshay 0613362057, Tar: 16/09/26 18:39:33 haraagagu waa $1.43. La soo deg App-ka WAAFI http://onelink.to/waafi"
        val result = engine.parse("192", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals("EVC Plus", result.provider)
        assertEquals(TransactionType.INCOME, result.transactionType)
        assertEquals(1.0, result.amount!!, 0.001)
        assertEquals(1.43, result.balance!!, 0.001)
        assertEquals("0613362057", result.senderNumber)
    }

    @Test
    fun testRealUserEDahab898Message() {
        val sms = "[-EDAHAB-] Waxaad $10 u dirtay 0625123456, haraagaagu waa $40. Tarjumaha:ED12345"
        val result = engine.parse("898", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals("E-Dahab", result.provider)
        assertEquals(TransactionType.EXPENSE, result.transactionType)
        assertEquals(10.0, result.amount!!, 0.001)
        assertEquals(40.0, result.balance!!, 0.001)
        assertEquals("0625123456", result.receiverNumber)
    }

    @Test
    fun testGenericSomaliIncome() {
        val sms = "Waxaad heshay $30.00 ka timid 619887766. Haraagaagu waa $110.00. Ref:TR99182"
        val result = engine.parse("+252619887766", sms, System.currentTimeMillis())

        assertTrue(result.isFinancial)
        assertEquals(TransactionType.INCOME, result.transactionType)
        assertEquals(30.00, result.amount!!, 0.001)
        assertEquals(110.00, result.balance!!, 0.001)
    }
}
