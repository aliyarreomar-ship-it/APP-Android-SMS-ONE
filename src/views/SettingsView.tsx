import React, { useState } from 'react';
import { useTransactions } from '../context/TransactionContext';
import {
  Settings,
  Trash2,
  RotateCcw,
  Sparkles,
  ShieldCheck,
  CheckCircle,
  Info,
  Zap,
  Bell,
  AlertTriangle,
  FileCheck,
} from 'lucide-react';

export const SettingsView: React.FC = () => {
  const {
    deleteAllTransactions,
    clearAllHistory,
    clearSmsCache,
    processSyntheticSms,
    showToast,
  } = useTransactions();

  const [showDeleteAllDialog, setShowDeleteAllDialog] = useState(false);
  const [showResetDialog, setShowResetDialog] = useState(false);
  const [showClearCacheDialog, setShowClearCacheDialog] = useState(false);
  const [popupEnabled, setPopupEnabled] = useState(true);

  const handleTestEvcIncome = () => {
    const testRef = 'EVC' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EVCPlus',
      `[+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX MAXAMED (615887766). Haraagaagu waa $185.00. Ref:${testRef}. Tar: 16/09/26 19:10:00`,
      popupEnabled
    );
  };

  const handleTestEvcExpense = () => {
    const testRef = 'EVC' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EVCPlus',
      `[-$15.00] Waad u dirtay DUKAANKA CUNTADA (612334455). Haraagaagu waa $170.00. Ref:${testRef}. Tar: 16/09/26 19:12:00`,
      popupEnabled
    );
  };

  const handleTestEDahab = () => {
    const testRef = 'EDH' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'EDAHAB',
      `Waxaad heshay $40.00 ka heshay AXMED ROOBLE (659911223). Haraagaagu waa $210.00. Tarjumaha: ${testRef}`,
      popupEnabled
    );
  };

  const handleTestJeeb = () => {
    const testRef = 'JB' + Math.floor(1000 + Math.random() * 9000);
    processSyntheticSms(
      'Jeeb',
      `You have received $50.00 from PremierBank to your Jeeb Account. Balance: $250.00. TxId: ${testRef}`,
      popupEnabled
    );
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">
          Habaynta & Maamulka (Settings)
        </h1>
        <p className="text-xs sm:text-sm font-semibold text-emerald-600">
          Version 2.0 Pro • Automatic & Smart Engine
        </p>
      </div>

      {/* Popup Permission / Modal Trigger Settings */}
      <div className="bg-emerald-50/70 border border-emerald-200 rounded-3xl p-6 space-y-3">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h3 className="text-base font-extrabold text-emerald-950 flex items-center gap-2">
              <Bell className="w-5 h-5 text-emerald-700" />
              Soo Bandhigista Popup-ka (Maxaan ku diiwaangeliyaa?)
            </h3>
            <p className="text-xs sm:text-sm text-emerald-800 mt-1">
              Marka fariin SMS ah soo dhacdo ama la tijaabiyo, toos u soo saar daaqadda &quot;Maxaan ku diiwaangeliyaa?&quot; si hal mar loogu qoro qaybta (Bajaaj, Cunto, Dukaan...).
            </p>
          </div>

          <label className="relative inline-flex items-center cursor-pointer shrink-0">
            <input
              type="checkbox"
              checked={popupEnabled}
              onChange={(e) => setPopupEnabled(e.target.checked)}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
          </label>
        </div>
      </div>

      {/* 1. Data Management (Clear History / Clean Slate) */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-red-50 text-red-600 flex items-center justify-center shrink-0">
            <Trash2 className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-extrabold text-slate-900">
              Maamulka Xogta (Data & History)
            </h2>
            <p className="text-xs text-slate-500">
              Tirtir xisaabaadkii hore ama bilaaw cusub (Clean Slate)
            </p>
          </div>
        </div>

        <div className="space-y-2.5 pt-2">
          <button
            onClick={() => setShowDeleteAllDialog(true)}
            className="w-full p-3.5 rounded-2xl text-xs sm:text-sm font-bold bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 transition-colors flex items-center justify-center gap-2"
          >
            <Trash2 className="w-4 h-4" />
            Tir Dhammaan Xisaabaadka (Delete All Transactions)
          </button>

          <button
            onClick={() => setShowResetDialog(true)}
            className="w-full p-3.5 rounded-2xl text-xs sm:text-sm font-bold bg-red-600 hover:bg-red-700 text-white shadow-xs transition-colors flex items-center justify-center gap-2"
          >
            <RotateCcw className="w-4 h-4" />
            Nadiifi App-ka (Full Clean Slate Reset)
          </button>

          <button
            onClick={() => setShowClearCacheDialog(true)}
            className="w-full p-3.5 rounded-2xl text-xs sm:text-sm font-semibold bg-slate-100 hover:bg-slate-200 text-slate-700 transition-colors flex items-center justify-center gap-2"
          >
            <FileCheck className="w-4 h-4" />
            Nadiifi Kaydka SMS-yada (Clear SMS Cache)
          </button>
        </div>
      </div>

      {/* 2. Permissions & Engine Status Card */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-slate-900">
                Oggolaanshaha & Shaandhaynta SMS-ka
              </h2>
              <p className="text-xs font-bold text-emerald-700">
                Dhammaan waa oggol yihiin (Active & Ready)
              </p>
            </div>
          </div>

          <CheckCircle className="w-7 h-7 text-emerald-600 shrink-0" />
        </div>
      </div>

      {/* 3. Engine Intelligence Info (Version 2.0) */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
            <Info className="w-5 h-5" />
          </div>
          <h2 className="text-base font-extrabold text-slate-900">
            Tiknoolijiyada Version 2.0
          </h2>
        </div>

        <div className="space-y-3 divide-y divide-slate-100 text-xs sm:text-sm">
          {[
            {
              title: '100% Automatic Sync',
              desc: 'Fariimaha si toos ah ayaa loo qabtaa marka ay kusoo dhacaan.',
            },
            {
              title: 'Zero Duplicate Engine',
              desc: 'Shaandhayn labalaabasho la’aan ah (Tixraac, Waqti & Lacag).',
            },
            {
              title: 'Smart Auto-Categorization',
              desc: 'Toos u kala soocida Kharashka, Dakhliga, Airtime-ka & Biilasha.',
            },
            {
              title: 'High-Speed Processing',
              desc: 'Soo saaris degdeg ah oo hufan.',
            },
          ].map((item) => (
            <div key={item.title} className="pt-2.5 flex items-start gap-2">
              <span className="text-emerald-600 font-bold">•</span>
              <div>
                <span className="font-bold text-slate-900 block">{item.title}</span>
                <span className="text-slate-500">{item.desc}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 4. Developer Test / Simulation Tools */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <div>
          <h2 className="text-base font-extrabold text-slate-900 flex items-center gap-2">
            <Zap className="w-4 h-4 text-amber-500" />
            Tijaabi Fariin Cusub (Test Simulations)
          </h2>
          <p className="text-xs text-slate-500">
            Ku tijaabi sida app-ku toos ugu qabanayo fariimaha EVC Plus, E-Dahab, iyo Jeeb:
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          <button
            onClick={handleTestEvcIncome}
            className="p-3 rounded-2xl text-xs font-bold bg-emerald-600 hover:bg-emerald-700 text-white transition-colors"
          >
            + Dakhli EVC (+$35.00)
          </button>

          <button
            onClick={handleTestEvcExpense}
            className="p-3 rounded-2xl text-xs font-bold bg-red-600 hover:bg-red-700 text-white transition-colors"
          >
            - Kharash EVC (-$15.00)
          </button>

          <button
            onClick={handleTestEDahab}
            className="p-3 rounded-2xl text-xs font-bold bg-amber-600 hover:bg-amber-700 text-white transition-colors"
          >
            Tijaabi E-Dahab (+ $40.00)
          </button>

          <button
            onClick={handleTestJeeb}
            className="p-3 rounded-2xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white transition-colors"
          >
            Tijaabi Jeeb Bank (+ $50.00)
          </button>
        </div>
      </div>

      {/* Delete All Modal */}
      {showDeleteAllDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl border border-slate-200 space-y-4">
            <h3 className="text-lg font-black text-red-600">
              Tir Dhammaan Xisaabaadka
            </h3>
            <p className="text-xs text-slate-500">
              Ma hubtaa inaad tirtirto dhammaan xisaabaadka ku jira app-ka? Xisaabaadkii hore dhan waa la tirtirayaa.
            </p>
            <div className="flex gap-2 pt-2">
              <button
                onClick={() => setShowDeleteAllDialog(false)}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-slate-100 text-slate-700 hover:bg-slate-200"
              >
                Ka Noqo
              </button>
              <button
                onClick={() => {
                  deleteAllTransactions();
                  setShowDeleteAllDialog(false);
                }}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-red-600 text-white hover:bg-red-700"
              >
                Haa, Tir Dhammaan
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reset Clean Slate Modal */}
      {showResetDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-black text-red-600 text-center">
              Nadiifi App-ka (Full Reset)
            </h3>
            <p className="text-xs text-slate-500 text-center">
              Tani waxay tirtiri doontaa dhammaan xisaabaadka iyo dhammaan fariimihii hore ee ku kaydsanaa app-ka. App-ku wuxuu noqonayaa mid cusub oo nadiif ah.
            </p>
            <div className="flex gap-2 pt-2">
              <button
                onClick={() => setShowResetDialog(false)}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-slate-100 text-slate-700 hover:bg-slate-200"
              >
                Ka Noqo
              </button>
              <button
                onClick={() => {
                  clearAllHistory();
                  setShowResetDialog(false);
                }}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-red-600 text-white hover:bg-red-700"
              >
                Haa, Nadiifi Dhammaan
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Clear Cache Modal */}
      {showClearCacheDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl border border-slate-200 space-y-4">
            <h3 className="text-lg font-black text-slate-900">
              Nadiifi Kaydka SMS-yada
            </h3>
            <p className="text-xs text-slate-500">
              Ma hubtaa inaad tirtirto kaydka SMS-yada ee la aqriyay? Xisaabaadka lama tirtiri doono.
            </p>
            <div className="flex gap-2 pt-2">
              <button
                onClick={() => setShowClearCacheDialog(false)}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-slate-100 text-slate-700 hover:bg-slate-200"
              >
                Ka Noqo
              </button>
              <button
                onClick={() => {
                  clearSmsCache();
                  setShowClearCacheDialog(false);
                }}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-emerald-600 text-white hover:bg-emerald-700"
              >
                Haa, Nadiifi
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
