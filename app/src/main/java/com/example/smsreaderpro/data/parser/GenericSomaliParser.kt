package com.example.smsreaderpro.data.parser

import com.example.smsreaderpro.data.model.ParseResult
import com.example.smsreaderpro.data.model.TransactionType
import java.util.Locale

class GenericSomaliParser : TransactionParser {
    override fun canParse(sender: String, body: String): Boolean {
        val s = sender.lowercase(Locale.ROOT)
        val b = body.lowercase(Locale.ROOT)
        return s.contains("zaad") || s.contains("telesom") || s.contains("sahal") || s.contains("golis") ||
                b.contains("zaad") || b.contains("sahal") || b.contains("haraaga") ||
                b.contains("waxaad heshay") || b.contains("waxaad dirtay") || b.contains("u dirtay") || b.contains("ka heshay")
    }

    override fun parse(sender: String, body: String): ParseResult? {
        return try {
            val s = sender.lowercase(Locale.ROOT)
            val b = body.lowercase(Locale.ROOT)

            val provider = when {
                s.contains("zaad") || b.contains("zaad") -> "ZAAD"
                s.contains("sahal") || b.contains("sahal") -> "SAHAL"
                s.contains("evc") || b.contains("evc") -> "EVCPlus"
                s.contains("dahab") || b.contains("dahab") -> "EDAHAB"
                else -> "MobileMoney"
            }

            val type = when {
                b.contains("waxaad heshay") || b.contains("laguu soo diray") || b.contains("received") -> TransactionType.INCOME
                b.contains("ku shubatay") || b.contains("airtime") -> TransactionType.AIRTIME
                b.contains("bixisay") -> TransactionType.BILL_PAYMENT
                else -> TransactionType.EXPENSE
            }

            val amountRegex = Regex("""\$\s*([0-9.,]+)""")
            val amountMatch = amountRegex.find(body) ?: Regex("""([0-9.,]+)\s*(?:USD|SLSH|\$)""", RegexOption.IGNORE_CASE).find(body)
            val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) return null

            val balMatch = Regex("""Haraaga(?:agu)?(?:\s+waa)?[:\s]*\$?\s*([0-9.,]+)""", RegexOption.IGNORE_CASE).find(body)
            val balance = balMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

            val refMatch = Regex("""(?:Ref|Tarjumaha|TxId)[:\s]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE).find(body)
            val reference = refMatch?.groupValues?.get(1)?.trim()

            val partyMatch = Regex("""(?:ka|u|from|to)\s+([^.\n,]+)""", RegexOption.IGNORE_CASE).find(body)
            val counterparty = partyMatch?.groupValues?.get(1)?.trim()

            ParseResult(
                amount = amount,
                type = type,
                sender = if (type == TransactionType.INCOME) counterparty else null,
                receiver = if (type != TransactionType.INCOME) counterparty else null,
                balance = balance,
                reference = reference,
                provider = provider,
                category = if (type == TransactionType.INCOME) "Dakhli" else "Other",
                confidenceScore = 0.8f,
                timestamp = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }
}
