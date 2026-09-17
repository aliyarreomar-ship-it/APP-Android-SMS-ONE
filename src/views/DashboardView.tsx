import React, { useState, useMemo } from 'react';
import { useTransactions } from '../context/TransactionContext';
import { TransactionItem } from '../components/TransactionItem';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  Clock,
  ArrowRight,
  ShieldCheck,
  Zap,
  Sparkles,
  PieChart,
} from 'lucide-react';

export const DashboardView: React.FC = () => {
  const {
    transactions,
    unverifiedTransactions,
    totalIncome,
    totalExpense,
    netBalance,
    setActiveTab,
    setSelectedDetailTx,
    deleteTransaction,
    processSyntheticSms,
  } = useTransactions();

  const [filterType, setFilterType] = useState<'ALL' | 'INCOME' | 'EXPENSE'>('ALL');

  const filteredRecentTransactions = useMemo(() => {
    return transactions
      .filter((t) => {
        if (filterType === 'INCOME') return t.transactionType === 'INCOME';
        if (filterType === 'EXPENSE')
          return (
            t.transactionType === 'EXPENSE' ||
            t.transactionType === 'AIRTIME' ||
            t.transactionType === 'BILL_PAYMENT'
          );
        return true;
      })
      .slice(0, 7);
  }, [transactions, filterType]);

  // Top spending categories
  const categoryStats = useMemo(() => {
    const map: { [cat: string]: number } = {};
    transactions
      .filter((t) => t.transactionType !== 'INCOME')
      .forEach((t) => {
        const cat = t.category || 'Other';
        map[cat] = (map[cat] || 0) + t.amount;
      });

    return Object.entries(map)
      .map(([name, total]) => ({ name, total }))
      .sort((a, b) => b.total - a.total)
      .slice(0, 4);
  }, [transactions]);

  const handleQuickSimulate = () => {
    const testRef = 'EVC' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EVCPlus',
      `[+$25.00] Waa laguu soo diray. Waxaana soo diray SAFIYA CALI (617711223). Haraagaagu waa $195.00. Ref:${testRef}. Tar: 16/09/26 19:30:00`,
      true
    );
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Top Banner: Real-time Sync & Version 2.0 */}
      <div className="bg-gradient-to-r from-emerald-900 via-slate-900 to-slate-900 text-white rounded-3xl p-6 shadow-md relative overflow-hidden">
        <div className="absolute right-0 top-0 translate-x-8 -translate-y-8 w-64 h-64 bg-emerald-500/10 rounded-full blur-2xl pointer-events-none" />

        <div className="relative z-10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                <ShieldCheck className="w-3.5 h-3.5" />
                V2.0 Automatic Sync Active
              </span>
              <span className="text-xs text-slate-400 font-mono">Zero Duplicate</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black tracking-tight">
              Guudmar Lacageed
            </h1>
            <p className="text-xs sm:text-sm text-slate-300">
              Qabashada iyo maareynta fariimaha EVC Plus, E-Dahab, iyo shirkadaha kale ee Soomaaliya.
            </p>
          </div>

          <button
            onClick={handleQuickSimulate}
            className="px-4 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-sm transition-all flex items-center gap-2 shrink-0"
          >
            <Zap className="w-4 h-4" />
            Tijaabi Fariin Cusub
          </button>
        </div>
      </div>

      {/* Financial Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Net Flow / Balance */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Haraaga Guud (Net Flow)
            </span>
            <div className="w-9 h-9 rounded-xl bg-slate-100 flex items-center justify-center text-slate-700">
              <Wallet className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <div
              className={`text-3xl sm:text-4xl font-black tracking-tight ${
                netBalance >= 0 ? 'text-slate-900' : 'text-red-600'
              }`}
            >
              {netBalance >= 0 ? '+' : '-'}${Math.abs(netBalance).toFixed(2)}
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Dakhli (${totalIncome.toFixed(2)}) - Kharash (${totalExpense.toFixed(2)})
            </p>
          </div>
        </div>

        {/* Total Income (Dakhli) */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">
              Dakhliga Guud (Income)
            </span>
            <div className="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingUp className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <div className="text-3xl sm:text-4xl font-black tracking-tight text-emerald-700">
              +${totalIncome.toFixed(2)}
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Dhammaan lacagaha laguu soo diray
            </p>
          </div>
        </div>

        {/* Total Expenses (Kharash) */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-red-600 uppercase tracking-wider">
              Kharashka Guud (Expense)
            </span>
            <div className="w-9 h-9 rounded-xl bg-red-50 text-red-600 flex items-center justify-center">
              <TrendingDown className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <div className="text-3xl sm:text-4xl font-black tracking-tight text-red-700">
              -${totalExpense.toFixed(2)}
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Kharashaad, biilal iyo airtime
            </p>
          </div>
        </div>
      </div>

      {/* Pending Reviews Banner (if any) */}
      {unverifiedTransactions.length > 0 && (
        <div className="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-amber-100 text-amber-800 flex items-center justify-center shrink-0">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-amber-900">
                Waxaa jira {unverifiedTransactions.length} xisaabood oo u baahan dib-u-eegis!
              </h4>
              <p className="text-xs text-amber-700">
                Fadlan hubi qaybta iyo faahfaahinta si xisaabaadkaagu u saxnaadaan.
              </p>
            </div>
          </div>
          <button
            onClick={() => setActiveTab('review')}
            className="px-4 py-2 rounded-xl text-xs font-bold bg-amber-600 hover:bg-amber-700 text-white shadow-xs transition-colors flex items-center gap-1.5 shrink-0"
          >
            <span>Eeg Hadda</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* Top Categories Distribution */}
      {categoryStats.length > 0 && (
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <PieChart className="w-5 h-5 text-emerald-600" />
              <h2 className="text-base font-extrabold text-slate-900">
                Qaybaha Kharashka Ugu Badan
              </h2>
            </div>
            <button
              onClick={() => setActiveTab('analytics')}
              className="text-xs font-bold text-emerald-600 hover:text-emerald-700 flex items-center gap-1"
            >
              Falanqayn dheeraad ah <ArrowRight className="w-3 h-3" />
            </button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {categoryStats.map((item) => {
              const pct = totalExpense > 0 ? (item.total / totalExpense) * 100 : 0;
              return (
                <div
                  key={item.name}
                  className="bg-slate-50/80 rounded-2xl p-3.5 border border-slate-100 space-y-1.5"
                >
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-bold text-slate-800">{item.name}</span>
                    <span className="font-mono text-slate-500">{pct.toFixed(0)}%</span>
                  </div>
                  <div className="text-base font-extrabold text-slate-900">
                    ${item.total.toFixed(2)}
                  </div>
                  <div className="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden">
                    <div
                      className="bg-emerald-600 h-full rounded-full"
                      style={{ width: `${Math.min(pct, 100)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Recent Transactions List */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-extrabold text-slate-900">
              Xisaabaadkii Ugu Dambeeyay
            </h2>
            <p className="text-xs text-slate-500">
              Fariimihii ugu dambeeyay ee laga qabtay SMS-yada
            </p>
          </div>

          {/* Type quick filter */}
          <div className="flex items-center gap-1.5 bg-slate-100 p-1 rounded-xl">
            {(['ALL', 'INCOME', 'EXPENSE'] as const).map((type) => (
              <button
                key={type}
                onClick={() => setFilterType(type)}
                className={`px-3 py-1 rounded-lg text-xs font-bold transition-colors ${
                  filterType === type
                    ? 'bg-white text-slate-900 shadow-xs'
                    : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                {type === 'ALL' ? 'Dhammaan' : type === 'INCOME' ? 'Dakhli (+)' : 'Kharash (-)'}
              </button>
            ))}
          </div>
        </div>

        {/* Transactions render */}
        {filteredRecentTransactions.length === 0 ? (
          <div className="text-center py-12 text-slate-400 space-y-2">
            <p className="text-sm font-semibold">Ma jiraan xisaabaad la helay.</p>
            <p className="text-xs">Ku dar fariin tijaabo ah adigoo gujinaya badhanka kore.</p>
          </div>
        ) : (
          <div className="space-y-2.5">
            {filteredRecentTransactions.map((tx) => (
              <TransactionItem
                key={tx.id}
                transaction={tx}
                onClick={() => setSelectedDetailTx(tx)}
                onDelete={() => deleteTransaction(tx.id)}
              />
            ))}
          </div>
        )}

        {transactions.length > 7 && (
          <div className="pt-2 text-center">
            <button
              onClick={() => setActiveTab('transactions')}
              className="px-5 py-2.5 rounded-xl text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-700 transition-colors inline-flex items-center gap-1.5"
            >
              Eeg Dhammaan Xisaabaadka ({transactions.length})
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
