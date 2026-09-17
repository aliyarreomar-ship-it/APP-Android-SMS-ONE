import React, { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import { Transaction, TransactionType } from '../types';
import { INITIAL_TRANSACTIONS } from '../data/seedData';
import { defaultParserEngine } from '../parser/SmsParserEngine';

export type AppTab = 'dashboard' | 'transactions' | 'review' | 'analytics' | 'reports' | 'scanner' | 'settings';

interface ToastInfo {
  id: number;
  text: string;
  type: 'success' | 'error' | 'info';
}

interface TransactionContextType {
  transactions: Transaction[];
  unverifiedTransactions: Transaction[];
  pendingPopupTx: Transaction | null;
  activeTab: AppTab;
  setActiveTab: (tab: AppTab) => void;
  selectedDetailTx: Transaction | null;
  setSelectedDetailTx: (tx: Transaction | null) => void;
  dismissPopup: () => void;
  savePopup: (category: string, note: string) => void;
  verifyTransaction: (id: number) => void;
  deleteTransaction: (id: number) => void;
  deleteAllTransactions: () => void;
  clearAllHistory: () => void;
  clearSmsCache: () => void;
  processSyntheticSms: (sender: string, body: string, triggerPopup?: boolean) => Transaction | null;
  scanBatchSms: (rawText: string) => { total: number; parsed: number };
  toast: ToastInfo | null;
  showToast: (text: string, type?: 'success' | 'error' | 'info') => void;
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
}

const STORAGE_KEY = 'sms_reader_pro_transactions_v2';

const TransactionContext = createContext<TransactionContextType | undefined>(undefined);

export const TransactionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {
      // fallback
    }
    return INITIAL_TRANSACTIONS;
  });

  const [activeTab, setActiveTab] = useState<AppTab>('dashboard');
  const [pendingPopupTx, setPendingPopupTx] = useState<Transaction | null>(null);
  const [selectedDetailTx, setSelectedDetailTx] = useState<Transaction | null>(null);
  const [toast, setToast] = useState<ToastInfo | null>(null);

  // Sync to local storage
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(transactions));
    } catch (e) {
      console.error('Failed to save transactions to localStorage', e);
    }
  }, [transactions]);

  const showToast = (text: string, type: 'success' | 'error' | 'info' = 'info') => {
    const id = Date.now();
    setToast({ id, text, type });
    setTimeout(() => {
      setToast((curr) => (curr?.id === id ? null : curr));
    }, 4000);
  };

  const unverifiedTransactions = useMemo(() => {
    return transactions.filter((t) => !t.isVerified);
  }, [transactions]);

  const totalIncome = useMemo(() => {
    return transactions
      .filter((t) => t.transactionType === 'INCOME')
      .reduce((sum, t) => sum + t.amount, 0);
  }, [transactions]);

  const totalExpense = useMemo(() => {
    return transactions
      .filter((t) => t.transactionType === 'EXPENSE' || t.transactionType === 'AIRTIME' || t.transactionType === 'BILL_PAYMENT')
      .reduce((sum, t) => sum + t.amount, 0);
  }, [transactions]);

  const netBalance = useMemo(() => {
    return totalIncome - totalExpense;
  }, [totalIncome, totalExpense]);

  const verifyTransaction = (id: number) => {
    setTransactions((prev) =>
      prev.map((t) => (t.id === id ? { ...t, isVerified: true } : t))
    );
    showToast('Xisaabta waa la ansixiyay (Saved)', 'success');
  };

  const deleteTransaction = (id: number) => {
    setTransactions((prev) => prev.filter((t) => t.id !== id));
    if (selectedDetailTx?.id === id) {
      setSelectedDetailTx(null);
    }
    showToast('Xisaabta waa la tirtiray', 'info');
  };

  const deleteAllTransactions = () => {
    setTransactions([]);
    setSelectedDetailTx(null);
    setPendingPopupTx(null);
    showToast('Dhammaan xisaabaadkii hore waa la tirtiray', 'error');
  };

  const clearAllHistory = () => {
    setTransactions([]);
    setSelectedDetailTx(null);
    setPendingPopupTx(null);
    localStorage.removeItem(STORAGE_KEY);
    showToast('App-ka hadda waa la nadiifiyay, diyaar ayuu u yahay fariimaha cusub!', 'success');
  };

  const clearSmsCache = () => {
    showToast('Kaydka SMS-yada waa la nadiifiyay', 'info');
  };

  const processSyntheticSms = (sender: string, body: string, triggerPopup = true): Transaction | null => {
    const parseResult = defaultParserEngine.parseSms(sender, body);
    if (!parseResult) {
      showToast('Fariintani kuma jiro xog xisaab oo la aqoonsan karo', 'error');
      return null;
    }

    // Check duplicate
    if (parseResult.reference) {
      const isDuplicate = transactions.some((t) => t.referenceId === parseResult.reference);
      if (isDuplicate) {
        showToast(`Fariintan horay ayaa loo qabtay (Ref: ${parseResult.reference})`, 'info');
        return null;
      }
    }

    const newTx: Transaction = {
      id: Date.now() + Math.floor(Math.random() * 1000),
      amount: parseResult.amount,
      senderNumber: parseResult.sender,
      receiverNumber: parseResult.receiver,
      transactionDate: parseResult.timestamp || Date.now(),
      rawSms: body,
      originalSms: body,
      transactionType: parseResult.type,
      category: parseResult.category || (parseResult.type === 'INCOME' ? 'Dakhli' : 'Other'),
      notes: '',
      referenceId: parseResult.reference,
      balance: parseResult.balance,
      provider: parseResult.provider,
      isVerified: !triggerPopup, // If popup triggered, will verify when saved
    };

    setTransactions((prev) => [newTx, ...prev]);

    if (triggerPopup) {
      setPendingPopupTx(newTx);
    } else {
      showToast(`Fariin cusub ayaa la qabtay (${newTx.provider}: $${newTx.amount.toFixed(2)})`, 'success');
    }

    return newTx;
  };

  const dismissPopup = () => {
    // If dismissed, remains in unverified or just keeps current state
    setPendingPopupTx(null);
  };

  const savePopup = (category: string, note: string) => {
    if (!pendingPopupTx) return;
    setTransactions((prev) =>
      prev.map((t) =>
        t.id === pendingPopupTx.id
          ? { ...t, category, notes: note, isVerified: true }
          : t
      )
    );
    showToast(`Xisaabta waxaa lagu diiwaangeliyay: ${category}`, 'success');
    setPendingPopupTx(null);
  };

  const scanBatchSms = (rawText: string) => {
    const chunks = rawText
      .split(/\n\s*\n|(?=\[\+?\-?\$|\bWaxaad\b|\bYou have received\b)/i)
      .map((s) => s.trim())
      .filter((s) => s.length > 15);

    let parsedCount = 0;
    for (const chunk of chunks) {
      let sender = 'EVCPlus';
      if (/edahab|somtel|tarjumaha/i.test(chunk)) sender = 'EDAHAB';
      else if (/jeeb|premier/i.test(chunk)) sender = 'Jeeb';
      else if (/zaad|telesom/i.test(chunk)) sender = 'ZAAD';
      else if (/sahal|golis/i.test(chunk)) sender = 'SAHAL';

      const tx = processSyntheticSms(sender, chunk, false);
      if (tx) parsedCount++;
    }

    return { total: chunks.length, parsed: parsedCount };
  };

  return (
    <TransactionContext.Provider
      value={{
        transactions,
        unverifiedTransactions,
        pendingPopupTx,
        activeTab,
        setActiveTab,
        selectedDetailTx,
        setSelectedDetailTx,
        dismissPopup,
        savePopup,
        verifyTransaction,
        deleteTransaction,
        deleteAllTransactions,
        clearAllHistory,
        clearSmsCache,
        processSyntheticSms,
        scanBatchSms,
        toast,
        showToast,
        totalIncome,
        totalExpense,
        netBalance,
      }}
    >
      {children}
    </TransactionContext.Provider>
  );
};

export const useTransactions = (): TransactionContextType => {
  const context = useContext(TransactionContext);
  if (!context) {
    throw new Error('useTransactions must be used within a TransactionProvider');
  }
  return context;
};
