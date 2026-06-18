import React, { useState, useEffect } from 'react';
import { vendorAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import { Plus, Edit2, Trash2, Mail, Phone, MapPin, Search } from 'lucide-react';
import ConfirmModal from '../components/ConfirmModal';
import Skeleton from '../components/Skeleton';

export default function Vendors() {
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [formData, setFormData] = useState({ name: '', email: '', phone: '', address: '' });
  const [error, setError] = useState('');
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, title: '', message: '', onConfirm: null });
  const { hasRole } = useAuth();

  const isEditor = hasRole(['ADMIN', 'FINANCE_MANAGER']);

  const fetchVendors = async () => {
    try {
      const res = await vendorAPI.list();
      setVendors(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVendors();
  }, []);

  const openAddModal = () => {
    setEditId(null);
    setFormData({ name: '', email: '', phone: '', address: '' });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (vendor) => {
    setEditId(vendor.id);
    setFormData({
      name: vendor.name,
      email: vendor.email || '',
      phone: vendor.phone || '',
      address: vendor.address || '',
    });
    setError('');
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editId) {
        await vendorAPI.update(editId, formData);
      } else {
        await vendorAPI.create(formData);
      }
      setIsModalOpen(false);
      fetchVendors();
    } catch (err) {
      setError(err.message || 'Action failed.');
    }
  };

  const handleDelete = (id) => {
    setConfirmDialog({
      isOpen: true,
      title: 'Delete Vendor',
      message: 'Are you sure you want to delete this vendor? All associated invoice references might be affected.',
      onConfirm: async () => {
        try {
          await vendorAPI.delete(id);
          fetchVendors();
        } catch (err) {
          alert(err.message || 'Cannot delete vendor.');
        }
      }
    });
  };

  const filteredVendors = vendors.filter(v => 
    v.name.toLowerCase().includes(search.toLowerCase()) ||
    (v.email && v.email.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      {/* Header Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-3 text-slate-400" size={16} />
          <input
            type="text"
            placeholder="Search vendors..."
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
            <span>Add Vendor</span>
          </button>
        )}
      </div>

      {/* Grid List */}
      {loading ? (
        <Skeleton type="cards" count={6} />
      ) : filteredVendors.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredVendors.map((vendor) => (
            <GlassCard key={vendor.id} hoverEffect className="flex flex-col justify-between">
              <div>
                <h4 className="text-base font-bold text-slate-800 dark:text-white truncate">{vendor.name}</h4>
                <div className="mt-4 space-y-2.5 text-xs text-slate-500 dark:text-slate-400 font-medium">
                  {vendor.email && (
                    <p className="flex items-center space-x-2">
                      <Mail size={14} className="text-emerald-500" />
                      <span className="truncate">{vendor.email}</span>
                    </p>
                  )}
                  {vendor.phone && (
                    <p className="flex items-center space-x-2">
                      <Phone size={14} className="text-emerald-500" />
                      <span>{vendor.phone}</span>
                    </p>
                  )}
                  {vendor.address && (
                    <p className="flex items-center space-x-2">
                      <MapPin size={14} className="text-emerald-500" />
                      <span className="truncate">{vendor.address}</span>
                    </p>
                  )}
                </div>
              </div>

              {isEditor && (
                <div className="flex justify-end space-x-2 border-t border-slate-200/50 dark:border-slate-850 pt-4 mt-6">
                  <button
                    onClick={() => openEditModal(vendor)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                    title="Edit Vendor"
                  >
                    <Edit2 size={14} />
                  </button>
                  <button
                    onClick={() => handleDelete(vendor.id)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/20 transition-colors"
                    title="Delete Vendor"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              )}
            </GlassCard>
          ))}
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold">
          No vendors found. Try adding a new supplier record.
        </div>
      )}

      {/* Add / Edit Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editId ? 'Edit Vendor' : 'Add Vendor'}>
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold">
            {error}
          </div>
        )}
        <form onSubmit={handleFormSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Vendor/Company Name</label>
            <input
              type="text"
              required
              placeholder="e.g. Amazon Web Services"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Billing Email Address</label>
            <input
              type="email"
              placeholder="e.g. accounts@vendor.com"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Phone Number</label>
            <input
              type="text"
              placeholder="e.g. +1-800-AWS"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Physical Address</label>
            <textarea
              placeholder="e.g. Seattle, WA"
              rows={2}
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all resize-none"
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
              {editId ? 'Save Changes' : 'Add Supplier'}
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
        variant="danger"
      />
    </div>
  );
}
