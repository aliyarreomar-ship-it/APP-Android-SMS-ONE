import { ParseResult } from '../types';

export interface TransactionParser {
  canParse(sender: string, body: string): boolean;
  parse(sender: string, body: string): ParseResult | null;
}
