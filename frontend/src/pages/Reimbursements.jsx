import React, { useState, useEffect } from 'react';
import { reimbursementAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import { useAuth } from '../context/AuthContext';
import { CreditCard, Calendar, CheckCircle2, AlertCircle } from 'lucide-react';
import Skeleton from '../components/Skeleton';

export default function Reimbursements() {
  const [reimbursements, setReimbursements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [formData, setFormData] = useState({ paymentMethod: 'Bank Transfer', paymentReference: '' });
  const [error, setError] = useState('');
  const { user, hasRole } = useAuth();

  const isFinancePayer = hasRole(['ADMIN', 'FINANCE_MANAGER']);

  const fetchReimbursements = async () => {
    try {
      const res = isFinancePayer 
        ? await reimbursementAPI.list() 
        : await reimbursementAPI.getMyList();
      setReimbursements(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReimbursements();
  }, [user]);

  const openPayModal = (id) => {
    setSelectedId(id);
    setFormData({ paymentMethod: 'Bank Transfer', paymentReference: '' });
    setError('');
    setIsModalOpen(true);
  };

  const handlePaySubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await reimbursementAPI.pay(selectedId, formData);
      setIsModalOpen(false);
      fetchReimbursements();
    } catch (err) {
      setError(err.message || 'Payment recording failed.');
    }
  };

  const fmt = (val) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div>
          <h3 className="font-bold text-slate-800 dark:text-white">Expense Reimbursements</h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {isFinancePayer ? 'Manage company employee disbursements' : 'Track your approved claim disbursements'}
          </p>
        </div>
      </div>

      {loading ? (
        <Skeleton type="cards" count={6} />
      ) : reimbursements.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {reimbursements.map((r) => (
            <GlassCard key={r.id} hoverEffect className="flex flex-col justify-between">
              <div>
                <div className="flex justify-between items-start">
                  <Badge status={r.status} />
                  <span className="text-xs text-slate-400 font-semibold uppercase">{r.expenseCategory}</span>
                </div>

                <div className="mt-4">
                  <p className="text-[10px] font-bold text-emerald-500 uppercase tracking-wide">Expense Claim</p>
                  <h4 className="font-extrabold text-slate-850 dark:text-white truncate">{r.expenseTitle}</h4>
                  <p className="text-xs text-slate-450 dark:text-slate-400 font-medium mt-1">Submitted by: {r.employeeFullName}</p>
                </div>

                <div className="mt-4 flex justify-between items-baseline">
                  <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Amount</span>
                  <span className="text-lg font-extrabold text-slate-800 dark:text-white">{fmt(r.expenseAmount)}</span>
                </div>
              </div>

              {/* Settlement Info */}
              <div className="border-t border-slate-200/50 dark:border-slate-850 pt-4 mt-6">
                {r.status === 'PAID' ? (
                  <div className="space-y-1.5 text-xs text-slate-500 dark:text-slate-400 font-medium">
                    <p className="flex items-center space-x-1.5 text-emerald-500 font-bold">
                      <CheckCircle2 size={13} />
                      <span>Settled: {r.paymentMethod}</span>
                    </p>
                    <p>Reference: <code className="bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded font-mono text-[10px]">{r.paymentReference}</code></p>
                    <p>Paid At: {r.paidAt?.split('T')[0]}</p>
                  </div>
                ) : (
                  <div className="flex justify-between items-center">
                    <span className="text-xs text-slate-400 italic">Awaiting disbursement</span>
                    {isFinancePayer && (
                      <button
                        onClick={() => openPayModal(r.id)}
                        className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs flex items-center space-x-1 transition-colors shadow-sm shadow-emerald-500/10 cursor-pointer"
                      >
                        <CreditCard size={12} />
                        <span>Disburse Payout</span>
                      </button>
                    )}
                  </div>
                )}
              </div>
            </GlassCard>
          ))}
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold">
          No reimbursements recorded. Approved expenses will trigger payout rows automatically.
        </div>
      )}

      {/* Record Payout Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Record Disbursement Payment">
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold flex items-center space-x-2">
            <AlertCircle size={14} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        <form onSubmit={handlePaySubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Disbursement Method</label>
            <select
              value={formData.paymentMethod}
              onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            >
              <option value="Bank Transfer">Bank Transfer (IMPS/NEFT)</option>
              <option value="ACH">ACH Transfer / Direct Deposit</option>
              <option value="Cash">Cash Account Clearing</option>
              <option value="Card">Corporate Credit Card Settlement</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Payment Reference / UTR Number</label>
            <input
              type="text"
              required
              placeholder="e.g. UTR123456789098"
              value={formData.paymentReference}
              onChange={(e) => setFormData({ ...formData, paymentReference: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t border-slate-200/50 dark:border-slate-850">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 rounded-xl text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 text-sm font-semibold transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-6 py-2 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Record Settlement
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
