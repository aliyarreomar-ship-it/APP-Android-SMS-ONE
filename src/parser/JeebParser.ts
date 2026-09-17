import { TransactionParser } from './TransactionParser';
import { ParseResult, TransactionType } from '../types';

export class JeebParser implements TransactionParser {
  private readonly validSenders = ['Jeeb', 'PremierBank', 'Premier', 'JeebPay'];

  canParse(sender: string, body: string): boolean {
    const s = sender.trim().toLowerCase();
    return this.validSenders.some((v) => s.includes(v.toLowerCase())) || body.toLowerCase().includes('jeeb');
  }

  parse(sender: string, body: string): ParseResult | null {
    try {
      let type: TransactionType = 'EXPENSE';
      let amount = 0;
      let counterparty: string | undefined;
      let balance: number | undefined;
      let reference: string | undefined;

      const lower = body.toLowerCase();
      if (lower.includes('received') || lower.includes('waxaad heshay') || lower.includes('credited')) {
        type = 'INCOME';
      } else {
        type = 'EXPENSE';
      }

      const amountMatch = body.match(/\$\s*([0-9.,]+)/) || body.match(/USD\s*([0-9.,]+)/i) || body.match(/([0-9.,]+)\s*USD/i);
      if (amountMatch) {
        amount = parseFloat(amountMatch[1].replace(/,/g, ''));
      }
      if (isNaN(amount) || amount <= 0) return null;

      const toFromMatch = body.match(/(?:from|to|ka|u)\s+([^.\n,]+)/i);
      if (toFromMatch) {
        counterparty = toFromMatch[1].trim();
      }

      const balMatch = body.match(/balance[:\s]*\$?\s*([0-9.,]+)/i) || body.match(/haraaga[:\s]*\$?\s*([0-9.,]+)/i);
      if (balMatch) {
        const b = parseFloat(balMatch[1].replace(/,/g, ''));
        if (!isNaN(b)) balance = b;
      }

      const refMatch = body.match(/(?:TxId|Ref|Reference)[:\s]*([A-Za-z0-9]+)/i);
      if (refMatch) {
        reference = refMatch[1].trim();
      }

      return {
        amount,
        type,
        sender: type === 'INCOME' ? counterparty : undefined,
        receiver: type !== 'INCOME' ? counterparty : undefined,
        balance,
        reference,
        provider: 'Jeeb',
        category: type === 'INCOME' ? 'Dakhli' : 'Other',
        confidenceScore: 0.9,
        timestamp: Date.now(),
      };
    } catch {
      return null;
    }
  }
}
