import React, { useState, useEffect } from 'react';
import { invoiceAPI, vendorAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import { useAuth } from '../context/AuthContext';
import { Plus, Calendar, FileText, CheckCircle, CreditCard, Search, ExternalLink, AlertTriangle } from 'lucide-react';
import ConfirmModal from '../components/ConfirmModal';
import Skeleton from '../components/Skeleton';

export default function Invoices() {
  const [invoices, setInvoices] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [file, setFile] = useState(null);
  const [formData, setFormData] = useState({
    invoiceNumber: '',
    amount: '',
    dueDate: '',
    vendorId: '',
  });
  const [error, setError] = useState('');
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, title: '', message: '', onConfirm: null, variant: 'success' });
  const { hasRole } = useAuth();

  const isEditor = hasRole(['ADMIN', 'FINANCE_MANAGER', 'MANAGER']);
  const isPayer = hasRole(['ADMIN', 'FINANCE_MANAGER']);

  const fetchData = async () => {
    try {
      const [invRes, venRes] = await Promise.all([
        invoiceAPI.list(),
        vendorAPI.list(),
      ]);
      setInvoices(invRes.data);
      setVendors(venRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const openAddModal = () => {
    setFormData({
      invoiceNumber: '',
      amount: '',
      dueDate: new Date().toISOString().split('T')[0],
      vendorId: vendors[0]?.id || '',
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

    if (!formData.vendorId) {
      setError('Please select a vendor.');
      return;
    }

    try {
      const data = new FormData();
      // Spring part mapping requires JSON descriptor string or separate fields
      // In Spring Boot, we map the fields in @RequestPart.
      // Since we defined the InvoiceRequest in @RequestPart("invoice") in our controller, 
      // we need to append the request data as a JSON Blob!
      const invoiceJson = JSON.stringify({
        invoiceNumber: formData.invoiceNumber,
        amount: parseFloat(formData.amount),
        dueDate: formData.dueDate,
        vendorId: parseInt(formData.vendorId),
      });

      data.append('invoice', new Blob([invoiceJson], { type: 'application/json' }));
      if (file) {
        data.append('file', file);
      }

      await invoiceAPI.create(data);
      setIsModalOpen(false);
      fetchData();
    } catch (err) {
      setError(err.message || 'Failed to upload invoice.');
    }
  };

  const handlePay = (id) => {
    setConfirmDialog({
      isOpen: true,
      title: 'Pay Invoice',
      message: 'Are you sure you want to mark this invoice as PAID? This will update the status and record the payment.',
      variant: 'success',
      onConfirm: async () => {
        try {
          await invoiceAPI.pay(id);
          fetchData();
        } catch (err) {
          alert(err.message || 'Payment mark failed.');
        }
      }
    });
  };

  const fmt = (val) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0);

  const filteredInvoices = invoices.filter(inv => 
    inv.invoiceNumber.toLowerCase().includes(search.toLowerCase()) ||
    inv.vendorName.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Top Banner Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-3 text-slate-400" size={16} />
          <input
            type="text"
            placeholder="Search invoices..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
          />
        </div>

        {isEditor && (
          <button
            onClick={openAddModal}
            className="w-full sm:w-auto px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm flex items-center justify-center space-x-1.5 shadow-md shadow-emerald-500/10 transition-all cursor-pointer"
          >
            <Plus size={16} />
            <span>Upload Invoice</span>
          </button>
        )}
      </div>

      {/* Grid of Invoices */}
      {loading ? (
        <Skeleton type="cards" count={6} />
      ) : filteredInvoices.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredInvoices.map((inv) => (
            <GlassCard key={inv.id} hoverEffect className="flex flex-col justify-between">
              <div>
                <div className="flex justify-between items-start">
                  <span className="font-semibold text-xs text-slate-400 uppercase tracking-wider">{inv.invoiceNumber}</span>
                  <Badge status={inv.status} />
                </div>

                <div className="mt-4">
                  <p className="text-[10px] font-bold text-emerald-500 uppercase tracking-wide">Vendor</p>
                  <h4 className="font-extrabold text-slate-850 dark:text-white truncate">{inv.vendorName}</h4>
                </div>

                <div className="mt-4 flex justify-between items-baseline">
                  <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Amount Due</span>
                  <span className="text-lg font-extrabold text-slate-800 dark:text-white">{fmt(inv.amount)}</span>
                </div>

                <div className="mt-4 border-t border-slate-200/50 dark:border-slate-850 pt-3 flex items-center text-xs text-slate-450 dark:text-slate-400 font-medium space-x-1">
                  <Calendar size={13} className="text-emerald-500" />
                  <span>Due: {inv.dueDate}</span>
                </div>
              </div>

              {/* Action Toolbar */}
              <div className="flex justify-between items-center border-t border-slate-200/50 dark:border-slate-850 pt-4 mt-6">
                {inv.fileUrl ? (
                  <a
                    href={inv.fileUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="text-xs font-bold text-emerald-500 hover:text-emerald-600 flex items-center space-x-1"
                  >
                    <FileText size={14} />
                    <span>View Bill</span>
                    <ExternalLink size={10} />
                  </a>
                ) : (
                  <span className="text-xs text-slate-400 italic">No bill PDF uploaded</span>
                )}

                {isPayer && inv.status !== 'PAID' && (
                  <button
                    onClick={() => handlePay(inv.id)}
                    className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs flex items-center space-x-1 transition-colors shadow-sm shadow-emerald-500/10 cursor-pointer"
                  >
                    <CreditCard size={12} />
                    <span>Mark Paid</span>
                  </button>
                )}
              </div>
            </GlassCard>
          ))}
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold">
          No invoices registered. Record incoming vendor bills to track due dates.
        </div>
      )}

      {/* Upload Invoice Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Record Incoming Invoice">
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold flex items-center space-x-2">
            <AlertTriangle size={14} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        <form onSubmit={handleFormSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Invoice Number</label>
              <input
                type="text"
                required
                placeholder="e.g. INV-AWS-2026-99"
                value={formData.invoiceNumber}
                onChange={(e) => setFormData({ ...formData, invoiceNumber: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Invoice Amount (INR)</label>
              <input
                type="number"
                required
                min="1"
                placeholder="e.g. 1500"
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Associated Vendor</label>
              <select
                value={formData.vendorId}
                onChange={(e) => setFormData({ ...formData, vendorId: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              >
                <option value="">Select Vendor</option>
                {vendors.map(v => (
                  <option key={v.id} value={v.id}>{v.name}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Payment Due Date</label>
              <input
                type="date"
                required
                value={formData.dueDate}
                onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Upload Bill PDF / Receipt Image</label>
            <input
              type="file"
              accept=".pdf,image/*"
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
              Register Invoice
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
