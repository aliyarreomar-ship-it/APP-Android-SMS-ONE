package com.example.data.parser

import com.example.domain.model.TransactionType

class GenericSomaliMobileMoneyParser : TransactionParser {
    override fun canParse(sender: String, body: String): Boolean {
        val b = body.lowercase()
        val hasFinancialKeywords = b.contains("waa laguu soo diray") ||
                b.contains("waad u dirtay") ||
                b.contains("waxaad u dirtay") ||
                b.contains("waad u wareejisay") ||
                b.contains("waxaad wareejisay") ||
                b.contains("waad heshay") ||
                b.contains("waxaad heshay") ||
                b.contains("ka heshay") ||
                b.contains("kugu shubtay") ||
                b.contains("ku shubatay") ||
                b.contains("bixisay") ||
                b.contains("haraaga") ||
                b.contains("balance")
        val hasAmount = b.contains("$") || b.contains("usd")
        return hasFinancialKeywords && hasAmount
    }

    override fun parse(sender: String, body: String, timestamp: Long): ParseResult {
        var type: TransactionType? = null
        var amount: Double? = null
        var senderNum: String? = null
        var receiverNum: String? = null
        var balance: Double? = null
        var refId: String? = null
        var confidence = 30
        var isFinancial = true

        val amountRegex = """(?:\$|USD)\s*([0-9]+(?:[.,][0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE)
        val bracketAmountRegex = """\[[-+]?\$?\s*([0-9]+(?:[.,][0-9]{1,2})?)\s*\]""".toRegex()
        val numRegex = """(?:\b|[^0-9])(6[1-9]\d{7}|2526[1-9]\d{7})\b""".toRegex()
        val refRegex = """(?:Ref|TxId|Tarjumaha|ID|Tar)[\s:]*([A-Za-z0-9]+)""".toRegex(RegexOption.IGNORE_CASE)
        val haraagaRegex = """(?:Haraaga|Haraagaagu|Haraagaaga|Balance).*?(?:\$|USD)\s*([0-9]+(?:[.,][0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE)

        val bodyLower = body.lowercase()

        // Detect Provider
        var provider = when {
            bodyLower.contains("evc") || bodyLower.contains("hormuud") || sender.lowercase().contains("evc") || sender.lowercase().contains("hormuud") -> "EVC Plus"
            bodyLower.contains("dahab") || bodyLower.contains("somtel") || sender.lowercase().contains("dahab") || sender.lowercase().contains("somtel") -> "E-Dahab"
            bodyLower.contains("zaad") || bodyLower.contains("telesom") || sender.lowercase().contains("zaad") || sender.lowercase().contains("telesom") -> "ZAAD"
            bodyLower.contains("sahal") || bodyLower.contains("golis") || sender.lowercase().contains("sahal") || sender.lowercase().contains("golis") -> "SAHAL"
            bodyLower.contains("jeeb") || bodyLower.contains("premier") || sender.lowercase().contains("jeeb") -> "Jeeb"
            bodyLower.contains("61") -> "EVC Plus"
            bodyLower.contains("62") || bodyLower.contains("65") -> "E-Dahab"
            bodyLower.contains("63") -> "ZAAD"
            bodyLower.contains("68") || bodyLower.contains("69") -> "SAHAL"
            else -> "Mobile Money"
        }

        // Determine Type
        if (bodyLower.contains("waa laguu soo diray") || bodyLower.contains("laguu soo diray") || bodyLower.contains("waad heshay") || bodyLower.contains("waxaad heshay") || bodyLower.contains("ka heshay") || bodyLower.startsWith("[+$")) {
            type = TransactionType.INCOME
            confidence += 30
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
            numRegex.find(body)?.let { senderNum = it.groupValues[1]; confidence += 10 }
        } else if (bodyLower.contains("waad u dirtay") || bodyLower.contains("waxaad u dirtay") || bodyLower.contains("waad u wareejisay") || bodyLower.contains("waxaad wareejisay") || bodyLower.contains("waad dirtay") || bodyLower.startsWith("[-$")) {
            type = TransactionType.EXPENSE
            confidence += 30
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
            numRegex.find(body)?.let { receiverNum = it.groupValues[1]; confidence += 10 }
        } else if (bodyLower.contains("kugu shubtay") || bodyLower.contains("ku shubatay") || bodyLower.contains("airtime")) {
            type = TransactionType.AIRTIME
            confidence += 30
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
        } else if (bodyLower.contains("bixisay") || bodyLower.contains("ku bixisay")) {
            type = TransactionType.BILL_PAYMENT
            confidence += 30
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
        } else {
            type = TransactionType.EXPENSE
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 15 }
        }

        refRegex.find(body)?.let { refId = it.groupValues[1]; confidence += 10 }
        haraagaRegex.find(body)?.let { balance = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 10 }

        return ParseResult(
            isFinancial = isFinancial && amount != null,
            provider = provider,
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
