package com.example.data.parser

import com.example.domain.model.TransactionType

class JeebParser : TransactionParser {
    override fun canParse(sender: String, body: String): Boolean {
        return sender.equals("Jeeb", ignoreCase = true) ||
               sender.equals("898") ||
               body.contains("Jeeb", ignoreCase = true)
    }

    override fun parse(sender: String, body: String, timestamp: Long): ParseResult {
        var type: TransactionType? = null
        var amount: Double? = null
        var senderNum: String? = null
        var receiverNum: String? = null
        var balance: Double? = null
        var refId: String? = null
        var confidence = 0
        var isFinancial = false

        val amountRegex = """(?:\$|USD)\s*([0-9,.]+)""".toRegex(RegexOption.IGNORE_CASE)
        val numRegex = """(6[1-9]\d{7})""".toRegex()
        val refRegex = """Ref:\s*([A-Za-z0-9]+)""".toRegex(RegexOption.IGNORE_CASE)
        val haraagaRegex = """(?:Haraaga|Balance).*?(?:\$|USD)\s*([0-9,.]+)""".toRegex(RegexOption.IGNORE_CASE)

        val bodyLower = body.lowercase()

        // Income patterns for Jeeb
        if (bodyLower.contains("waa laguu soo diray") || bodyLower.contains("received")) {
            isFinancial = true
            type = TransactionType.INCOME
            confidence += 40
            amountRegex.find(body)?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
            numRegex.find(body)?.let { senderNum = it.groupValues[1]; confidence += 20 }
        } 
        // Expense patterns for Jeeb
        else if (bodyLower.contains("waad u dirtay") || bodyLower.contains("sent")) {
            isFinancial = true
            type = TransactionType.EXPENSE
            confidence += 40
            amountRegex.find(body)?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
            numRegex.find(body)?.let { receiverNum = it.groupValues[1]; confidence += 20 }
        } 
        // Bill payment / Merchant
        else if (bodyLower.contains("bixisay") || bodyLower.contains("paid")) {
            isFinancial = true
            type = TransactionType.BILL_PAYMENT
            confidence += 40
            amountRegex.find(body)?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
        }

        if (isFinancial) {
            refRegex.find(body)?.let { refId = it.groupValues[1]; confidence += 10 }
            haraagaRegex.find(body)?.let { balance = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 10 }
        }

        return ParseResult(
            isFinancial = isFinancial,
            provider = "Jeeb",
            transactionType = type,
            amount = amount,
            senderNumber = senderNum,
            receiverNumber = receiverNum,
            balance = balance,
            referenceId = refId,
            confidence = confidence.coerceAtMost(100)
        )
    }
}
