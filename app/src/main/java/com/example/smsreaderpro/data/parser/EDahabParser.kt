package com.example.smsreaderpro.data.parser

import com.example.smsreaderpro.data.model.ParseResult
import com.example.smsreaderpro.data.model.TransactionType
import java.util.Locale

class EDahabParser : TransactionParser {
    private val validSenders = listOf("EDAHAB", "898", "Somtel", "330", "e-dahab")

    override fun canParse(sender: String, body: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        val isSenderMatch = validSenders.any { s.contains(it.lowercase(Locale.ROOT)) || it.lowercase(Locale.ROOT).contains(s) }
        val hasKeywords = body.contains("edahab", ignoreCase = true) ||
                body.contains("e-dahab", ignoreCase = true) ||
                body.contains("tarjumaha:", ignoreCase = true) ||
                (body.contains("waxaad heshay", ignoreCase = true) && body.contains("somtel", ignoreCase = true))
        return isSenderMatch || hasKeywords
    }

    override fun parse(sender: String, body: String): ParseResult? {
        return try {
            val lower = body.lowercase(Locale.ROOT)
            val type = when {
                lower.contains("waxaad heshay") || lower.contains("laguu soo diray") -> TransactionType.INCOME
                lower.contains("ku shubatay") || lower.contains("airtime") -> TransactionType.AIRTIME
                lower.contains("ku bixisay") || lower.contains("bixisay") -> TransactionType.BILL_PAYMENT
                else -> TransactionType.EXPENSE
            }

            val amountRegex = Regex("""\$\s*([0-9.,]+)""")
            val amountMatch = amountRegex.find(body) ?: Regex("""([0-9.,]+)\s*USD""", RegexOption.IGNORE_CASE).find(body)
            val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) return null

            var counterpartyName: String? = null
            var counterpartyPhone: String? = null

            if (type == TransactionType.INCOME) {
                val partyMatch = Regex("""ka\s+(?:heshay\s+)?([^(\n]+?)\s*(?:\(([^)]+)\))?""", RegexOption.IGNORE_CASE).find(body)
                if (partyMatch != null) {
                    counterpartyName = partyMatch.groupValues[1].trim()
                    if (partyMatch.groupValues.size > 2) {
                        counterpartyPhone = partyMatch.groupValues[2].trim().ifEmpty { null }
                    }
                }
            } else {
                val partyMatch = Regex("""u\s+(?:dirtay\s+|wareejisay\s+)?([^(\n]+?)\s*(?:\(([^)]+)\))?""", RegexOption.IGNORE_CASE).find(body)
                if (partyMatch != null) {
                    counterpartyName = partyMatch.groupValues[1].trim()
                    if (partyMatch.groupValues.size > 2) {
                        counterpartyPhone = partyMatch.groupValues[2].trim().ifEmpty { null }
                    }
                }
            }

            var balance: Double? = null
            val balMatch = Regex("""Haraaga(?:agu)?(?:\s+waa)?[:\s]*\$?\s*([0-9.,]+)""", RegexOption.IGNORE_CASE).find(body)
            if (balMatch != null) {
                balance = balMatch.groupValues[1].replace(",", "").toDoubleOrNull()
            }

            var reference: String? = null
            val refMatch = Regex("""(?:Tarjumaha|Ref)[:\s]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE).find(body)
            if (refMatch != null) {
                reference = refMatch.groupValues[1].trim()
            }

            val counterparty = when {
                counterpartyName != null && counterpartyPhone != null -> "$counterpartyName ($counterpartyPhone)"
                counterpartyName != null -> counterpartyName
                else -> counterpartyPhone
            }

            var category = when (type) {
                TransactionType.INCOME -> "Dakhli"
                TransactionType.AIRTIME -> "Ku hadal Taleefan"
                else -> "Other"
            }

            counterpartyName?.lowercase(Locale.ROOT)?.let { lowerName ->
                when {
                    lowerName.contains("bajaaj") || lowerName.contains("taxi") -> category = "Bajaaj"
                    lowerName.contains("cunto") || lowerName.contains("restaurant") -> category = "Cunto"
                    lowerName.contains("dukaan") || lowerName.contains("supermarket") -> category = "Dukaan"
                    lowerName.contains("koronto") -> category = "Koronto"
                    lowerName.contains("biyo") -> category = "Biyo"
                    lowerName.contains("kiro") -> category = "Kiro"
                    lowerName.contains("internet") -> category = "Internet"
                    lowerName.contains("shaah") -> category = "Shaah"
                }
            }

            ParseResult(
                amount = amount,
                type = type,
                sender = if (type == TransactionType.INCOME) counterparty else null,
                receiver = if (type != TransactionType.INCOME) counterparty else null,
                balance = balance,
                reference = reference,
                provider = "EDAHAB",
                category = category,
                confidenceScore = 0.95f,
                timestamp = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }
}
