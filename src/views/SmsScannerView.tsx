import React, { useState } from 'react';
import { useTransactions } from '../context/TransactionContext';
import { defaultParserEngine } from '../parser/SmsParserEngine';
import {
  ScanText,
  Sparkles,
  CheckCircle2,
  AlertCircle,
  FileText,
  ArrowRight,
  Zap,
} from 'lucide-react';

const PRESET_SAMPLES = [
  {
    title: 'EVC Plus Dakhli ($35.00)',
    text: '[+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX MAXAMED (615887766). Haraagaagu waa $185.00. Ref:EVC8491. Tar: 16/09/26 19:10:00',
  },
  {
    title: 'EVC Plus Kharash ($15.00)',
    text: '[-$15.00] Waad u dirtay DUKAANKA CUNTADA (612334455). Haraagaagu waa $170.00. Ref:EVC9201. Tar: 16/09/26 19:12:00',
  },
  {
    title: 'E-Dahab Dakhli ($40.00)',
    text: 'Waxaad heshay $40.00 ka heshay AXMED ROOBLE (659911223). Haraagaagu waa $210.00. Tarjumaha: EDH4920',
  },
  {
    title: 'EVC Plus Airtime ($2.00)',
    text: 'Waxaad $2.00 ugu shubtay 615554433. Haraagaagu waa $139.50. Ref:EVC1102',
  },
  {
    title: 'Jeeb Premier ($50.00)',
    text: 'You have received $50.00 from PremierBank to your Jeeb Account. Balance: $250.00. TxId: JB50192',
  },
];

export const SmsScannerView: React.FC = () => {
  const { scanBatchSms, processSyntheticSms, showToast, setActiveTab } =
    useTransactions();
  const [smsInput, setSmsInput] = useState('');
  const [previewResult, setPreviewResult] = useState<any>(null);

  const handlePreview = () => {
    if (!smsInput.trim()) {
      showToast('Fadlan geli qoraalka SMS-ka', 'error');
      return;
    }

    const res = defaultParserEngine.parseSms('UserSMS', smsInput.trim());
    if (res) {
      setPreviewResult(res);
      showToast('Fariinta si guul leh ayaa loo aqoonsaday!', 'success');
    } else {
      setPreviewResult(null);
      showToast(
        'Lama aqoonsan habka fariintan. Hubi inay tahay EVC Plus, E-Dahab ama Jeeb.',
        'error'
      );
    }
  };

  const handleImportSingle = () => {
    if (!smsInput.trim()) return;
    const tx = processSyntheticSms('UserSMS', smsInput.trim(), true);
    if (tx) {
      setSmsInput('');
      setPreviewResult(null);
    }
  };

  const handleImportBatch = () => {
    if (!smsInput.trim()) return;
    const result = scanBatchSms(smsInput);
    showToast(
      `Waxaa la aqriyay ${result.total} fariimood, ${result.parsed} xisaabood ayaa la qabtay!`,
      'success'
    );
    setSmsInput('');
    setPreviewResult(null);
    setActiveTab('transactions');
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">
          Akhristaha & Shaandheeyaha SMS-ka (Scanner)
        </h1>
        <p className="text-xs sm:text-sm text-slate-500">
          Ku dheji (paste) fariin kasta oo EVC Plus, E-Dahab ama Jeeb ah si toos ahna u qabo xisaabteeda
        </p>
      </div>

      {/* Preset Quick Test Buttons */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-3">
        <div className="flex items-center gap-2">
          <Zap className="w-4 h-4 text-amber-500" />
          <span className="text-xs font-bold text-slate-700 uppercase tracking-wide">
            Tusaalayaal Diyaar Ah (Quick Samples):
          </span>
        </div>

        <div className="flex flex-wrap gap-2">
          {PRESET_SAMPLES.map((sample) => (
            <button
              key={sample.title}
              onClick={() => {
                setSmsInput(sample.text);
                const res = defaultParserEngine.parseSms('EVCPlus', sample.text);
                setPreviewResult(res);
              }}
              className="px-3 py-1.5 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-emerald-50 hover:text-emerald-700 hover:border-emerald-200 border border-transparent transition-all"
            >
              {sample.title}
            </button>
          ))}
        </div>
      </div>

      {/* Text Area Input */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs space-y-4">
        <label className="block text-sm font-bold text-slate-800">
          Geli ama Ku dheji Fariinta SMS-ka (Paste Raw SMS):
        </label>

        <textarea
          rows={5}
          value={smsInput}
          onChange={(e) => {
            setSmsInput(e.target.value);
            if (e.target.value.trim().length > 10) {
              const res = defaultParserEngine.parseSms('Auto', e.target.value);
              setPreviewResult(res);
            } else {
              setPreviewResult(null);
            }
          }}
          placeholder="Ku dheji halkan qoraalka fariinta SMS-ka (Tus: [+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX...)"
          className="w-full p-4 rounded-2xl text-sm font-mono border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 bg-slate-50/50 leading-relaxed"
        />

        <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
          <div className="flex items-center gap-2">
            <button
              onClick={handlePreview}
              className="px-4 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-slate-100 hover:bg-slate-200 text-slate-800 transition-colors flex items-center gap-1.5"
            >
              <ScanText className="w-4 h-4" />
              Tijaabi / Preview
            </button>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleImportBatch}
              className="px-4 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-slate-800 hover:bg-slate-900 text-white transition-colors"
            >
              Qabo Dhammaan (Batch Import)
            </button>

            <button
              onClick={handleImportSingle}
              disabled={!previewResult}
              className="px-5 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-emerald-600 hover:bg-emerald-700 disabled:opacity-40 disabled:hover:bg-emerald-600 text-white shadow-xs transition-colors flex items-center gap-2"
            >
              <CheckCircle2 className="w-4 h-4" />
              Diiwaangeli Xisaabtan
            </button>
          </div>
        </div>
      </div>

      {/* Parser Live Extraction Card */}
      {previewResult && (
        <div className="bg-emerald-50/60 rounded-3xl p-6 border border-emerald-200/80 shadow-xs space-y-4 animate-in fade-in">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-emerald-800 font-extrabold text-base">
              <CheckCircle2 className="w-5 h-5 text-emerald-600" />
              Natiijada Tooska Ah ee Shaandheeyaha (Parsed Successfully)
            </div>
            <span className="text-xs font-bold font-mono px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800">
              Score: {(previewResult.confidenceScore * 100).toFixed(0)}%
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div className="bg-white p-3 rounded-xl border border-emerald-100">
              <span className="text-slate-400 block mb-0.5">Shirkadda:</span>
              <span className="font-extrabold text-slate-900 text-sm">
                {previewResult.provider}
              </span>
            </div>

            <div className="bg-white p-3 rounded-xl border border-emerald-100">
              <span className="text-slate-400 block mb-0.5">Nooca:</span>
              <span className="font-extrabold text-slate-900 text-sm">
                {previewResult.type}
              </span>
            </div>

            <div className="bg-white p-3 rounded-xl border border-emerald-100">
              <span className="text-slate-400 block mb-0.5">Lacagta:</span>
              <span
                className={`font-black text-base ${
                  previewResult.type === 'INCOME' ? 'text-emerald-700' : 'text-red-700'
                }`}
              >
                ${previewResult.amount.toFixed(2)}
              </span>
            </div>

            <div className="bg-white p-3 rounded-xl border border-emerald-100">
              <span className="text-slate-400 block mb-0.5">Haraaga:</span>
              <span className="font-extrabold text-slate-900 text-sm">
                {previewResult.balance !== undefined ? `$${previewResult.balance.toFixed(2)}` : 'Lama helin'}
              </span>
            </div>
          </div>

          {(previewResult.sender || previewResult.receiver || previewResult.reference) && (
            <div className="bg-white p-3.5 rounded-xl border border-emerald-100 text-xs space-y-1">
              {previewResult.sender && (
                <div>
                  <span className="text-slate-400">Qofka soo diray: </span>
                  <strong className="text-slate-800">{previewResult.sender}</strong>
                </div>
              )}
              {previewResult.receiver && (
                <div>
                  <span className="text-slate-400">Qofka loo diray: </span>
                  <strong className="text-slate-800">{previewResult.receiver}</strong>
                </div>
              )}
              {previewResult.reference && (
                <div>
                  <span className="text-slate-400">Tixraaca: </span>
                  <strong className="font-mono text-emerald-700">{previewResult.reference}</strong>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
