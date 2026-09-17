export type TransactionType = 'INCOME' | 'EXPENSE' | 'AIRTIME' | 'BILL_PAYMENT' | 'TRANSFER';

export interface Transaction {
  id: number;
  amount: number;
  senderNumber?: string;
  receiverNumber?: string;
  transactionDate: number; // timestamp ms
  rawSms: string;
  originalSms: string;
  transactionType: TransactionType;
  category?: string;
  notes?: string;
  referenceId?: string;
  balance?: number;
  provider: string; // 'EVCPlus', 'EDAHAB', 'Jeeb', 'ZAAD', 'SAHAL', etc.
  isVerified: boolean;
}

export interface ParseResult {
  amount: number;
  type: TransactionType;
  sender?: string;
  receiver?: string;
  balance?: number;
  reference?: string;
  provider: string;
  category?: string;
  confidenceScore: number;
  timestamp?: number;
}

export type TimeRange = 'ALL' | 'TODAY' | 'WEEK' | 'MONTH' | 'YEAR';

export interface QuickCategory {
  name: string;
  iconName?: string;
  color?: string;
}
