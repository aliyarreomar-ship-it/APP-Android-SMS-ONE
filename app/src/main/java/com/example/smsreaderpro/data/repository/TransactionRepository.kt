package com.example.smsreaderpro.data.repository

import com.example.smsreaderpro.data.local.TransactionDao
import com.example.smsreaderpro.data.local.TransactionEntity
import com.example.smsreaderpro.data.model.Transaction
import com.example.smsreaderpro.data.model.TransactionType
import com.example.smsreaderpro.data.parser.SmsParserEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val dao: TransactionDao,
    private val parserEngine: SmsParserEngine = SmsParserEngine.defaultEngine
) {
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions().map { entities ->
        entities.map { it.toDomain() }
    }

    val unverifiedTransactions: Flow<List<Transaction>> = dao.getUnverifiedTransactions().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun checkAndSeedInitialData() {
        if (dao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val seed = listOf(
                Transaction(
                    id = 0,
                    amount = 35.0,
                    senderNumber = "FAARAX MAXAMED (615887766)",
                    transactionDate = now - (1000 * 60 * 35),
                    rawSms = "[+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX MAXAMED (615887766). Haraagaagu waa $185.00. Ref:EVC8491. Tar: 16/09/26 19:10:00",
                    originalSms = "[+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX MAXAMED (615887766). Haraagaagu waa $185.00. Ref:EVC8491. Tar: 16/09/26 19:10:00",
                    transactionType = TransactionType.INCOME,
                    category = "Dakhli",
                    notes = "Lacagtii heshiiska shaqada",
                    referenceId = "EVC8491",
                    balance = 185.0,
                    provider = "EVCPlus",
                    isVerified = true
                ),
                Transaction(
                    id = 0,
                    amount = 15.0,
                    receiverNumber = "DUKAANKA CUNTADA (612334455)",
                    transactionDate = now - (1000 * 60 * 95),
                    rawSms = "[-$15.00] Waad u dirtay DUKAANKA CUNTADA (612334455). Haraagaagu waa $170.00. Ref:EVC9201. Tar: 16/09/26 19:12:00",
                    originalSms = "[-$15.00] Waad u dirtay DUKAANKA CUNTADA (612334455). Haraagaagu waa $170.00. Ref:EVC9201. Tar: 16/09/26 19:12:00",
                    transactionType = TransactionType.EXPENSE,
                    category = "Cunto",
                    notes = "Raashin & qudaar",
                    referenceId = "EVC9201",
                    balance = 170.0,
                    provider = "EVCPlus",
                    isVerified = true
                ),
                Transaction(
                    id = 0,
                    amount = 40.0,
                    senderNumber = "AXMED ROOBLE (659911223)",
                    transactionDate = now - (1000 * 60 * 60 * 5),
                    rawSms = "Waxaad heshay $40.00 ka heshay AXMED ROOBLE (659911223). Haraagaagu waa $210.00. Tarjumaha: EDH4920",
                    originalSms = "Waxaad heshay $40.00 ka heshay AXMED ROOBLE (659911223). Haraagaagu waa $210.00. Tarjumaha: EDH4920",
                    transactionType = TransactionType.INCOME,
                    category = "Dakhli",
                    notes = "Wareejin E-Dahab Somtel",
                    referenceId = "EDH4920",
                    balance = 210.0,
                    provider = "EDAHAB",
                    isVerified = true
                ),
                Transaction(
                    id = 0,
                    amount = 3.5,
                    receiverNumber = "BAJAAJ DEKEDDA (615002233)",
                    transactionDate = now - (1000 * 60 * 60 * 9),
                    rawSms = "[-$3.50] Waad u dirtay BAJAAJ DEKEDDA (615002233). Haraagaagu waa $166.50. Ref:EVC3811. Tar: 16/09/26 10:45:00",
                    originalSms = "[-$3.50] Waad u dirtay BAJAAJ DEKEDDA (615002233). Haraagaagu waa $166.50. Ref:EVC3811. Tar: 16/09/26 10:45:00",
                    transactionType = TransactionType.EXPENSE,
                    category = "Bajaaj",
                    notes = "Raacid bajaaj suuqa Bakaaraha",
                    referenceId = "EVC3811",
                    balance = 166.5,
                    provider = "EVCPlus",
                    isVerified = true
                ),
                Transaction(
                    id = 0,
                    amount = 25.0,
                    receiverNumber = "BECO POWER (619001122)",
                    transactionDate = now - (1000 * 60 * 60 * 26),
                    rawSms = "[-$25.00] Waad u dirtay BECO POWER (619001122). Haraagaagu waa $141.50. Ref:EVC7722. Tar: 15/09/26 14:20:00",
                    originalSms = "[-$25.00] Waad u dirtay BECO POWER (619001122). Haraagaagu waa $141.50. Ref:EVC7722. Tar: 15/09/26 14:20:00",
                    transactionType = TransactionType.BILL_PAYMENT,
                    category = "Koronto",
                    notes = "Biilka korontada bishan",
                    referenceId = "EVC7722",
                    balance = 141.5,
                    provider = "EVCPlus",
                    isVerified = true
                ),
                Transaction(
                    id = 0,
                    amount = 50.0,
                    senderNumber = "SALARY PREMIER (PremierBank)",
                    transactionDate = now - (1000 * 60 * 60 * 72),
                    rawSms = "You have received $50.00 from PremierBank to your Jeeb Account. Balance: $250.00. TxId: JB50192",
                    originalSms = "You have received $50.00 from PremierBank to your Jeeb Account. Balance: $250.00. TxId: JB50192",
                    transactionType = TransactionType.INCOME,
                    category = "Dakhli",
                    notes = "Dakhli Jeeb Pay",
                    referenceId = "JB50192",
                    balance = 250.0,
                    provider = "Jeeb",
                    isVerified = true
                )
            )
            dao.insertAll(seed.map { TransactionEntity.fromDomain(it) })
        }
    }

    suspend fun processIncomingSms(sender: String, body: String, isVerified: Boolean = false): Transaction? {
        val result = parserEngine.parseSms(sender, body) ?: return null

        if (result.reference != null) {
            val existing = dao.findByReference(result.reference)
            if (existing != null) {
                return null
            }
        }

        val transaction = Transaction(
            id = 0,
            amount = result.amount,
            senderNumber = result.sender,
            receiverNumber = result.receiver,
            transactionDate = result.timestamp,
            rawSms = body,
            originalSms = body,
            transactionType = result.type,
            category = result.category ?: if (result.type == TransactionType.INCOME) "Dakhli" else "Other",
            notes = null,
            referenceId = result.reference,
            balance = result.balance,
            provider = result.provider,
            isVerified = isVerified
        )

        val generatedId = dao.insertTransaction(TransactionEntity.fromDomain(transaction))
        return transaction.copy(id = generatedId)
    }

    suspend fun verifyTransaction(id: Long) {
        dao.verifyTransaction(id)
    }

    suspend fun updateCategoryAndNotes(id: Long, category: String, notes: String) {
        dao.updateCategoryAndNotes(id, category, notes)
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteAllTransactions() {
        dao.deleteAll()
    }
}
