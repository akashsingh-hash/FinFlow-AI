import React, { useState, useEffect } from 'react';
import { expenseAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import { useAuth } from '../context/AuthContext';
import { 
  Plus, 
  Send, 
  Edit2, 
  Trash2, 
  FileText, 
  ExternalLink, 
  AlertTriangle, 
  Search, 
  SlidersHorizontal 
} from 'lucide-react';
import ConfirmModal from '../components/ConfirmModal';
import Skeleton from '../components/Skeleton';

export default function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [viewScope, setViewScope] = useState('my'); // 'my' or 'company'
  const [file, setFile] = useState(null);
  const [editId, setEditId] = useState(null);
  const [formData, setFormData] = useState({ title: '', amount: '', category: 'Travel', description: '' });
  const [error, setError] = useState('');
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, title: '', message: '', onConfirm: null, variant: 'danger' });
  const { user, hasRole } = useAuth();

  const isCompanyViewer = hasRole(['ADMIN', 'FINANCE_MANAGER', 'AUDITOR']);

  const fetchExpenses = async () => {
    setLoading(true);
    try {
      const res = viewScope === 'company' 
        ? await expenseAPI.getCompanyExpenses() 
        : await expenseAPI.getMyExpenses();
      setExpenses(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, [viewScope, user]);

  const openAddModal = () => {
    setEditId(null);
    setFormData({ title: '', amount: '', category: 'Travel', description: '' });
    setFile(null);
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (exp) => {
    setEditId(exp.id);
    setFormData({
      title: exp.title,
      amount: exp.amount.toString(),
      category: exp.category,
      description: exp.description || '',
    });
    setFile(null);
    setError('');
    setIsModalOpen(true);
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const data = new FormData();
      const expenseJson = JSON.stringify({
        title: formData.title,
        amount: parseFloat(formData.amount),
        category: formData.category,
        description: formData.description,
      });

      data.append('expense', new Blob([expenseJson], { type: 'application/json' }));
      if (file) {
        data.append('file', file);
      }

      if (editId) {
        await expenseAPI.update(editId, data);
      } else {
        await expenseAPI.createDraft(data);
      }
      setIsModalOpen(false);
      fetchExpenses();
    } catch (err) {
      setError(err.message || 'Failed to save expense claim.');
    }
  };

  const handleSubmitClaim = (id) => {
    setConfirmDialog({
      isOpen: true,
      title: 'Submit Expense Claim',
      message: 'Are you sure you want to submit this claim for approval? You cannot edit it once submitted.',
      variant: 'success',
      onConfirm: async () => {
        try {
          await expenseAPI.submit(id);
          fetchExpenses();
        } catch (err) {
          alert(err.message || 'Submission failed.');
        }
      }
    });
  };

  const handleDelete = (id) => {
    setConfirmDialog({
      isOpen: true,
      title: 'Delete Expense Draft',
      message: 'Are you sure you want to delete this expense draft? This action cannot be undone.',
      variant: 'danger',
      onConfirm: async () => {
        try {
          await expenseAPI.delete(id);
          fetchExpenses();
        } catch (err) {
          alert(err.message || 'Delete failed.');
        }
      }
    });
  };

  const fmt = (val) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0);

  const filteredExpenses = expenses.filter(exp => 
    exp.title.toLowerCase().includes(search.toLowerCase()) ||
    exp.category.toLowerCase().includes(search.toLowerCase()) ||
    (exp.userFullName && exp.userFullName.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      {/* Top Banner Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 w-full sm:w-auto">
          {/* Search bar */}
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-3 text-slate-400" size={16} />
            <input
              type="text"
              placeholder="Search claims..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-sm rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
            />
          </div>

          {/* Toggle Directory Scope */}
          {isCompanyViewer && (
            <div className="flex bg-slate-105 dark:bg-slate-800 p-1 rounded-xl border border-slate-200/50 dark:border-slate-700/50">
              <button
                onClick={() => setViewScope('my')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                  viewScope === 'my' 
                    ? 'bg-white dark:bg-slate-900 text-slate-800 dark:text-white shadow-sm' 
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-800'
                }`}
              >
                My Claims
              </button>
              <button
                onClick={() => setViewScope('company')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                  viewScope === 'company' 
                    ? 'bg-white dark:bg-slate-900 text-slate-800 dark:text-white shadow-sm' 
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-800'
                }`}
              >
                All Company
              </button>
            </div>
          )}
        </div>

        {/* File Expense Claim Button */}
        <button
          onClick={openAddModal}
          className="w-full sm:w-auto px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm flex items-center justify-center space-x-1.5 shadow-md shadow-emerald-500/10 transition-all cursor-pointer"
        >
          <Plus size={16} />
          <span>File Expense Claim</span>
        </button>
      </div>

      {/* Directory Table */}
      {loading ? (
        <Skeleton type="table" count={5} />
      ) : filteredExpenses.length > 0 ? (
        <GlassCard className="overflow-hidden p-0 border border-slate-200 dark:border-slate-800">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-500/5 border-b border-slate-200/60 dark:border-slate-800/80 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-3.5 px-6">Claim Details</th>
                  <th className="py-3.5 px-6">Employee</th>
                  <th className="py-3.5 px-6">Department</th>
                  <th className="py-3.5 px-6">Category</th>
                  <th className="py-3.5 px-6">Amount</th>
                  <th className="py-3.5 px-6">Claim Status</th>
                  <th className="py-3.5 px-6 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200/40 dark:divide-slate-800/40 text-sm text-slate-750 dark:text-slate-300">
                {filteredExpenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-500/5 transition-colors">
                    <td className="py-4 px-6 max-w-xs">
                      <p className="font-bold text-slate-850 dark:text-white truncate">{exp.title}</p>
                      {exp.description && <p className="text-xs text-slate-450 dark:text-slate-500 line-clamp-1 mt-0.5">{exp.description}</p>}
                    </td>
                    <td className="py-4 px-6">{exp.userFullName}</td>
                    <td className="py-4 px-6">{exp.budgetDepartment || 'Administration'}</td>
                    <td className="py-4 px-6">
                      <span className="px-2.5 py-0.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 text-xs font-semibold">
                        {exp.category}
                      </span>
                    </td>
                    <td className="py-4 px-6 font-bold text-slate-800 dark:text-slate-200">{fmt(exp.amount)}</td>
                    <td className="py-4 px-6">
                      <Badge status={exp.status} />
                    </td>
                    <td className="py-4 px-6 text-right space-x-1.5 whitespace-nowrap">
                      {/* View File */}
                      {exp.receiptUrl ? (
                        <a
                          href={exp.receiptUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                          title="View Receipt File"
                        >
                          <FileText size={14} />
                        </a>
                      ) : (
                        <span className="inline-block w-8"></span>
                      )}

                      {/* Draft controls */}
                      {exp.status === 'DRAFT' && exp.userId === user?.id && (
                        <>
                          <button
                            onClick={() => handleSubmitClaim(exp.id)}
                            className="p-1.5 rounded-lg text-emerald-500 hover:text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-950/20 transition-colors cursor-pointer"
                            title="Submit for Approval"
                          >
                            <Send size={14} />
                          </button>
                          <button
                            onClick={() => openEditModal(exp)}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                            title="Edit Draft"
                          >
                            <Edit2 size={14} />
                          </button>
                          <button
                            onClick={() => handleDelete(exp.id)}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/20 transition-colors cursor-pointer"
                            title="Delete Draft"
                          >
                            <Trash2 size={14} />
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </GlassCard>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold">
          No expense claims found. Get started by filing a new expense draft!
        </div>
      )}

      {/* New Draft / Edit Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editId ? 'Edit Claim' : 'File Expense Claim'}>
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold flex items-center space-x-2">
            <AlertTriangle size={14} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        <form onSubmit={handleFormSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Claim Title</label>
            <input
              type="text"
              required
              placeholder="e.g. Client Dinner - Tech Hub"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Amount (INR)</label>
              <input
                type="number"
                required
                min="1"
                placeholder="e.g. 250"
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Category</label>
              <select
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              >
                <option value="Travel">Travel & Lodging</option>
                <option value="Meals & Entertainment">Meals & Entertainment</option>
                <option value="Office Supplies">Office Supplies</option>
                <option value="Software">Software & Tool Licensing</option>
                <option value="Hardware">IT Hardware / Devices</option>
                <option value="Others">Others</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Description / Purpose</label>
            <textarea
              placeholder="Provide a clear description of the purchase for the approvers..."
              rows={2}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all resize-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Upload Invoice / Receipt Document</label>
            <input
              type="file"
              accept="image/*,.pdf"
              onChange={handleFileChange}
              className="w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-emerald-500/10 file:text-emerald-500 hover:file:bg-emerald-500/20 file:transition-colors file:cursor-pointer"
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
              Save Draft
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={confirmDialog.isOpen}
        onClose={() => setConfirmDialog({ ...confirmDialog, isOpen: false })}
        onConfirm={confirmDialog.onConfirm}
        title={confirmDialog.title}
        message={confirmDialog.message}
        variant={confirmDialog.variant}
      />
    </div>
  );
}
