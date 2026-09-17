import React from 'react';
import { Transaction } from '../types';
import {
  ArrowDownLeft,
  ArrowUpRight,
  Smartphone,
  CreditCard,
  Trash2,
  Calendar,
} from 'lucide-react';

interface TransactionItemProps {
  transaction: Transaction;
  onClick?: () => void;
  onDelete?: () => void;
}

export const TransactionItem: React.FC<TransactionItemProps> = ({
  transaction,
  onClick,
  onDelete,
}) => {
  const isIncome = transaction.transactionType === 'INCOME';
  const isAirtime = transaction.transactionType === 'AIRTIME';
  const isExpense =
    transaction.transactionType === 'EXPENSE' ||
    transaction.transactionType === 'BILL_PAYMENT';

  const amountColor = isIncome
    ? 'text-emerald-700'
    : isAirtime
    ? 'text-teal-700'
    : 'text-red-700';

  const prefix = isIncome ? '+' : isExpense || isAirtime ? '-' : '';

  const icon = isIncome ? (
    <ArrowDownLeft className="w-5 h-5 text-emerald-600" />
  ) : isAirtime ? (
    <Smartphone className="w-5 h-5 text-teal-600" />
  ) : (
    <ArrowUpRight className="w-5 h-5 text-red-600" />
  );

  const iconBgColor = isIncome
    ? 'bg-emerald-50'
    : isAirtime
    ? 'bg-teal-50'
    : 'bg-red-50';

  const peer = isIncome
    ? transaction.senderNumber
    : transaction.receiverNumber;

  const displaySubtitle = peer
    ? (isIncome ? `Laga helay: ${peer}` : `U dirtay: ${peer}`)
    : transaction.referenceId
    ? `Ref: ${transaction.referenceId}`
    : transaction.transactionType;

  const formattedDate = new Date(transaction.transactionDate).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div
      onClick={onClick}
      className={`group relative bg-white border border-slate-200/80 rounded-2xl p-4 transition-all duration-150 shadow-xs hover:shadow-sm hover:border-slate-300 ${
        onClick ? 'cursor-pointer' : ''
      }`}
    >
      <div className="flex items-center justify-between gap-3">
        {/* Left: Icon & Details */}
        <div className="flex items-center gap-3.5 min-w-0">
          <div
            className={`w-11 h-11 rounded-full flex items-center justify-center shrink-0 ${iconBgColor}`}
          >
            {icon}
          </div>

          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-bold text-slate-900 text-sm sm:text-base">
                {transaction.provider}
              </span>

              {/* Category chip */}
              {transaction.category && transaction.category !== 'Other' && (
                <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200/60">
                  {transaction.category}
                </span>
              )}

              {/* Needs Review Pill */}
              {!transaction.isVerified && (
                <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-bold bg-amber-50 text-amber-800 border border-amber-200">
                  Dib-u-eegis
                </span>
              )}
            </div>

            <p className="text-xs sm:text-sm text-slate-500 truncate mt-0.5">
              {displaySubtitle}
            </p>
          </div>
        </div>

        {/* Right: Amount, Date & Actions */}
        <div className="flex items-center gap-2 shrink-0">
          <div className="text-right">
            <div className={`font-extrabold text-base sm:text-lg tracking-tight ${amountColor}`}>
              {prefix}${transaction.amount.toFixed(2)}
            </div>
            <div className="text-xs text-slate-400 flex items-center justify-end gap-1 mt-0.5">
              <Calendar className="w-3 h-3" />
              <span>{formattedDate}</span>
            </div>
          </div>

          {onDelete && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onDelete();
              }}
              className="opacity-60 hover:opacity-100 p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors ml-1"
              title="Delete transaction"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
