import React from 'react';
import { TransactionProvider, useTransactions } from './context/TransactionContext';
import { Navbar } from './components/Navbar';
import { DashboardView } from './views/DashboardView';
import { TransactionsView } from './views/TransactionsView';
import { ReviewView } from './views/ReviewView';
import { AnalyticsView } from './views/AnalyticsView';
import { ReportsView } from './views/ReportsView';
import { SmsScannerView } from './views/SmsScannerView';
import { SettingsView } from './views/SettingsView';
import { TransactionPopupModal } from './components/TransactionPopupModal';
import { TransactionDetailModal } from './components/TransactionDetailModal';
import { CheckCircle2, AlertCircle, Info } from 'lucide-react';

const MainAppContent: React.FC = () => {
  const { activeTab, toast } = useTransactions();

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col selection:bg-emerald-500 selection:text-white">
      {/* Top Navigation */}
      <Navbar />

      {/* Main View Body */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 pt-6">
        {activeTab === 'dashboard' && <DashboardView />}
        {activeTab === 'transactions' && <TransactionsView />}
        {activeTab === 'review' && <ReviewView />}
        {activeTab === 'analytics' && <AnalyticsView />}
        {activeTab === 'reports' && <ReportsView />}
        {activeTab === 'scanner' && <SmsScannerView />}
        {activeTab === 'settings' && <SettingsView />}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200/70 bg-white py-6 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-slate-400">
          <div>
            <strong className="text-slate-700">SMS Reader Pro</strong> • Private personal tracker for Somali mobile money transactions
          </div>
          <div className="font-medium">
            Version 2.0 Pro • Automatic & Smart Engine
          </div>
        </div>
      </footer>

      {/* Global Modals & Dialogs */}
      <TransactionPopupModal />
      <TransactionDetailModal />

      {/* Toast Notification */}
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 animate-in slide-in-from-bottom-3 duration-200">
          <div
            className={`flex items-center gap-2.5 px-4 py-3 rounded-2xl shadow-xl border text-sm font-semibold ${
              toast.type === 'success'
                ? 'bg-emerald-950 text-emerald-200 border-emerald-800'
                : toast.type === 'error'
                ? 'bg-red-950 text-red-200 border-red-800'
                : 'bg-slate-900 text-slate-200 border-slate-800'
            }`}
          >
            {toast.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            ) : toast.type === 'error' ? (
              <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
            ) : (
              <Info className="w-4 h-4 text-blue-400 shrink-0" />
            )}
            <span>{toast.text}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <TransactionProvider>
      <MainAppContent />
    </TransactionProvider>
  );
};

export default App;
