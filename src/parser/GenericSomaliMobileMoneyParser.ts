import { TransactionParser } from './TransactionParser';
import { ParseResult, TransactionType } from '../types';

export class GenericSomaliMobileMoneyParser implements TransactionParser {
  canParse(sender: string, body: string): boolean {
    const s = sender.toLowerCase();
    const b = body.toLowerCase();
    return (
      s.includes('zaad') ||
      s.includes('telesom') ||
      s.includes('sahal') ||
      s.includes('golis') ||
      b.includes('zaad') ||
      b.includes('sahal') ||
      b.includes('haraaga') ||
      b.includes('waxaad heshay') ||
      b.includes('waxaad dirtay') ||
      b.includes('u dirtay') ||
      b.includes('ka heshay')
    );
  }

  parse(sender: string, body: string): ParseResult | null {
    try {
      let provider = 'MobileMoney';
      const s = sender.toLowerCase();
      const b = body.toLowerCase();
      if (s.includes('zaad') || b.includes('zaad')) provider = 'ZAAD';
      else if (s.includes('sahal') || b.includes('sahal')) provider = 'SAHAL';
      else if (s.includes('evc') || b.includes('evc')) provider = 'EVCPlus';
      else if (s.includes('dahab') || b.includes('dahab')) provider = 'EDAHAB';

      let type: TransactionType = 'EXPENSE';
      if (b.includes('waxaad heshay') || b.includes('laguu soo diray') || b.includes('received')) {
        type = 'INCOME';
      } else if (b.includes('ku shubatay') || b.includes('airtime')) {
        type = 'AIRTIME';
      } else if (b.includes('bixisay')) {
        type = 'BILL_PAYMENT';
      }

      let amount = 0;
      const amountMatch = body.match(/\$\s*([0-9.,]+)/) || body.match(/([0-9.,]+)\s*(?:USD|SLSH|\$)/i);
      if (amountMatch) {
        amount = parseFloat(amountMatch[1].replace(/,/g, ''));
      }
      if (isNaN(amount) || amount <= 0) return null;

      let balance: number | undefined;
      const balMatch = body.match(/Haraaga(?:agu)?(?:\s+waa)?[:\s]*\$?\s*([0-9.,]+)/i);
      if (balMatch) {
        const bl = parseFloat(balMatch[1].replace(/,/g, ''));
        if (!isNaN(bl)) balance = bl;
      }

      let reference: string | undefined;
      const refMatch = body.match(/(?:Ref|Tarjumaha|TxId)[:\s]*([A-Za-z0-9]+)/i);
      if (refMatch) reference = refMatch[1].trim();

      let counterparty: string | undefined;
      const partyMatch = body.match(/(?:ka|u|from|to)\s+([^.\n,]+)/i);
      if (partyMatch) counterparty = partyMatch[1].trim();

      return {
        amount,
        type,
        sender: type === 'INCOME' ? counterparty : undefined,
        receiver: type !== 'INCOME' ? counterparty : undefined,
        balance,
        reference,
        provider,
        category: type === 'INCOME' ? 'Dakhli' : 'Other',
        confidenceScore: 0.8,
        timestamp: Date.now(),
      };
    } catch {
      return null;
    }
  }
}
