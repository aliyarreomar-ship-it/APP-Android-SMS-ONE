package com.example.data.parser

import com.example.domain.model.TransactionType

class EvcPlusParser : TransactionParser {
    override fun canParse(sender: String, body: String): Boolean {
        val s = sender.trim().lowercase()
        val b = body.lowercase()

        // Exclude other explicit providers
        if (s == "898" || s.contains("somtel") || s.contains("dahab") || b.contains("edahab") || b.contains("e-dahab") || b.contains("[-edahab-]") || b.contains("tarjumaha:")) {
            return false
        }
        if (s.contains("jeeb") || s.contains("premier") || b.contains("jeeb")) {
            return false
        }
        if (s.contains("zaad") || s.contains("telesom") || b.contains("zaad")) {
            return false
        }

        return s == "192" ||
               s == "770" ||
               s == "780" ||
               s == "199" ||
               s == "141" ||
               s.contains("evc") ||
               s.contains("hormuud") ||
               s.contains("waafi") ||
               b.contains("evcplus") ||
               b.contains("[-evcplus-]") ||
               b.contains("evc plus") ||
               b.contains("hormuud") ||
               b.contains("waafi") ||
               b.startsWith("[-$") ||
               b.startsWith("[+$") ||
               b.contains("ka heshay") ||
               b.contains("waa laguu soo diray") ||
               b.contains("waad u dirtay") ||
               b.contains("waxaad u dirtay") ||
               b.contains("waad u wareejisay")
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

        // Amount matching: $0.1, $1, $1.43, $10.50, USD 20, [+$50], [-$12]
        val amountRegex = """(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE)
        val bracketAmountRegex = """\[[-+]?\$?\s*([0-9]+(?:\.[0-9]{1,2})?)\s*\]""".toRegex()
        
        // Somali Phone Number: 061xxxxxxx, 61xxxxxxx, +25261xxxxxxx, 068xxxxxxx
        val phoneRegex = """(?:\b|[^0-9])((?:\+?252|0)?(?:6[1-9]|90)\d{7})\b""".toRegex()
        val directSenderRegex = """(?:ka heshay|soo diray|from)\s+([A-Za-z0-9_\s]*?\b0?(?:6[1-9]|90)\d{7}|\+?252[0-9]{8,10}|0[0-9]{8,10})""".toRegex(RegexOption.IGNORE_CASE)
        val directReceiverRegex = """(?:u dirtay|wareejisay|to)\s+([A-Za-z0-9_\s]*?\b0?(?:6[1-9]|90)\d{7}|\+?252[0-9]{8,10}|0[0-9]{8,10})""".toRegex(RegexOption.IGNORE_CASE)

        val refRegex = """(?:Ref|TxId|ID|Tar[\s:]+no)[\s:]*([A-Za-z0-9]+)""".toRegex(RegexOption.IGNORE_CASE)
        val tarDateRegex = """Tar[\s:]*([0-9/]{6,8}\s+[0-9:]{5,8})""".toRegex(RegexOption.IGNORE_CASE)
        val haraagaRegex = """(?:haraag[a-z]*|balance)[^0-9$]*?(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE)

        val bodyLower = body.lowercase()

        // 1. Income (Money received, e.g. "[-EVCPLUS-] waxaad $0.1 ka heshay 0613362057")
        if (bodyLower.contains("ka heshay") ||
            bodyLower.contains("waad heshay") ||
            bodyLower.contains("waxaad heshay") ||
            bodyLower.contains("waa laguu soo diray") ||
            bodyLower.contains("laguu soo diray") ||
            bodyLower.contains("received") ||
            bodyLower.startsWith("[+$")
        ) {
            isFinancial = true
            type = TransactionType.INCOME
            confidence += 40

            // Extract amount: check right after "waxaad" first if possible, or general amount
            val waxaadAmountMatch = """waxaad\s+(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE).find(body)
            val match = waxaadAmountMatch ?: bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let {
                amount = it.groupValues[1].replace(",", "").toDoubleOrNull()
                confidence += 20
            }

            val directMatch = directSenderRegex.find(body)
            if (directMatch != null) {
                senderNum = directMatch.groupValues[1].trim()
                confidence += 20
            } else {
                phoneRegex.find(body)?.let {
                    senderNum = it.groupValues[1].trim()
                    confidence += 20
                }
            }
        }
        // 2. Expense / Transfer (Money sent, e.g. "[-EVCPLUS-] waxaad $5 u dirtay 0615123456")
        else if (bodyLower.contains("u dirtay") ||
                 bodyLower.contains("waad u dirtay") ||
                 bodyLower.contains("waxaad u dirtay") ||
                 bodyLower.contains("waad dirtay") ||
                 bodyLower.contains("waxaad dirtay") ||
                 bodyLower.contains("u wareejisay") ||
                 bodyLower.contains("wareejisay") ||
                 bodyLower.contains("sent") ||
                 bodyLower.startsWith("[-$")
        ) {
            isFinancial = true
            type = TransactionType.EXPENSE
            confidence += 40

            val waxaadAmountMatch = """waxaad\s+(?:\$|USD)\s*([0-9]+(?:\.[0-9]{1,2})?)""".toRegex(RegexOption.IGNORE_CASE).find(body)
            val match = waxaadAmountMatch ?: bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let {
                amount = it.groupValues[1].replace(",", "").toDoubleOrNull()
                confidence += 20
            }

            val directMatch = directReceiverRegex.find(body)
            if (directMatch != null) {
                receiverNum = directMatch.groupValues[1].trim()
                confidence += 20
            } else {
                phoneRegex.find(body)?.let {
                    receiverNum = it.groupValues[1].trim()
                    confidence += 20
                }
            }
        }
        // 3. Airtime (e.g. kugu shubtay, airtime)
        else if (bodyLower.contains("kugu shubtay") || bodyLower.contains("ugu shubtay") || bodyLower.contains("ku shubatay") || bodyLower.contains("airtime")) {
            isFinancial = true
            type = TransactionType.AIRTIME
            confidence += 40
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let {
                amount = it.groupValues[1].replace(",", "").toDoubleOrNull()
                confidence += 20
            }
        }
        // 4. Bills / Merchant
        else if (bodyLower.contains("bixisay") || bodyLower.contains("ku bixisay") || bodyLower.contains("paid") || bodyLower.contains("merchant")) {
            isFinancial = true
            type = TransactionType.BILL_PAYMENT
            confidence += 40
            val match = bracketAmountRegex.find(body) ?: amountRegex.find(body)
            match?.let {
                amount = it.groupValues[1].replace(",", "").toDoubleOrNull()
                confidence += 20
            }
        }

        if (isFinancial) {
            refRegex.find(body)?.let {
                refId = it.groupValues[1]
                confidence += 10
            } ?: run {
                // If Ref: not found, check Tar: date/time like Tar: 16/09/26 18:34:50
                tarDateRegex.find(body)?.let {
                    refId = "EVC_" + it.groupValues[1].replace(Regex("[^0-9]"), "")
                    confidence += 10
                }
            }

            haraagaRegex.find(body)?.let {
                balance = it.groupValues[1].replace(",", "").toDoubleOrNull()
                confidence += 10
            }
        }

        // Smart Category Detection
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

        // Parse extracted timestamp if Tar: dd/MM/yy HH:mm:ss format is available
        var extractedTime: Long? = null
        try {
            tarDateRegex.find(body)?.let {
                val rawDateStr = it.groupValues[1].trim()
                val sdf = java.text.SimpleDateFormat("dd/MM/yy HH:mm:ss", java.util.Locale.US)
                val parsed = sdf.parse(rawDateStr)
                if (parsed != null) {
                    extractedTime = parsed.time
                }
            }
        } catch (_: Exception) {}

        confidence = confidence.coerceAtMost(100)

        return ParseResult(
            isFinancial = isFinancial,
            provider = "EVC Plus",
            transactionType = type,
            amount = amount,
            senderNumber = senderNum,
            receiverNumber = receiverNum,
            balance = balance,
            referenceId = refId,
            confidence = confidence,
            suggestedCategory = suggestedCategory,
            extractedTimestamp = extractedTime
        )
    }
}
