import React, { useState } from 'react';
import { useTransactions } from '../context/TransactionContext';
import {
  X,
  Check,
  ArrowDownLeft,
  ArrowUpRight,
  Plus,
  Receipt,
  Tag,
  FileText,
} from 'lucide-react';

const SOMALI_CATEGORIES = [
  'Bajaaj',
  'Cunto',
  'Dukaan',
  'Kiro',
  'Koronto',
  'Biyo',
  'Gas',
  'TV',
  'Nadaafad',
  'Ku hadal Taleefan',
  'Internet',
  'Shaah',
];

export const TransactionPopupModal: React.FC = () => {
  const { pendingPopupTx, dismissPopup, savePopup } = useTransactions();

  const [selectedCategory, setSelectedCategory] = useState<string>(() => {
    if (
      pendingPopupTx?.category &&
      pendingPopupTx.category !== 'Other' &&
      pendingPopupTx.category !== 'Guud'
    ) {
      return pendingPopupTx.category;
    }
    return 'Cunto';
  });

  const [isCustom, setIsCustom] = useState(false);
  const [customCategory, setCustomCategory] = useState('');
  const [note, setNote] = useState('');

  if (!pendingPopupTx) return null;

  const isIncome = pendingPopupTx.transactionType === 'INCOME';
  const isEvc =
    pendingPopupTx.provider.toLowerCase().includes('evc') ||
    pendingPopupTx.provider.toLowerCase().includes('hormuud');

  const brandTitle = isEvc ? 'EVC PLUS (192)' : 'E-DAHAB (898)';
  const flowTitle = isIncome
    ? 'XAWILAAD LAGUU SOO DIRAY'
    : 'WAXAAD U DIRTAY LACAG';

  const brandColorClass = isIncome
    ? 'bg-emerald-600 text-white'
    : isEvc
    ? 'bg-amber-600 text-white'
    : 'bg-red-600 text-white';

  const brandTextColorClass = isIncome
    ? 'text-emerald-700'
    : isEvc
    ? 'text-amber-700'
    : 'text-red-700';

  const counterparty =
    pendingPopupTx.senderNumber || pendingPopupTx.receiverNumber;

  const handleSave = () => {
    const finalCategory =
      isCustom && customCategory.trim()
        ? customCategory.trim()
        : selectedCategory;
    savePopup(finalCategory, note.trim());
  };

  const currentChosenCategory =
    isCustom && customCategory.trim()
      ? customCategory.trim()
      : selectedCategory;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[92vh]">
        {/* Top Header Bar */}
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-xs font-black uppercase tracking-wider text-slate-700">
              {brandTitle}
            </span>
          </div>

          <button
            onClick={dismissPopup}
            className="p-1.5 rounded-full text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="p-6 overflow-y-auto space-y-5">
          {/* Visual flow & Amount */}
          <div className="text-center space-y-2">
            <div className="inline-flex p-3 rounded-2xl bg-slate-100 mb-1">
              {isIncome ? (
                <ArrowDownLeft className="w-8 h-8 text-emerald-600" />
              ) : (
                <ArrowUpRight className="w-8 h-8 text-red-600" />
              )}
            </div>

            <div className={`text-xs font-black tracking-wider uppercase ${brandTextColorClass}`}>
              {flowTitle}
            </div>

            <div className={`text-4xl sm:text-5xl font-black tracking-tight ${brandTextColorClass}`}>
              {isIncome ? '+' : '-'}${pendingPopupTx.amount.toFixed(2)}
            </div>
          </div>

          {/* Quick Details Box */}
          <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100 space-y-2.5 text-xs sm:text-sm">
            {counterparty && (
              <div className="flex justify-between items-center">
                <span className="text-slate-500 font-medium">
                  {isIncome ? 'Qofka soo diray:' : 'Qofka loo diray:'}
                </span>
                <span className="font-bold text-slate-900 text-right">
                  {counterparty}
                </span>
              </div>
            )}

            {pendingPopupTx.balance !== undefined && (
              <div className="flex justify-between items-center">
                <span className="text-slate-500 font-medium">Haraagaaga cusub:</span>
                <span className="font-extrabold text-emerald-700">
                  ${pendingPopupTx.balance.toFixed(2)}
                </span>
              </div>
            )}

            {pendingPopupTx.referenceId && (
              <div className="flex justify-between items-center">
                <span className="text-slate-500 font-medium">Tixraaca (Ref ID):</span>
                <span className="font-mono font-bold text-slate-700">
                  {pendingPopupTx.referenceId}
                </span>
              </div>
            )}
          </div>

          {/* Categorization Prompt */}
          <div className="space-y-3">
            <div>
              <h3 className="text-base font-extrabold text-slate-900 flex items-center gap-1.5">
                <Tag className="w-4 h-4 text-emerald-600" />
                Maxaan ku diiwaangeliyaa?
              </h3>
              <p className="text-xs text-slate-500">
                Dooro mid ka mid ah qaybaha hoose ama qor wax cusub:
              </p>
            </div>

            {/* Somali Category Chips Grid */}
            <div className="flex flex-wrap gap-2">
              {SOMALI_CATEGORIES.map((cat) => {
                const isSelected = !isCustom && selectedCategory === cat;
                return (
                  <button
                    key={cat}
                    onClick={() => {
                      setIsCustom(false);
                      setSelectedCategory(cat);
                    }}
                    className={`px-3 py-1.5 rounded-xl text-xs sm:text-sm font-semibold transition-all flex items-center gap-1.5 ${
                      isSelected
                        ? `${brandColorClass} shadow-xs scale-102`
                        : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                    }`}
                  >
                    {isSelected && <Check className="w-3.5 h-3.5" />}
                    {cat}
                  </button>
                );
              })}

              <button
                onClick={() => setIsCustom(true)}
                className={`px-3 py-1.5 rounded-xl text-xs sm:text-sm font-semibold transition-all flex items-center gap-1 ${
                  isCustom
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200'
                }`}
              >
                <Plus className="w-3.5 h-3.5" />
                Wax cusub
              </button>
            </div>

            {/* Custom Category Input */}
            {isCustom && (
              <div className="pt-2 animate-in fade-in duration-150">
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Qor magaca qaybta cusub:
                </label>
                <input
                  type="text"
                  value={customCategory}
                  onChange={(e) => setCustomCategory(e.target.value)}
                  placeholder="Tus: Dhismaha guriga, Maalgashi..."
                  className="w-full px-3.5 py-2 rounded-xl text-sm border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
                  autoFocus
                />
              </div>
            )}

            {/* Optional Note Field */}
            <div className="pt-1">
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <FileText className="w-3.5 h-3.5 text-slate-400" />
                Faahfaahin / Xusid gaar ah (Optional):
              </label>
              <input
                type="text"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Tus: Lacagtii heshiiska, shaaha shalay..."
                className="w-full px-3.5 py-2 rounded-xl text-sm border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
              />
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="p-4 sm:p-6 border-t border-slate-100 bg-slate-50/50 flex flex-col sm:flex-row gap-2.5">
          <button
            onClick={dismissPopup}
            className="w-full sm:w-auto px-5 py-3 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-200/70 transition-colors order-2 sm:order-1"
          >
            Dib u dhig (Later)
          </button>

          <button
            onClick={handleSave}
            className={`w-full sm:flex-1 px-6 py-3 rounded-xl text-sm font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 text-white order-1 sm:order-2 ${brandColorClass}`}
          >
            <Check className="w-4.5 h-4.5" />
            Diiwaangeli ({currentChosenCategory})
          </button>
        </div>
      </div>
    </div>
  );
};
