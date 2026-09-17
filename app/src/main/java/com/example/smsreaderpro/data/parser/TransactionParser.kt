package com.example.smsreaderpro.data.parser

import com.example.smsreaderpro.data.model.ParseResult

interface TransactionParser {
    fun canParse(sender: String, body: String): Boolean
    fun parse(sender: String, body: String): ParseResult?
}
