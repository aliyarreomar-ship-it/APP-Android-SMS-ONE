import React, { useState, useMemo } from 'react';
import { useTransactions } from '../context/TransactionContext';
import {
  FileSpreadsheet,
  Download,
  Printer,
  Calendar,
  DollarSign,
  TrendingUp,
  TrendingDown,
  FileText,
} from 'lucide-react';

export const ReportsView: React.FC = () => {
  const { transactions, showToast } = useTransactions();

  const [selectedMonth, setSelectedMonth] = useState<string>(() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  });

  // Unique months available
  const availableMonths = useMemo(() => {
    const set = new Set<string>();
    transactions.forEach((t) => {
      const d = new Date(t.transactionDate);
      set.add(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
    });
    // always add current month
    const d = new Date();
    set.add(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
    return Array.from(set).sort().reverse();
  }, [transactions]);

  // Filtered transactions for selected month
  const monthTransactions = useMemo(() => {
    return transactions.filter((t) => {
      const d = new Date(t.transactionDate);
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      return key === selectedMonth;
    });
  }, [transactions, selectedMonth]);

  const monthIncome = useMemo(() => {
    return monthTransactions
      .filter((t) => t.transactionType === 'INCOME')
      .reduce((acc, t) => acc + t.amount, 0);
  }, [monthTransactions]);

  const monthExpense = useMemo(() => {
    return monthTransactions
      .filter((t) => t.transactionType !== 'INCOME')
      .reduce((acc, t) => acc + t.amount, 0);
  }, [monthTransactions]);

  const monthNet = monthIncome - monthExpense;

  // Export CSV
  const handleExportCsv = () => {
    if (transactions.length === 0) {
      showToast('Ma jiraan xisaabaad la dhoofin karo', 'error');
      return;
    }

    const headers = [
      'ID',
      'Date & Time',
      'Provider',
      'Type',
      'Amount (USD)',
      'Reference ID',
      'Sender / Receiver',
      'Category',
      'Notes',
      'Post Balance (USD)',
      'Verified',
      'Raw SMS',
    ];

    const rows = transactions.map((t) => [
      t.id,
      `"${new Date(t.transactionDate).toISOString()}"`,
      `"${t.provider}"`,
      `"${t.transactionType}"`,
      t.amount.toFixed(2),
      `"${t.referenceId || ''}"`,
      `"${t.senderNumber || t.receiverNumber || ''}"`,
      `"${t.category || ''}"`,
      `"${t.notes?.replace(/"/g, '""') || ''}"`,
      t.balance !== undefined ? t.balance.toFixed(2) : '',
      t.isVerified ? 'YES' : 'NO',
      `"${t.originalSms.replace(/"/g, '""')}"`,
    ]);

    const csvContent =
      'data:text/csv;charset=utf-8,' +
      [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute(
      'download',
      `sms_reader_pro_report_${new Date().toISOString().slice(0, 10)}.csv`
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast('Warbixinta CSV-ga waa la soo degsaday!', 'success');
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            Warbixino & Bayaan Lacageed (Reports)
          </h1>
          <p className="text-xs sm:text-sm text-slate-500">
            Dhoofso oo daabac bayaanka xisaabaadkaaga bil kasta
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleExportCsv}
            className="px-4 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            Dhoofso CSV
          </button>
          <button
            onClick={handlePrint}
            className="px-4 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-slate-100 hover:bg-slate-200 text-slate-800 transition-colors flex items-center gap-2"
          >
            <Printer className="w-4 h-4" />
            Daabac (Print)
          </button>
        </div>
      </div>

      {/* Month Selector Card */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <Calendar className="w-5 h-5 text-emerald-600" />
            <span className="font-extrabold text-slate-900 text-base">
              Dooro Bisha (Select Statement Period):
            </span>
          </div>

          <div className="flex flex-wrap gap-2">
            {availableMonths.map((m) => {
              const [year, month] = m.split('-');
              const dateObj = new Date(parseInt(year), parseInt(month) - 1, 1);
              const label = dateObj.toLocaleDateString('so-SO', {
                month: 'long',
                year: 'numeric',
              });

              return (
                <button
                  key={m}
                  onClick={() => setSelectedMonth(m)}
                  className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
                    selectedMonth === m
                      ? 'bg-emerald-600 text-white shadow-xs'
                      : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                  }`}
                >
                  {label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Month Summary Bar */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
          <div className="bg-emerald-50/70 p-4 rounded-2xl border border-emerald-100 space-y-1">
            <span className="text-xs font-bold text-emerald-800 uppercase flex items-center gap-1">
              <TrendingUp className="w-3.5 h-3.5" /> Dakhliga Bisha
            </span>
            <div className="text-2xl font-black text-emerald-700">
              +${monthIncome.toFixed(2)}
            </div>
          </div>

          <div className="bg-red-50/70 p-4 rounded-2xl border border-red-100 space-y-1">
            <span className="text-xs font-bold text-red-800 uppercase flex items-center gap-1">
              <TrendingDown className="w-3.5 h-3.5" /> Kharashka Bisha
            </span>
            <div className="text-2xl font-black text-red-700">
              -${monthExpense.toFixed(2)}
            </div>
          </div>

          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 space-y-1">
            <span className="text-xs font-bold text-slate-600 uppercase flex items-center gap-1">
              <DollarSign className="w-3.5 h-3.5" /> Farqiga (Net Savings)
            </span>
            <div
              className={`text-2xl font-black ${
                monthNet >= 0 ? 'text-slate-900' : 'text-red-600'
              }`}
            >
              {monthNet >= 0 ? '+' : '-'}${Math.abs(monthNet).toFixed(2)}
            </div>
          </div>
        </div>
      </div>

      {/* Month Ledger Statement Table */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-extrabold text-slate-900">
            Bayaanka Faahfaahsan ee Bisha ({monthTransactions.length} xisaabood)
          </h2>
        </div>

        {monthTransactions.length === 0 ? (
          <div className="py-12 text-center text-slate-400 space-y-2">
            <p className="text-sm font-semibold">Ma jiraan xisaabaad bishan la duubay.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-bold text-slate-500 uppercase">
                <tr>
                  <th className="py-3 px-4">Taariikhda</th>
                  <th className="py-3 px-4">Shirkadda</th>
                  <th className="py-3 px-4">Qofka / Tixraaca</th>
                  <th className="py-3 px-4">Qaybta</th>
                  <th className="py-3 px-4 text-right">Lacagta</th>
                  <th className="py-3 px-4 text-right">Haraaga</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {monthTransactions.map((t) => {
                  const isInc = t.transactionType === 'INCOME';
                  return (
                    <tr key={t.id} className="hover:bg-slate-50/50">
                      <td className="py-3 px-4 text-xs font-medium text-slate-600 whitespace-nowrap">
                        {new Date(t.transactionDate).toLocaleDateString('en-GB', {
                          day: '2-digit',
                          month: 'short',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </td>
                      <td className="py-3 px-4 font-bold text-slate-900">
                        {t.provider}
                      </td>
                      <td className="py-3 px-4 text-xs text-slate-600">
                        <div>{t.senderNumber || t.receiverNumber || 'N/A'}</div>
                        {t.referenceId && (
                          <span className="font-mono text-slate-400">
                            Ref: {t.referenceId}
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-xs">
                        <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 font-medium">
                          {t.category || 'Other'}
                        </span>
                      </td>
                      <td
                        className={`py-3 px-4 text-right font-bold ${
                          isInc ? 'text-emerald-700' : 'text-red-700'
                        }`}
                      >
                        {isInc ? '+' : '-'}${t.amount.toFixed(2)}
                      </td>
                      <td className="py-3 px-4 text-right font-extrabold text-slate-800">
                        {t.balance !== undefined ? `$${t.balance.toFixed(2)}` : '-'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
