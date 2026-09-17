import React from 'react';
import { useTransactions, AppTab } from '../context/TransactionContext';
import {
  LayoutDashboard,
  ReceiptText,
  Clock,
  BarChart3,
  FileSpreadsheet,
  ScanText,
  Settings,
  PlusCircle,
  MinusCircle,
  Zap,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const {
    activeTab,
    setActiveTab,
    unverifiedTransactions,
    processSyntheticSms,
  } = useTransactions();

  const navItems: { tab: AppTab; label: string; somaliLabel: string; icon: React.ReactNode; badge?: number }[] = [
    {
      tab: 'dashboard',
      label: 'Dashboard',
      somaliLabel: 'Guudmar',
      icon: <LayoutDashboard className="w-4 h-4" />,
    },
    {
      tab: 'transactions',
      label: 'Transactions',
      somaliLabel: 'Xisaabaadka',
      icon: <ReceiptText className="w-4 h-4" />,
    },
    {
      tab: 'review',
      label: 'Needs Review',
      somaliLabel: 'Dib-u-eegis',
      icon: <Clock className="w-4 h-4" />,
      badge: unverifiedTransactions.length,
    },
    {
      tab: 'analytics',
      label: 'Analytics',
      somaliLabel: 'Falanqaynta',
      icon: <BarChart3 className="w-4 h-4" />,
    },
    {
      tab: 'reports',
      label: 'Reports',
      somaliLabel: 'Warbixino',
      icon: <FileSpreadsheet className="w-4 h-4" />,
    },
    {
      tab: 'scanner',
      label: 'SMS Scanner',
      somaliLabel: 'Akhri SMS',
      icon: <ScanText className="w-4 h-4" />,
    },
    {
      tab: 'settings',
      label: 'Settings',
      somaliLabel: 'Habaynta',
      icon: <Settings className="w-4 h-4" />,
    },
  ];

  const handleTestEvcIncome = () => {
    const testRef = 'EVC' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EVCPlus',
      `[+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX MAXAMED (615887766). Haraagaagu waa $185.00. Ref:${testRef}. Tar: 16/09/26 19:10:00`,
      true
    );
  };

  const handleTestEvcExpense = () => {
    const testRef = 'EVC' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EVCPlus',
      `[-$15.00] Waad u dirtay DUKAANKA CUNTADA (612334455). Haraagaagu waa $170.00. Ref:${testRef}. Tar: 16/09/26 19:12:00`,
      true
    );
  };

  const handleTestEDahab = () => {
    const testRef = 'EDH' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EDAHAB',
      `Waxaad heshay $40.00 ka heshay AXMED ROOBLE (659911223). Haraagaagu waa $210.00. Tarjumaha: ${testRef}`,
      true
    );
  };

  return (
    <header className="sticky top-0 z-30 bg-white border-b border-slate-200/80 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-black text-lg shadow-sm ring-2 ring-emerald-500/20">
              $
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-slate-900 tracking-tight text-lg">SMS Reader Pro</span>
                <span className="hidden sm:inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  v2.0 Smart
                </span>
              </div>
              <p className="text-xs text-slate-500 font-medium hidden md:block">
                Somali Mobile-Money Tracker • EVC Plus & E-Dahab
              </p>
            </div>
          </div>

          {/* Quick SMS Simulators */}
          <div className="hidden lg:flex items-center gap-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1">
              <Zap className="w-3.5 h-3.5 text-amber-500" /> Tijaabi SMS:
            </span>
            <button
              onClick={handleTestEvcIncome}
              className="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200 transition-colors flex items-center gap-1"
              title="Test EVC Plus Income SMS"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              + $35 EVC
            </button>
            <button
              onClick={handleTestEvcExpense}
              className="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition-colors flex items-center gap-1"
              title="Test EVC Plus Expense SMS"
            >
              <MinusCircle className="w-3.5 h-3.5" />
              - $15 EVC
            </button>
            <button
              onClick={handleTestEDahab}
              className="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-amber-50 text-amber-800 hover:bg-amber-100 border border-amber-200 transition-colors flex items-center gap-1"
              title="Test E-Dahab Income SMS"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              + $40 E-Dahab
            </button>
          </div>
        </div>

        {/* Navigation Tabs Bar */}
        <nav className="flex space-x-1 sm:space-x-2 overflow-x-auto pb-2 scrollbar-none">
          {navItems.map((item) => {
            const isActive = activeTab === item.tab;
            return (
              <button
                key={item.tab}
                onClick={() => setActiveTab(item.tab)}
                className={`relative flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-semibold whitespace-nowrap transition-all ${
                  isActive
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                }`}
              >
                {item.icon}
                <span>{item.somaliLabel}</span>
                <span className="hidden md:inline text-xs opacity-75">({item.label})</span>

                {item.badge !== undefined && item.badge > 0 && (
                  <span
                    className={`ml-1 px-1.5 py-0.2 rounded-full text-xs font-bold ${
                      isActive ? 'bg-white text-emerald-700' : 'bg-amber-500 text-white'
                    }`}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>
    </header>
  );
};
