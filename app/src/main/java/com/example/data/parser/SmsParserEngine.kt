package com.example.data.parser

class SmsParserEngine {
    private val parsers = listOf(
        EvcPlusParser(),
        EDahabParser(),
        JeebParser(),
        GenericSomaliMobileMoneyParser()
    )

    fun parse(sender: String, body: String, timestamp: Long): ParseResult {
        val parser = parsers.firstOrNull { it.canParse(sender, body) }
        
        return parser?.parse(sender, body, timestamp) ?: ParseResult(isFinancial = false)
    }
}
