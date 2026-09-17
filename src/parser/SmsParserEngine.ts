import { TransactionParser } from './TransactionParser';
import { EvcPlusParser } from './EvcPlusParser';
import { EDahabParser } from './EDahabParser';
import { JeebParser } from './JeebParser';
import { GenericSomaliMobileMoneyParser } from './GenericSomaliMobileMoneyParser';
import { ParseResult } from '../types';

export class SmsParserEngine {
  private parsers: TransactionParser[];

  constructor() {
    this.parsers = [
      new EvcPlusParser(),
      new EDahabParser(),
      new JeebParser(),
      new GenericSomaliMobileMoneyParser(),
    ];
  }

  parseSms(sender: string, body: string): ParseResult | null {
    for (const parser of this.parsers) {
      if (parser.canParse(sender, body)) {
        const result = parser.parse(sender, body);
        if (result !== null) {
          return result;
        }
      }
    }
    return null;
  }
}

export const defaultParserEngine = new SmsParserEngine();
