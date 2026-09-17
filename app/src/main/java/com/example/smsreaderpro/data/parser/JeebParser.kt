package com.example.smsreaderpro.data.parser

import com.example.smsreaderpro.data.model.ParseResult
import com.example.smsreaderpro.data.model.TransactionType
import java.util.Locale

class JeebParser : TransactionParser {
    private val validSenders = listOf("Jeeb", "PremierBank", "Premier", "JeebPay")

    override fun canParse(sender: String, body: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        return validSenders.any { s.contains(it.lowercase(Locale.ROOT)) } || body.contains("jeeb", ignoreCase = true)
    }

    override fun parse(sender: String, body: String): ParseResult? {
        return try {
            val lower = body.lowercase(Locale.ROOT)
            val type = if (lower.contains("received") || lower.contains("waxaad heshay") || lower.contains("credited")) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }

            val amountRegex = Regex("""\$\s*([0-9.,]+)""")
            val amountMatch = amountRegex.find(body) ?: Regex("""USD\s*([0-9.,]+)""", RegexOption.IGNORE_CASE).find(body)
            val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) return null

            val toFromMatch = Regex("""(?:from|to|ka|u)\s+([^.\n,]+)""", RegexOption.IGNORE_CASE).find(body)
            val counterparty = toFromMatch?.groupValues?.get(1)?.trim()

            val balMatch = Regex("""(?:balance|haraaga)[:\s]*\$?\s*([0-9.,]+)""", RegexOption.IGNORE_CASE).find(body)
            val balance = balMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

            val refMatch = Regex("""(?:TxId|Ref|Reference)[:\s]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE).find(body)
            val reference = refMatch?.groupValues?.get(1)?.trim()

            ParseResult(
                amount = amount,
                type = type,
                sender = if (type == TransactionType.INCOME) counterparty else null,
                receiver = if (type != TransactionType.INCOME) counterparty else null,
                balance = balance,
                reference = reference,
                provider = "Jeeb",
                category = if (type == TransactionType.INCOME) "Dakhli" else "Other",
                confidenceScore = 0.9f,
                timestamp = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }
}
