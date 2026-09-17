package com.example.smsreaderpro.data.parser

import com.example.smsreaderpro.data.model.ParseResult
import com.example.smsreaderpro.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class EvcPlusParser : TransactionParser {
    private val validSenders = listOf("EVCPlus", "192", "Hormuud", "waafi", "770", "780", "199", "141")

    override fun canParse(sender: String, body: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        val isSenderMatch = validSenders.any { s.contains(it.lowercase(Locale.ROOT)) || it.lowercase(Locale.ROOT).contains(s) }
        val hasEvcKeywords = body.contains("EVCPlus", ignoreCase = true) ||
                body.contains("waa laguu soo diray", ignoreCase = true) ||
                body.contains("waad u dirtay", ignoreCase = true) ||
                body.contains("Haraagaagu waa", ignoreCase = true) ||
                body.contains("Haraaga:", ignoreCase = true) ||
                body.contains("[+$") ||
                body.contains("[-$")
        return isSenderMatch || hasEvcKeywords
    }

    override fun parse(sender: String, body: String): ParseResult? {
        return try {
            val lower = body.lowercase(Locale.ROOT)
            var type = when {
                body.contains("[+$") || lower.contains("waa laguu soo diray") || lower.contains("waxaad ka heshay") -> TransactionType.INCOME
                lower.contains("ku shubatay") || lower.contains("kugu shubtay") || lower.contains("airtime") -> TransactionType.AIRTIME
                lower.contains("bixisay") || lower.contains("ku bixisay") || lower.contains("merchant") -> TransactionType.BILL_PAYMENT
                else -> TransactionType.EXPENSE
            }

            var amount = 0.0
            val bracketRegex = Regex("""\[([+-]?)\$([0-9.,]+)\]""")
            val bracketMatch = bracketRegex.find(body)
            if (bracketMatch != null) {
                amount = bracketMatch.groupValues[2].replace(",", "").toDoubleOrNull() ?: 0.0
                if (bracketMatch.groupValues[1] == "+") type = TransactionType.INCOME
                if (bracketMatch.groupValues[1] == "-") type = TransactionType.EXPENSE
            } else {
                val dollarRegex = Regex("""\$([0-9.,]+)""")
                val dollarMatch = dollarRegex.find(body)
                if (dollarMatch != null) {
                    amount = dollarMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: 0.0
                }
            }

            if (amount <= 0.0) return null

            var counterpartyName: String? = null
            var counterpartyPhone: String? = null

            if (type == TransactionType.INCOME) {
                val senderRegex = Regex("""(?:Waxaana soo diray|ka heshay)\s*([^(\n]+?)\s*(?:\(([^)]+)\))?""", RegexOption.IGNORE_CASE)
                val senderMatch = senderRegex.find(body)
                if (senderMatch != null) {
                    counterpartyName = senderMatch.groupValues[1].trim()
                    if (senderMatch.groupValues.size > 2) {
                        counterpartyPhone = senderMatch.groupValues[2].trim().ifEmpty { null }
                    }
                }
            } else {
                val receiverRegex = Regex("""(?:Waad u dirtay|u dirtay|u wareejisay)\s*([^(\n]+?)\s*(?:\(([^)]+)\))?""", RegexOption.IGNORE_CASE)
                val receiverMatch = receiverRegex.find(body)
                if (receiverMatch != null) {
                    counterpartyName = receiverMatch.groupValues[1].trim()
                    if (receiverMatch.groupValues.size > 2) {
                        counterpartyPhone = receiverMatch.groupValues[2].trim().ifEmpty { null }
                    }
                }
            }

            var balance: Double? = null
            val balanceRegex = Regex("""Haraaga(?:agu)?(?:\s+waa)?[:\s]*\$([0-9.,]+)""", RegexOption.IGNORE_CASE)
            val balanceMatch = balanceRegex.find(body)
            if (balanceMatch != null) {
                balance = balanceMatch.groupValues[1].replace(",", "").toDoubleOrNull()
            }

            var reference: String? = null
            val refRegex = Regex("""Ref[:\s]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE)
            val refMatch = refRegex.find(body)
            if (refMatch != null) {
                reference = refMatch.groupValues[1].trim()
            }

            var timestamp = System.currentTimeMillis()
            val dateRegex = Regex("""Tar[:\s]*(\d{2}/\d{2}/\d{2,4}\s+\d{2}:\d{2}(?::\d{2})?)""", RegexOption.IGNORE_CASE)
            val dateMatch = dateRegex.find(body)
            if (dateMatch != null) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.ROOT)
                    val dateStr = dateMatch.groupValues[1].trim()
                    val parsed = sdf.parse(if (dateStr.count { it == ':' } == 1) "$dateStr:00" else dateStr)
                    if (parsed != null) timestamp = parsed.time
                } catch (_: Exception) {}
            }

            val counterparty = when {
                counterpartyName != null && counterpartyPhone != null -> "$counterpartyName ($counterpartyPhone)"
                counterpartyName != null -> counterpartyName
                else -> counterpartyPhone
            }

            var category = when (type) {
                TransactionType.INCOME -> "Dakhli"
                TransactionType.AIRTIME -> "Ku hadal Taleefan"
                TransactionType.BILL_PAYMENT -> "Biilasha"
                else -> "Other"
            }

            counterpartyName?.lowercase(Locale.ROOT)?.let { lowerName ->
                when {
                    lowerName.contains("bajaaj") || lowerName.contains("taxi") -> category = "Bajaaj"
                    lowerName.contains("cunto") || lowerName.contains("rest") || lowerName.contains("cafe") -> category = "Cunto"
                    lowerName.contains("dukaan") || lowerName.contains("supermarket") || lowerName.contains("mall") -> category = "Dukaan"
                    lowerName.contains("koronto") || lowerName.contains("electric") || lowerName.contains("beco") -> category = "Koronto"
                    lowerName.contains("biyo") || lowerName.contains("water") -> category = "Biyo"
                    lowerName.contains("kiro") || lowerName.contains("rent") -> category = "Kiro"
                    lowerName.contains("internet") || lowerName.contains("wifi") -> category = "Internet"
                    lowerName.contains("shaah") || lowerName.contains("tea") -> category = "Shaah"
                }
            }

            ParseResult(
                amount = amount,
                type = type,
                sender = if (type == TransactionType.INCOME) counterparty else null,
                receiver = if (type != TransactionType.INCOME) counterparty else null,
                balance = balance,
                reference = reference,
                provider = "EVCPlus",
                category = category,
                confidenceScore = 0.95f,
                timestamp = timestamp
            )
        } catch (_: Exception) {
            null
        }
    }
}
