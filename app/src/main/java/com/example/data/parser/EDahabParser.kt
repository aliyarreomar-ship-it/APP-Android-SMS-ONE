package com.example.data.parser

import com.example.domain.model.TransactionType

class EDahabParser : TransactionParser {
    override fun canParse(sender: String, body: String): Boolean {
        val s = sender.trim().lowercase()
        val b = body.lowercase()
        return s == "898" ||
               s == "330" ||
               s.contains("dahab") || 
               s.contains("somtel") || 
               b.contains("edahab") || 
               b.contains("e-dahab") || 
               b.contains("[-edahab-]") ||
               b.contains("somtel") ||
               b.contains("tarjumaha:") ||
               ((b.contains("waad heshay") || b.contains("waxaad heshay") || b.contains("ka heshay") || b.contains("waad dirtay") || b.contains("u dirtay")) && (b.contains("65") || b.contains("62") || s.contains("65") || s.contains("62")))
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

        val amountRegex = """(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE)
        val bracketAmountRegex = """\[[-+]?\$?\s*([0-9]+(?:\.[0-9]{1,2})?)\s*\]""".toRegex()
        val phoneRegex = """(?:\b|[^0-9])((?:\+?252|0)?(?:6[2-9]|90)\d{7})\b""".toRegex()
        val directSenderRegex = """(?:ka heshay|soo diray|from)\s+([A-Za-z0-9_\s]*?\b0?(?:6[2-9]|90)\d{7}|\+?252[0-9]{8,10}|0[0-9]{8,10})""".toRegex(RegexOption.IGNORE_CASE)
        val directReceiverRegex = """(?:u dirtay|wareejisay|to)\s+([A-Za-z0-9_\s]*?\b0?(?:6[2-9]|90)\d{7}|\+?252[0-9]{8,10}|0[0-9]{8,10})""".toRegex(RegexOption.IGNORE_CASE)

        val refRegex = """(?:Tarjumaha|TxId|Ref|ID)[\s:]*([A-Za-z0-9]+)""".toRegex(RegexOption.IGNORE_CASE)
        val haraagaRegex = """(?:haraag[a-z]*|balance)[^0-9$]*?(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE)

        val bodyLower = body.lowercase()

        if (bodyLower.contains("waad heshay") || bodyLower.contains("waxaad heshay") || bodyLower.contains("ka heshay") || bodyLower.contains("laguu soo diray") || bodyLower.startsWith("[+$")) {
            isFinancial = true
            type = TransactionType.INCOME
            confidence += 40
            val waxaadAmountMatch = """waxaad\s+(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE).find(body)
            val match = waxaadAmountMatch ?: bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
            
            val directMatch = directSenderRegex.find(body)
            if (directMatch != null) {
                senderNum = directMatch.groupValues[1].trim()
                confidence += 20
            } else {
                phoneRegex.find(body)?.let { senderNum = it.groupValues[1].trim(); confidence += 20 }
            }
        } else if (bodyLower.contains("waad dirtay") || bodyLower.contains("waxaad u dirtay") || bodyLower.contains("u dirtay") || bodyLower.contains("wareejisay") || bodyLower.startsWith("[-$")) {
            isFinancial = true
            type = TransactionType.EXPENSE
            confidence += 40
            val waxaadAmountMatch = """waxaad\s+(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE).find(body)
            val match = waxaadAmountMatch ?: bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }

            val directMatch = directReceiverRegex.find(body)
            if (directMatch != null) {
                receiverNum = directMatch.groupValues[1].trim()
                confidence += 20
            } else {
                phoneRegex.find(body)?.let { receiverNum = it.groupValues[1].trim(); confidence += 20 }
            }
        } else if (bodyLower.contains("bixisay") || bodyLower.contains("ku bixisay")) {
            isFinancial = true
            type = TransactionType.BILL_PAYMENT
            confidence += 40
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let { amount = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 20 }
        }

        if (isFinancial) {
            refRegex.find(body)?.let { refId = it.groupValues[1]; confidence += 10 }
            haraagaRegex.find(body)?.let { balance = it.groupValues[1].replace(",", "").toDoubleOrNull(); confidence += 10 }
        }

        val suggestedCategory = when {
            type == TransactionType.AIRTIME || bodyLower.contains("kuuboon") || bodyLower.contains("airtime") || bodyLower.contains("kugu shubtay") -> "Airtime"
            bodyLower.contains("koronto") || bodyLower.contains("biyo") || bodyLower.contains("internet") || bodyLower.contains("wifi") || bodyLower.contains("biil") -> "Bills"
            bodyLower.contains("maqaayad") || bodyLower.contains("cunto") || bodyLower.contains("restaurant") || bodyLower.contains("hotel") || bodyLower.contains("cafe") -> "Food"
            bodyLower.contains("dukaan") || bodyLower.contains("supermarket") || bodyLower.contains("iibsatay") || bodyLower.contains("dawaaqo") || bodyLower.contains("bixisay") -> "Shopping"
            bodyLower.contains("mushaar") || bodyLower.contains("salary") -> "Salary"
            bodyLower.contains("hooyo") || bodyLower.contains("aabe") || bodyLower.contains("walaal") || bodyLower.contains("reer") -> "Family"
            type == TransactionType.INCOME -> "Income"
            else -> "Other"
        }

        return ParseResult(
            isFinancial = isFinancial,
            provider = "E-Dahab",
            transactionType = type,
            amount = amount,
            senderNumber = senderNum,
            receiverNumber = receiverNum,
            balance = balance,
            referenceId = refId,
            confidence = confidence.coerceAtMost(100),
            suggestedCategory = suggestedCategory
        )
    }
}
