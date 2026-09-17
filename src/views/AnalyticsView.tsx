import React, { useMemo } from 'react';
import { useTransactions } from '../context/TransactionContext';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import {
  BarChart3,
  TrendingUp,
  TrendingDown,
  Percent,
  Layers,
  ArrowRight,
} from 'lucide-react';

const COLORS = [
  '#059669', // Emerald
  '#2563eb', // Blue
  '#d97706', // Amber
  '#dc2626', // Red
  '#7c3aed', // Purple
  '#0d9488', // Teal
  '#e11d48', // Rose
  '#4b5563', // Gray
];

export const AnalyticsView: React.FC = () => {
  const { transactions, totalIncome, totalExpense, netBalance } = useTransactions();

  // Category breakdown
  const categoryData = useMemo(() => {
    const map: { [cat: string]: number } = {};
    transactions
      .filter((t) => t.transactionType !== 'INCOME')
      .forEach((t) => {
        const cat = t.category || 'Other';
        map[cat] = (map[cat] || 0) + t.amount;
      });

    return Object.entries(map)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value);
  }, [transactions]);

  // Provider breakdown
  const providerData = useMemo(() => {
    const map: { [p: string]: { income: number; expense: number; count: number } } = {};
    transactions.forEach((t) => {
      const p = t.provider || 'Other';
      if (!map[p]) map[p] = { income: 0, expense: 0, count: 0 };
      map[p].count += 1;
      if (t.transactionType === 'INCOME') {
        map[p].income += t.amount;
      } else {
        map[p].expense += t.amount;
      }
    });

    return Object.entries(map).map(([name, data]) => ({
      name,
      income: data.income,
      expense: data.expense,
      count: data.count,
    }));
  }, [transactions]);

  // Overview metrics
  const avgAmount = useMemo(() => {
    if (transactions.length === 0) return 0;
    const total = transactions.reduce((acc, t) => acc + t.amount, 0);
    return total / transactions.length;
  }, [transactions]);

  const highestIncome = useMemo(() => {
    const incomes = transactions.filter((t) => t.transactionType === 'INCOME');
    return incomes.length > 0 ? Math.max(...incomes.map((t) => t.amount)) : 0;
  }, [transactions]);

  const highestExpense = useMemo(() => {
    const expenses = transactions.filter((t) => t.transactionType !== 'INCOME');
    return expenses.length > 0 ? Math.max(...expenses.map((t) => t.amount)) : 0;
  }, [transactions]);

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">
          Falanqaynta Lacageed (Analytics)
        </h1>
        <p className="text-xs sm:text-sm text-slate-500">
          Shaxanka dakhliga, kharashka, iyo dhaqdhaqaaqa shirkadaha
        </p>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3.5">
        <div className="bg-white p-5 rounded-3xl border border-slate-200/80 shadow-xs space-y-1">
          <span className="text-xs font-bold text-slate-400 uppercase">
            Tirada Guud
          </span>
          <div className="text-2xl font-black text-slate-900">
            {transactions.length}
          </div>
          <p className="text-xs text-slate-400">Xisaabaad la diiwaangeliyay</p>
        </div>

        <div className="bg-white p-5 rounded-3xl border border-slate-200/80 shadow-xs space-y-1">
          <span className="text-xs font-bold text-slate-400 uppercase">
            Celceliska Xisaabta
          </span>
          <div className="text-2xl font-black text-slate-900">
            ${avgAmount.toFixed(2)}
          </div>
          <p className="text-xs text-slate-400">Hal fariin celcelis ahaan</p>
        </div>

        <div className="bg-white p-5 rounded-3xl border border-slate-200/80 shadow-xs space-y-1">
          <span className="text-xs font-bold text-emerald-600 uppercase">
            Dakhliga Ugu Weyn
          </span>
          <div className="text-2xl font-black text-emerald-700">
            +${highestIncome.toFixed(2)}
          </div>
          <p className="text-xs text-slate-400">Hal mar la helay</p>
        </div>

        <div className="bg-white p-5 rounded-3xl border border-slate-200/80 shadow-xs space-y-1">
          <span className="text-xs font-bold text-red-600 uppercase">
            Kharashka Ugu Weyn
          </span>
          <div className="text-2xl font-black text-red-700">
            -${highestExpense.toFixed(2)}
          </div>
          <p className="text-xs text-slate-400">Hal mar la bixiyay</p>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chart 1: Provider Comparison */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-extrabold text-slate-900">
                Shirkadaha & Mugga Lacagaha
              </h2>
              <p className="text-xs text-slate-500">
                Dakhliga iyo Kharashka shirkad kasta (EVC Plus, E-Dahab, Jeeb...)
              </p>
            </div>
            <Layers className="w-5 h-5 text-emerald-600" />
          </div>

          <div className="h-64 w-full">
            {providerData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={providerData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip
                    formatter={(value: any) => [`$${Number(value).toFixed(2)}`, '']}
                    contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0' }}
                  />
                  <Legend />
                  <Bar dataKey="income" name="Dakhli (+)" fill="#059669" radius={[6, 6, 0, 0]} />
                  <Bar dataKey="expense" name="Kharash (-)" fill="#dc2626" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-xs">
                Xog kuma filna shaxanka
              </div>
            )}
          </div>
        </div>

        {/* Chart 2: Category Breakdown */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-extrabold text-slate-900">
                Kharashaadka Qaybaha (Categories)
              </h2>
              <p className="text-xs text-slate-500">
                Kala qaybsanaanta kharashka: Bajaaj, Cunto, Koronto, Dukaan...
              </p>
            </div>
            <BarChart3 className="w-5 h-5 text-emerald-600" />
          </div>

          <div className="h-64 w-full">
            {categoryData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {categoryData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(value: any) => [`$${Number(value).toFixed(2)}`, 'Lacagta']}
                    contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0' }}
                  />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-xs">
                Kharashaad la duubay ma jiraan
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Category Breakdown Table */}
      {categoryData.length > 0 && (
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
          <h2 className="text-base font-extrabold text-slate-900">
            Jadwalka Faahfaahsan ee Qaybaha
          </h2>

          <div className="divide-y divide-slate-100">
            {categoryData.map((item, index) => {
              const pct = totalExpense > 0 ? (item.value / totalExpense) * 100 : 0;
              return (
                <div key={item.name} className="py-3 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <span
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: COLORS[index % COLORS.length] }}
                    />
                    <span className="font-bold text-slate-800 text-sm">{item.name}</span>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="w-24 bg-slate-100 h-2 rounded-full hidden sm:block overflow-hidden">
                      <div
                        className="h-full rounded-full"
                        style={{
                          width: `${pct}%`,
                          backgroundColor: COLORS[index % COLORS.length],
                        }}
                      />
                    </div>
                    <span className="text-xs font-mono text-slate-500 w-10 text-right">
                      {pct.toFixed(1)}%
                    </span>
                    <span className="font-extrabold text-slate-900 text-sm w-20 text-right">
                      ${item.value.toFixed(2)}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
