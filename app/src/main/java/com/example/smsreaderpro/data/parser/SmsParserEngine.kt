package com.example.smsreaderpro.data.parser

import com.example.smsreaderpro.data.model.ParseResult

class SmsParserEngine(
    private val parsers: List<TransactionParser> = listOf(
        EvcPlusParser(),
        EDahabParser(),
        JeebParser(),
        GenericSomaliParser()
    )
) {
    fun parseSms(sender: String, body: String): ParseResult? {
        for (parser in parsers) {
            if (parser.canParse(sender, body)) {
                val result = parser.parse(sender, body)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    companion object {
        val defaultEngine = SmsParserEngine()
    }
}
