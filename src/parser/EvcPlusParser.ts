import { TransactionParser } from './TransactionParser';
import { ParseResult, TransactionType } from '../types';

export class EvcPlusParser implements TransactionParser {
  private readonly validSenders = ['EVCPlus', '192', 'Hormuud', 'waafi', '770', '780', '199', '141'];

  canParse(sender: string, body: string): boolean {
    const s = sender.trim().toLowerCase();
    const isSenderMatch = this.validSenders.some(
      (valid) => s.includes(valid.toLowerCase()) || valid.toLowerCase().includes(s)
    );
    const hasEvcKeywords =
      body.includes('EVCPlus') ||
      body.includes('waa laguu soo diray') ||
      body.includes('waad u dirtay') ||
      body.includes('Haraagaagu waa') ||
      body.includes('Haraaga:') ||
      body.includes('[+$') ||
      body.includes('[-$');

    return isSenderMatch || hasEvcKeywords;
  }

  parse(sender: string, body: string): ParseResult | null {
    try {
      let type: TransactionType = 'EXPENSE';
      let amount = 0;
      let counterpartyName: string | undefined;
      let counterpartyPhone: string | undefined;
      let balance: number | undefined;
      let reference: string | undefined;
      let timestamp: number | undefined;

      // Check transaction type
      if (body.includes('[+$') || body.toLowerCase().includes('waa laguu soo diray') || body.toLowerCase().includes('waxaad ka heshay')) {
        type = 'INCOME';
      } else if (body.toLowerCase().includes('ku shubatay') || body.toLowerCase().includes('kugu shubtay') || body.toLowerCase().includes('airtime')) {
        type = 'AIRTIME';
      } else if (body.toLowerCase().includes('bixisay') || body.toLowerCase().includes('ku bixisay') || body.toLowerCase().includes('merchant')) {
        type = 'BILL_PAYMENT';
      } else {
        type = 'EXPENSE';
      }

      // Amount extraction
      const bracketAmountMatch = body.match(/\[([+-]?)\$([0-9.,]+)\]/);
      if (bracketAmountMatch) {
        amount = parseFloat(bracketAmountMatch[2].replace(/,/g, ''));
        if (bracketAmountMatch[1] === '+') type = 'INCOME';
        if (bracketAmountMatch[1] === '-') type = 'EXPENSE';
      } else {
        // Match general dollar patterns: $35.00 or 35.00 USD
        const dollarMatch = body.match(/\$([0-9.,]+)/) || body.match(/([0-9.,]+)\s*USD/i);
        if (dollarMatch) {
          amount = parseFloat(dollarMatch[1].replace(/,/g, ''));
        }
      }

      if (isNaN(amount) || amount <= 0) {
        return null;
      }

      // Counterparty extraction
      if (type === 'INCOME') {
        // "Waxaana soo diray FAARAX MAXAMED (615887766)"
        const senderMatch = body.match(/Waxaana soo diray\s*([^(\n]+?)\s*(?:\(([^)]+)\))?/i) ||
                            body.match(/ka heshay\s*([^(\n]+?)\s*(?:\(([^)]+)\))?/i);
        if (senderMatch) {
          counterpartyName = senderMatch[1]?.trim();
          counterpartyPhone = senderMatch[2]?.trim();
        }
      } else {
        // "Waad u dirtay DUKAANKA CUNTADA (612334455)"
        const receiverMatch = body.match(/Waad u dirtay\s*([^(\n]+?)\s*(?:\(([^)]+)\))?/i) ||
                              body.match(/u dirtay\s*([^(\n]+?)\s*(?:\(([^)]+)\))?/i) ||
                              body.match(/u wareejisay\s*([^(\n]+?)\s*(?:\(([^)]+)\))?/i);
        if (receiverMatch) {
          counterpartyName = receiverMatch[1]?.trim();
          counterpartyPhone = receiverMatch[2]?.trim();
        }
      }

      // Balance extraction: "Haraagaagu waa $185.00" or "Haraaga: $185.00"
      const balanceMatch = body.match(/Haraaga(?:agu)?(?:\s+waa)?[:\s]*\$([0-9.,]+)/i);
      if (balanceMatch) {
        const parsedBalance = parseFloat(balanceMatch[1].replace(/,/g, ''));
        if (!isNaN(parsedBalance)) {
          balance = parsedBalance;
        }
      }

      // Reference extraction: "Ref:EVC1234" or "Ref: 123456"
      const refMatch = body.match(/Ref[:\s]*([A-Za-z0-9]+)/i);
      if (refMatch) {
        reference = refMatch[1].trim();
      }

      // Date/Time extraction: "Tar: 16/09/26 19:10:00"
      const dateMatch = body.match(/Tar[:\s]*(\d{2}\/\d{2}\/\d{2,4}\s+\d{2}:\d{2}(?::\d{2})?)/i);
      if (dateMatch) {
        try {
          const parts = dateMatch[1].split(' ');
          const dateParts = parts[0].split('/');
          const timeParts = parts[1].split(':');
          let year = parseInt(dateParts[2], 10);
          if (year < 100) year += 2000;
          const month = parseInt(dateParts[1], 10) - 1;
          const day = parseInt(dateParts[0], 10);
          const hour = parseInt(timeParts[0], 10);
          const min = parseInt(timeParts[1], 10);
          const sec = timeParts[2] ? parseInt(timeParts[2], 10) : 0;
          timestamp = new Date(year, month, day, hour, min, sec).getTime();
        } catch {
          // ignore date parse fallback
        }
      }

      const counterparty = counterpartyName
        ? (counterpartyPhone ? `${counterpartyName} (${counterpartyPhone})` : counterpartyName)
        : counterpartyPhone;

      // Smart auto-category
      let category = type === 'INCOME' ? 'Dakhli' : 'Other';
      if (type === 'AIRTIME') category = 'Ku hadal Taleefan';
      if (type === 'BILL_PAYMENT') category = 'Biilasha';
      if (counterpartyName) {
        const lowerName = counterpartyName.toLowerCase();
        if (lowerName.includes('bajaaj') || lowerName.includes('taxi')) category = 'Bajaaj';
        else if (lowerName.includes('cunto') || lowerName.includes('rest') || lowerName.includes('cafe')) category = 'Cunto';
        else if (lowerName.includes('dukaan') || lowerName.includes('supermarket') || lowerName.includes('mall')) category = 'Dukaan';
        else if (lowerName.includes('koronto') || lowerName.includes('electric') || lowerName.includes('beco')) category = 'Koronto';
        else if (lowerName.includes('biyo') || lowerName.includes('water')) category = 'Biyo';
        else if (lowerName.includes('kiro') || lowerName.includes('rent')) category = 'Kiro';
        else if (lowerName.includes('internet') || lowerName.includes('wifi')) category = 'Internet';
        else if (lowerName.includes('shaah') || lowerName.includes('tea')) category = 'Shaah';
      }

      return {
        amount,
        type,
        sender: type === 'INCOME' ? counterparty : undefined,
        receiver: type !== 'INCOME' ? counterparty : undefined,
        balance,
        reference,
        provider: 'EVCPlus',
        category,
        confidenceScore: 0.95,
        timestamp: timestamp || Date.now(),
      };
    } catch {
      return null;
    }
  }
}
