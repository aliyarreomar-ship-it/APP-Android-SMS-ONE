import React, { useState } from 'react';
import { useTransactions } from '../context/TransactionContext';
import {
  X,
  CheckCircle2,
  Copy,
  Check,
  Trash2,
  Calendar,
  Building,
  Tag,
  Hash,
  User,
  Wallet,
  FileText,
} from 'lucide-react';

export const TransactionDetailModal: React.FC = () => {
  const { selectedDetailTx, setSelectedDetailTx, deleteTransaction, showToast } =
    useTransactions();
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);
  const [copied, setCopied] = useState(false);

  if (!selectedDetailTx) return null;

  const isIncome = selectedDetailTx.transactionType === 'INCOME';
  const isAirtime = selectedDetailTx.transactionType === 'AIRTIME';
  const isExpense =
    selectedDetailTx.transactionType === 'EXPENSE' ||
    selectedDetailTx.transactionType === 'BILL_PAYMENT';

  const amountColor = isIncome
    ? 'text-emerald-700'
    : isAirtime
    ? 'text-teal-700'
    : 'text-red-700';

  const prefix = isIncome ? '+' : isExpense || isAirtime ? '-' : '';

  const handleCopySms = () => {
    navigator.clipboard.writeText(selectedDetailTx.originalSms);
    setCopied(true);
    showToast('Fariinta waa la koobiyeeyay (Copied to clipboard)', 'info');
    setTimeout(() => setCopied(false), 2500);
  };

  const handleDelete = () => {
    deleteTransaction(selectedDetailTx.id);
    setSelectedDetailTx(null);
  };

  const formattedDate = new Date(selectedDetailTx.transactionDate).toLocaleString('so-SO', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <h2 className="text-base font-extrabold text-slate-900">
            Faahfaahinta Xisaabta
          </h2>
          <button
            onClick={() => setSelectedDetailTx(null)}
            className="p-1.5 rounded-full text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Receipt Area */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Receipt Top Section */}
          <div className="text-center space-y-2">
            <div className="inline-flex p-3 rounded-2xl bg-emerald-50 text-emerald-600 mb-1">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
              {isIncome
                ? 'Lacag Laguugu Soo Diray'
                : isAirtime
                ? 'Kuuboon / Airtime'
                : 'Lacag Aad Dirtay'}
            </div>

            <div className={`text-4xl sm:text-5xl font-black tracking-tight ${amountColor}`}>
              {prefix}${selectedDetailTx.amount.toFixed(2)}
            </div>
          </div>

          {/* Key Value Details */}
          <div className="bg-slate-50/80 rounded-2xl p-4 border border-slate-100 divide-y divide-slate-200/60 text-sm">
            <div className="flex justify-between py-2.5 items-center">
              <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                <Building className="w-4 h-4 text-slate-400" />
                Shirkadda (Provider)
              </span>
              <span className="font-bold text-slate-900">{selectedDetailTx.provider}</span>
            </div>

            <div className="flex justify-between py-2.5 items-center">
              <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                <Tag className="w-4 h-4 text-slate-400" />
                Nooca Xisaabta
              </span>
              <span className="font-semibold text-slate-800">{selectedDetailTx.transactionType}</span>
            </div>

            <div className="flex justify-between py-2.5 items-center">
              <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                <Calendar className="w-4 h-4 text-slate-400" />
                Taariikhda & Waqtiga
              </span>
              <span className="font-semibold text-slate-800 text-right">{formattedDate}</span>
            </div>

            {selectedDetailTx.referenceId && (
              <div className="flex justify-between py-2.5 items-center">
                <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                  <Hash className="w-4 h-4 text-slate-400" />
                  Tixraaca (Ref ID)
                </span>
                <span className="font-mono font-bold text-emerald-700">
                  {selectedDetailTx.referenceId}
                </span>
              </div>
            )}

            {isIncome && selectedDetailTx.senderNumber && (
              <div className="flex justify-between py-2.5 items-center">
                <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                  <User className="w-4 h-4 text-slate-400" />
                  Qofka Soo Diray
                </span>
                <span className="font-bold text-slate-900">{selectedDetailTx.senderNumber}</span>
              </div>
            )}

            {!isIncome && selectedDetailTx.receiverNumber && (
              <div className="flex justify-between py-2.5 items-center">
                <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                  <User className="w-4 h-4 text-slate-400" />
                  Qofka Loo Diray
                </span>
                <span className="font-bold text-slate-900">{selectedDetailTx.receiverNumber}</span>
              </div>
            )}

            {selectedDetailTx.category && (
              <div className="flex justify-between py-2.5 items-center">
                <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                  <Tag className="w-4 h-4 text-slate-400" />
                  Qaybta (Category)
                </span>
                <span className="px-2.5 py-0.5 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-800 border border-emerald-200/80">
                  {selectedDetailTx.category}
                </span>
              </div>
            )}

            {selectedDetailTx.balance !== undefined && (
              <div className="flex justify-between py-2.5 items-center">
                <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                  <Wallet className="w-4 h-4 text-slate-400" />
                  Haraaga Ka Dambeeyay
                </span>
                <span className="font-extrabold text-slate-900">
                  ${selectedDetailTx.balance.toFixed(2)}
                </span>
              </div>
            )}

            {selectedDetailTx.notes && (
              <div className="flex justify-between py-2.5 items-start">
                <span className="text-slate-500 flex items-center gap-1.5 font-medium">
                  <FileText className="w-4 h-4 text-slate-400" />
                  Xusid (Notes)
                </span>
                <span className="font-medium text-slate-800 text-right max-w-xs">
                  {selectedDetailTx.notes}
                </span>
              </div>
            )}
          </div>

          {/* Original SMS Box */}
          <div className="bg-slate-100/80 rounded-2xl p-4 border border-slate-200/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Fariinta Asalka Ah (Original SMS)
              </span>
              <button
                onClick={handleCopySms}
                className="flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-semibold text-slate-600 hover:text-emerald-700 hover:bg-slate-200 transition-colors"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Koobiyeeyay' : 'Koobiyeey'}</span>
              </button>
            </div>
            <p className="text-xs font-mono text-slate-700 leading-relaxed break-words bg-white/70 p-3 rounded-xl border border-slate-200/60">
              {selectedDetailTx.originalSms}
            </p>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="p-4 sm:p-6 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between gap-3">
          {showConfirmDelete ? (
            <div className="w-full flex items-center gap-2 animate-in fade-in">
              <span className="text-xs font-bold text-red-600 flex-1">
                Ma hubtaa inaad tirtirto?
              </span>
              <button
                onClick={() => setShowConfirmDelete(false)}
                className="px-3 py-2 rounded-xl text-xs font-semibold bg-slate-200 text-slate-700 hover:bg-slate-300"
              >
                Ka Noqo
              </button>
              <button
                onClick={handleDelete}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-red-600 text-white hover:bg-red-700"
              >
                Haa, Tir
              </button>
            </div>
          ) : (
            <>
              <button
                onClick={() => setShowConfirmDelete(true)}
                className="px-4 py-2.5 rounded-xl text-sm font-semibold text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2"
              >
                <Trash2 className="w-4 h-4" />
                <span>Tir Xisaabtan (Delete)</span>
              </button>

              <button
                onClick={() => setSelectedDetailTx(null)}
                className="px-5 py-2.5 rounded-xl text-sm font-bold bg-slate-900 text-white hover:bg-slate-800 transition-colors"
              >
                Xir (Close)
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
