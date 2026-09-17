import React from 'react';
import { useTransactions } from '../context/TransactionContext';
import { CheckCircle, Check, X, ShieldAlert, Sparkles } from 'lucide-react';

export const ReviewView: React.FC = () => {
  const { unverifiedTransactions, verifyTransaction, deleteTransaction } =
    useTransactions();

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">
          Needs Review (Dib-u-eegis)
        </h1>
        <p className="text-xs sm:text-sm text-slate-500">
          Fariimaha cusub ee u baahan xaqiijin iyo kala soocid
        </p>
      </div>

      {unverifiedTransactions.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-200/80 shadow-xs space-y-3">
          <div className="w-14 h-14 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto">
            <CheckCircle className="w-8 h-8" />
          </div>
          <div className="space-y-1">
            <h3 className="text-lg font-black text-slate-900">All caught up!</h3>
            <p className="text-xs text-slate-500">
              Dhammaan fariimihii hore waa la ansixiyay oo waxay ku jiraan xisaabaadkaaga.
            </p>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          {unverifiedTransactions.map((tx) => {
            const isIncome = tx.transactionType === 'INCOME';
            const isAirtime = tx.transactionType === 'AIRTIME';
            const isExpense =
              tx.transactionType === 'EXPENSE' ||
              tx.transactionType === 'BILL_PAYMENT';

            const amountColor = isIncome
              ? 'text-emerald-700'
              : isAirtime
              ? 'text-teal-700'
              : 'text-red-700';

            const prefix = isIncome ? '+' : isExpense || isAirtime ? '-' : '';

            const desc = isIncome
              ? `Money received from ${tx.senderNumber || 'Unknown'}`
              : `Money sent to ${tx.receiverNumber || 'Unknown'}`;

            return (
              <div
                key={tx.id}
                className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-black text-emerald-600 tracking-wider uppercase bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200/60">
                    NEW TRANSACTION
                  </span>
                  <span className="text-xs text-slate-400 font-mono">
                    {tx.referenceId ? `Ref: ${tx.referenceId}` : ''}
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-base font-extrabold text-slate-900">
                      {tx.provider}
                    </h3>
                    <p className="text-sm text-slate-600 mt-0.5">{desc}</p>
                    {tx.balance !== undefined && (
                      <p className="text-xs text-slate-400 mt-1">
                        Balance ka dambeeyay: <strong>${tx.balance.toFixed(2)}</strong>
                      </p>
                    )}
                  </div>

                  <div className={`text-2xl font-black ${amountColor}`}>
                    {prefix}${tx.amount.toFixed(2)}
                  </div>
                </div>

                {/* SMS Raw Snippet */}
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-100 text-xs font-mono text-slate-600">
                  {tx.originalSms}
                </div>

                {/* Actions */}
                <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                  <button
                    onClick={() => deleteTransaction(tx.id)}
                    className="px-4 py-2 rounded-xl text-xs font-bold text-red-600 hover:bg-red-50 transition-colors flex items-center gap-1.5"
                  >
                    <X className="w-4 h-4" />
                    DISCARD (Tir)
                  </button>

                  <button
                    onClick={() => verifyTransaction(tx.id)}
                    className="px-5 py-2 rounded-xl text-xs font-bold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
                  >
                    <Check className="w-4 h-4" />
                    SAVE (Kaydi)
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
