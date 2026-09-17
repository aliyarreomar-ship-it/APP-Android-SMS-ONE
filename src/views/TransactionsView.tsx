import React, { useState, useMemo } from 'react';
import { useTransactions } from '../context/TransactionContext';
import { TransactionItem } from '../components/TransactionItem';
import {
  Search,
  Filter,
  Trash2,
  AlertTriangle,
  ArrowUpDown,
  FileDown,
} from 'lucide-react';

export const TransactionsView: React.FC = () => {
  const {
    transactions,
    setSelectedDetailTx,
    deleteTransaction,
    deleteAllTransactions,
  } = useTransactions();

  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<'ALL' | 'INCOME' | 'EXPENSE' | 'AIRTIME'>('ALL');
  const [providerFilter, setProviderFilter] = useState<string>('ALL');
  const [showDeleteAllModal, setShowDeleteAllModal] = useState(false);

  // Available unique providers
  const availableProviders = useMemo(() => {
    const set = new Set<string>();
    transactions.forEach((t) => {
      if (t.provider) set.add(t.provider);
    });
    return Array.from(set);
  }, [transactions]);

  // Filtered transactions
  const filteredTransactions = useMemo(() => {
    return transactions.filter((t) => {
      // Type filter
      if (typeFilter === 'INCOME' && t.transactionType !== 'INCOME') return false;
      if (
        typeFilter === 'EXPENSE' &&
        t.transactionType !== 'EXPENSE' &&
        t.transactionType !== 'BILL_PAYMENT'
      )
        return false;
      if (typeFilter === 'AIRTIME' && t.transactionType !== 'AIRTIME') return false;

      // Provider filter
      if (providerFilter !== 'ALL' && t.provider !== providerFilter) return false;

      // Query search
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        const matchesSender = t.senderNumber?.toLowerCase().includes(q);
        const matchesReceiver = t.receiverNumber?.toLowerCase().includes(q);
        const matchesRef = t.referenceId?.toLowerCase().includes(q);
        const matchesNote = t.notes?.toLowerCase().includes(q);
        const matchesCategory = t.category?.toLowerCase().includes(q);
        const matchesProvider = t.provider.toLowerCase().includes(q);
        const matchesAmount = t.amount.toString().includes(q);
        const matchesSms = t.originalSms.toLowerCase().includes(q);

        return (
          matchesSender ||
          matchesReceiver ||
          matchesRef ||
          matchesNote ||
          matchesCategory ||
          matchesProvider ||
          matchesAmount ||
          matchesSms
        );
      }

      return true;
    });
  }, [transactions, typeFilter, providerFilter, searchQuery]);

  // Sum of filtered items
  const filteredSum = useMemo(() => {
    return filteredTransactions.reduce((acc, t) => {
      if (t.transactionType === 'INCOME') return acc + t.amount;
      return acc - t.amount;
    }, 0);
  }, [filteredTransactions]);

  return (
    <div className="space-y-6 pb-12">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            Xisaabaadka (Transactions)
          </h1>
          <p className="text-xs sm:text-sm text-slate-500">
            Dhammaan fariimaha iyo xogaha lacageed ee la qabtay
          </p>
        </div>

        {transactions.length > 0 && (
          <button
            onClick={() => setShowDeleteAllModal(true)}
            className="px-3.5 py-2 rounded-xl text-xs font-bold text-red-600 hover:bg-red-50 border border-red-200 transition-colors flex items-center gap-1.5 self-start sm:self-auto"
          >
            <Trash2 className="w-3.5 h-3.5" />
            Tir Dhammaan
          </button>
        )}
      </div>

      {/* Search & Filters Card */}
      <div className="bg-white rounded-3xl p-5 border border-slate-200/80 shadow-xs space-y-4">
        {/* Search Bar */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Raadi magac, taleefan, tixraac (Ref), qayb ama qoraalka fariinta..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl text-sm border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 bg-slate-50/50"
          />
        </div>

        {/* Filter Pills */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-1 border-t border-slate-100">
          {/* Transaction Type Filter */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
            {[
              { id: 'ALL', label: 'Dhammaan' },
              { id: 'INCOME', label: '+ Dakhli' },
              { id: 'EXPENSE', label: '- Kharash' },
              { id: 'AIRTIME', label: 'Airtime' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setTypeFilter(tab.id as any)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                  typeFilter === tab.id
                    ? 'bg-slate-900 text-white shadow-xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Provider Filter */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
            <span className="text-xs font-semibold text-slate-400 mr-1">Shirkadda:</span>
            <button
              onClick={() => setProviderFilter('ALL')}
              className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors ${
                providerFilter === 'ALL'
                  ? 'bg-emerald-600 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              All
            </button>
            {availableProviders.map((p) => (
              <button
                key={p}
                onClick={() => setProviderFilter(p)}
                className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors ${
                  providerFilter === p
                    ? 'bg-emerald-600 text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {p}
              </button>
            ))}
          </div>
        </div>

        {/* Stats Row */}
        <div className="flex items-center justify-between text-xs text-slate-500 pt-2 border-t border-slate-100">
          <span>
            Waxaa la helay: <strong className="text-slate-800">{filteredTransactions.length}</strong> xisaabood
          </span>
          <span>
            Isu-geyn: <strong className={filteredSum >= 0 ? 'text-emerald-700' : 'text-red-700'}>
              {filteredSum >= 0 ? '+' : ''}${filteredSum.toFixed(2)}
            </strong>
          </span>
        </div>
      </div>

      {/* Transactions List */}
      {filteredTransactions.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-200/80 shadow-xs space-y-2">
          <p className="text-base font-bold text-slate-700">Lama helin wax xisaabaad ah</p>
          <p className="text-xs text-slate-400">
            Isku day inaad bedesho erayga raadinta ama shaandhada.
          </p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {filteredTransactions.map((tx) => (
            <TransactionItem
              key={tx.id}
              transaction={tx}
              onClick={() => setSelectedDetailTx(tx)}
              onDelete={() => deleteTransaction(tx.id)}
            />
          ))}
        </div>
      )}

      {/* Delete All Modal */}
      {showDeleteAllModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <div className="text-center space-y-1">
              <h3 className="text-lg font-black text-slate-900">
                Tir Dhammaan Xisaabaadka?
              </h3>
              <p className="text-xs text-slate-500">
                Ma hubtaa inaad tirtirto dhammaan xisaabaadka ku jira app-ka? Xisaabaadkii hore dhan waa la tirtirayaa.
              </p>
            </div>
            <div className="flex gap-2 pt-2">
              <button
                onClick={() => setShowDeleteAllModal(false)}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 transition-colors"
              >
                Ka Noqo
              </button>
              <button
                onClick={() => {
                  deleteAllTransactions();
                  setShowDeleteAllModal(false);
                }}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-red-600 text-white hover:bg-red-700 transition-colors"
              >
                Haa, Tir Dhammaan
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
