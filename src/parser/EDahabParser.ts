import { TransactionParser } from './TransactionParser';
import { ParseResult, TransactionType } from '../types';

export class EDahabParser implements TransactionParser {
  private readonly validSenders = ['EDAHAB', '898', 'Somtel', '330', 'e-dahab'];

  canParse(sender: string, body: string): boolean {
    const s = sender.trim().toLowerCase();
    const isSenderMatch = this.validSenders.some(
      (valid) => s.includes(valid.toLowerCase()) || valid.toLowerCase().includes(s)
    );
    const hasEDahabKeywords =
      body.toLowerCase().includes('edahab') ||
      body.toLowerCase().includes('e-dahab') ||
      body.toLowerCase().includes('tarjumaha:') ||
      (body.toLowerCase().includes('waxaad heshay') && body.includes('Somtel'));

    return isSenderMatch || hasEDahabKeywords;
  }

  parse(sender: string, body: string): ParseResult | null {
    try {
      let type: TransactionType = 'EXPENSE';
      let amount = 0;
      let counterpartyName: string | undefined;
      let counterpartyPhone: string | undefined;
      let balance: number | undefined;
      let reference: string | undefined;

      const lowerBody = body.toLowerCase();
      if (lowerBody.includes('waxaad heshay') || lowerBody.includes('laguu soo diray')) {
        type = 'INCOME';
      } else if (lowerBody.includes('ku shubatay') || lowerBody.includes('airtime')) {
        type = 'AIRTIME';
      } else if (lowerBody.includes('ku bixisay') || lowerBody.includes('bixisay')) {
        type = 'BILL_PAYMENT';
      } else {
        type = 'EXPENSE';
      }

      // Amount: "$40.00" or "$ 40.00" or "40.00 USD"
      const amountMatch = body.match(/\$\s*([0-9.,]+)/) || body.match(/([0-9.,]+)\s*USD/i);
      if (amountMatch) {
        amount = parseFloat(amountMatch[1].replace(/,/g, ''));
      }

      if (isNaN(amount) || amount <= 0) {
        return null;
      }

      // Counterparty
      if (type === 'INCOME') {
        const pMatch = body.match(/ka\s+(?:heshay\s+)?([^(\n]+?)\s*(?:\(([^)]+)\))?/i);
        if (pMatch) {
          counterpartyName = pMatch[1]?.trim();
          counterpartyPhone = pMatch[2]?.trim();
        }
      } else {
        const pMatch = body.match(/u\s+(?:dirtay\s+|wareejisay\s+)?([^(\n]+?)\s*(?:\(([^)]+)\))?/i);
        if (pMatch) {
          counterpartyName = pMatch[1]?.trim();
          counterpartyPhone = pMatch[2]?.trim();
        }
      }

      // Balance
      const balanceMatch = body.match(/Haraaga(?:agu)?(?:\s+waa)?[:\s]*\$?\s*([0-9.,]+)/i);
      if (balanceMatch) {
        const b = parseFloat(balanceMatch[1].replace(/,/g, ''));
        if (!isNaN(b)) balance = b;
      }

      // Reference: "Tarjumaha: EDH1234" or "Ref: 12345"
      const refMatch = body.match(/Tarjumaha[:\s]*([A-Za-z0-9]+)/i) || body.match(/Ref[:\s]*([A-Za-z0-9]+)/i);
      if (refMatch) {
        reference = refMatch[1].trim();
      }

      const counterparty = counterpartyName
        ? (counterpartyPhone ? `${counterpartyName} (${counterpartyPhone})` : counterpartyName)
        : counterpartyPhone;

      let category = type === 'INCOME' ? 'Dakhli' : 'Other';
      if (type === 'AIRTIME') category = 'Ku hadal Taleefan';
      if (counterpartyName) {
        const lower = counterpartyName.toLowerCase();
        if (lower.includes('bajaaj') || lower.includes('taxi')) category = 'Bajaaj';
        else if (lower.includes('cunto') || lower.includes('restaurant')) category = 'Cunto';
        else if (lower.includes('dukaan') || lower.includes('supermarket')) category = 'Dukaan';
        else if (lower.includes('koronto')) category = 'Koronto';
        else if (lower.includes('biyo')) category = 'Biyo';
        else if (lower.includes('kiro')) category = 'Kiro';
        else if (lower.includes('internet')) category = 'Internet';
        else if (lower.includes('shaah')) category = 'Shaah';
      }

      return {
        amount,
        type,
        sender: type === 'INCOME' ? counterparty : undefined,
        receiver: type !== 'INCOME' ? counterparty : undefined,
        balance,
        reference,
        provider: 'EDAHAB',
        category,
        confidenceScore: 0.95,
        timestamp: Date.now(),
      };
    } catch {
      return null;
    }
  }
}
