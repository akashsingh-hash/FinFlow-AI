import React, { useState, useEffect } from 'react';
import { budgetAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import { Plus, Edit2, Calendar, Target, AlertTriangle } from 'lucide-react';
import Skeleton from '../components/Skeleton';

export default function Budgets() {
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [formData, setFormData] = useState({
    department: 'Engineering',
    allocatedAmount: '',
    startDate: '',
    endDate: '',
  });
  const [error, setError] = useState('');
  const { hasRole } = useAuth();

  const isEditor = hasRole(['ADMIN', 'FINANCE_MANAGER']);

  const fetchBudgets = async () => {
    try {
      const res = await budgetAPI.list();
      setBudgets(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, []);

  const openAddModal = () => {
    setEditId(null);
    setFormData({
      department: 'Engineering',
      allocatedAmount: '',
      startDate: new Date().toISOString().split('T')[0],
      endDate: new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0],
    });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (budget) => {
    setEditId(budget.id);
    setFormData({
      department: budget.department,
      allocatedAmount: budget.allocatedAmount.toString(),
      startDate: budget.startDate,
      endDate: budget.endDate,
    });
    setError('');
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const payload = {
        ...formData,
        allocatedAmount: parseFloat(formData.allocatedAmount),
      };

      if (editId) {
        await budgetAPI.update(editId, payload);
      } else {
        await budgetAPI.create(payload);
      }
      setIsModalOpen(false);
      fetchBudgets();
    } catch (err) {
      setError(err.message || 'Operation failed.');
    }
  };

  const fmt = (val) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0);

  return (
    <div className="space-y-6">
      {/* Top Banner Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div>
          <h3 className="font-bold text-slate-800 dark:text-white">Department Allocations</h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">Control and track departmental spend thresholds</p>
        </div>

        {isEditor && (
          <button
            onClick={openAddModal}
            className="w-full sm:w-auto px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm flex items-center justify-center space-x-1.5 shadow-md shadow-emerald-500/10 transition-all cursor-pointer"
          >
            <Plus size={16} />
            <span>Create Allocation</span>
          </button>
        )}
      </div>

      {/* Grid of Budget Cards */}
      {loading ? (
        <Skeleton type="cards" count={6} />
      ) : budgets.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {budgets.map((budget) => {
            const utilizationPercent = budget.allocatedAmount > 0 
              ? Math.round((budget.utilizedAmount / budget.allocatedAmount) * 100) 
              : 0;

            const isOverspent = budget.utilizedAmount > budget.allocatedAmount;

            return (
              <GlassCard key={budget.id} hoverEffect className="flex flex-col justify-between relative overflow-hidden">
                <div>
                  <div className="flex justify-between items-start">
                    <span className="px-3 py-1 rounded-lg bg-emerald-500/10 dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 text-xs font-bold uppercase tracking-wider">
                      {budget.department}
                    </span>
                    {isEditor && (
                      <button
                        onClick={() => openEditModal(budget)}
                        className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                        title="Edit Allocation"
                      >
                        <Edit2 size={13} />
                      </button>
                    )}
                  </div>

                  {/* Allocated and Utilized */}
                  <div className="mt-6 space-y-4">
                    <div className="flex justify-between items-baseline">
                      <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Allocated Limit</span>
                      <span className="text-xl font-extrabold text-slate-800 dark:text-white">{fmt(budget.allocatedAmount)}</span>
                    </div>

                    <div className="flex justify-between items-baseline">
                      <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Total Utilized</span>
                      <span className={`text-sm font-bold ${isOverspent ? 'text-rose-500' : 'text-slate-700 dark:text-slate-300'}`}>
                        {fmt(budget.utilizedAmount)}
                      </span>
                    </div>

                    {/* Progress slider */}
                    <div className="w-full bg-slate-150 dark:bg-slate-800 h-2 rounded-full overflow-hidden mt-2">
                      <div 
                        className={`h-full rounded-full transition-all duration-350 ${isOverspent ? 'bg-rose-500' : 'bg-emerald-500'}`}
                        style={{ width: `${Math.min(utilizationPercent, 100)}%` }}
                      />
                    </div>
                    
                    <div className="flex justify-between items-center text-[10px] text-slate-400 font-medium">
                      <span>{utilizationPercent}% Consumed</span>
                      <span>Remaining: {fmt(budget.allocatedAmount - budget.utilizedAmount)}</span>
                    </div>
                  </div>
                </div>

                {/* Date Ranges */}
                <div className="border-t border-slate-250/50 dark:border-slate-850 pt-4 mt-6 flex justify-between items-center text-[11px] text-slate-400 font-semibold">
                  <div className="flex items-center space-x-1">
                    <Calendar size={12} className="text-emerald-500" />
                    <span>Active: {budget.startDate}</span>
                  </div>
                  <span>to {budget.endDate}</span>
                </div>
              </GlassCard>
            );
          })}
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold">
          No budgets configured yet. Create a budget allocation to begin filing claims.
        </div>
      )}

      {/* Add / Edit Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editId ? 'Modify Allocation' : 'Create Allocation'}>
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold flex items-center space-x-2">
            <AlertTriangle size={14} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        <form onSubmit={handleFormSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Target Department</label>
            <select
              disabled={!!editId} // Department cannot be updated once saved (ACID properties)
              value={formData.department}
              onChange={(e) => setFormData({ ...formData, department: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all disabled:opacity-50"
            >
              <option value="Engineering">Engineering</option>
              <option value="Marketing">Marketing</option>
              <option value="Sales">Sales</option>
              <option value="Finance">Finance</option>
              <option value="HR">HR</option>
              <option value="Executive">Executive</option>
              <option value="Audit">Audit</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Allocated Budget Limit (INR)</label>
            <input
              type="number"
              required
              min="1"
              placeholder="e.g. 500000"
              value={formData.allocatedAmount}
              onChange={(e) => setFormData({ ...formData, allocatedAmount: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Start Date</label>
              <input
                type="date"
                required
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">End Date</label>
              <input
                type="date"
                required
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
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
              {editId ? 'Save Changes' : 'Confirm Allocation'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
