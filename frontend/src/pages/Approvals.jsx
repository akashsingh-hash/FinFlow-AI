import React, { useState, useEffect } from 'react';
import { approvalAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import { useAuth } from '../context/AuthContext';
import { ClipboardCheck, FileText, Check, X, AlertTriangle, Eye, RefreshCw } from 'lucide-react';
import Skeleton from '../components/Skeleton';

export default function Approvals() {
  const [approvals, setApprovals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedStep, setSelectedStep] = useState(null);
  const [comments, setComments] = useState('');
  const [error, setError] = useState('');
  const { user } = useAuth();

  const fetchPending = async () => {
    setLoading(true);
    try {
      const res = await approvalAPI.getPending();
      setApprovals(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, [user]);

  const openReviewModal = (step) => {
    setSelectedStep(step);
    setComments('');
    setError('');
    setIsModalOpen(true);
  };

  const handleApprove = async () => {
    setError('');
    try {
      await approvalAPI.approve(selectedStep.id, comments);
      setIsModalOpen(false);
      fetchPending();
    } catch (err) {
      setError(err.message || 'Approval action failed. Check departmental budget limits.');
    }
  };

  const handleReject = async () => {
    setError('');
    try {
      await approvalAPI.reject(selectedStep.id, comments);
      setIsModalOpen(false);
      fetchPending();
    } catch (err) {
      setError(err.message || 'Rejection action failed.');
    }
  };

  const handleClaim = async (id) => {
    try {
      await approvalAPI.claim(id);
      fetchPending();
    } catch (err) {
      alert(err.message || 'Claiming step failed.');
    }
  };

  const fmt = (val) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0);

  // Split direct reviews vs unassigned pool reviews
  const directReviews = approvals.filter(a => a.approverId === user?.id);
  const poolReviews = approvals.filter(a => a.approverFullName === 'Finance Pool');

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div>
          <h3 className="font-bold text-slate-800 dark:text-white">Workflow Approvals</h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">Verify employee claims and approve payouts</p>
        </div>
      </div>

      {loading ? (
        <Skeleton type="cards" count={3} />
      ) : approvals.length > 0 ? (
        <div className="space-y-8">
          {/* Direct Reviews */}
          {directReviews.length > 0 && (
            <div className="space-y-4">
              <h4 className="text-sm font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Assigned to You</h4>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {directReviews.map((step) => (
                  <GlassCard key={step.id} hoverEffect className="flex flex-col justify-between">
                    <div>
                      <div className="flex justify-between items-start">
                        <span className="px-2.5 py-0.5 rounded-lg bg-emerald-500/10 text-emerald-600 text-xs font-bold uppercase">{step.expenseCategory || 'General'}</span>
                        <Badge status={step.status} />
                      </div>
                      <div className="mt-4">
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Filer</p>
                        <h4 className="font-extrabold text-slate-850 dark:text-white truncate">{step.expenseUserFullName}</h4>
                        <p className="text-sm font-medium text-slate-600 dark:text-slate-350 truncate mt-1">{step.expenseTitle}</p>
                      </div>
                      <div className="mt-4 flex justify-between items-baseline">
                        <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Amount</span>
                        <span className="text-lg font-extrabold text-slate-850 dark:text-white">{fmt(step.expenseAmount)}</span>
                      </div>
                    </div>

                    <div className="border-t border-slate-200/50 dark:border-slate-850 pt-4 mt-6">
                      <button
                        onClick={() => openReviewModal(step)}
                        className="w-full py-2 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-xl text-xs flex items-center justify-center space-x-1 transition-colors cursor-pointer"
                      >
                        <ClipboardCheck size={14} />
                        <span>Perform Review</span>
                      </button>
                    </div>
                  </GlassCard>
                ))}
              </div>
            </div>
          )}

          {/* Unassigned Pool Reviews */}
          {poolReviews.length > 0 && (
            <div className="space-y-4">
              <h4 className="text-sm font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Unassigned Finance Pool</h4>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {poolReviews.map((step) => (
                  <GlassCard key={step.id} hoverEffect className="flex flex-col justify-between border-dashed border-2 border-slate-200 dark:border-slate-800">
                    <div>
                      <div className="flex justify-between items-start">
                        <span className="px-2.5 py-0.5 rounded-lg bg-blue-500/10 text-blue-600 text-xs font-bold uppercase">{step.expenseCategory || 'General'}</span>
                        <Badge status="PENDING" />
                      </div>
                      <div className="mt-4">
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Filer</p>
                        <h4 className="font-extrabold text-slate-850 dark:text-white truncate">{step.expenseUserFullName}</h4>
                        <p className="text-sm font-medium text-slate-600 dark:text-slate-350 truncate mt-1">{step.expenseTitle}</p>
                      </div>
                      <div className="mt-4 flex justify-between items-baseline">
                        <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Amount</span>
                        <span className="text-lg font-extrabold text-slate-800 dark:text-white">{fmt(step.expenseAmount)}</span>
                      </div>
                    </div>

                    <div className="border-t border-slate-200/50 dark:border-slate-850 pt-4 mt-6 flex space-x-2">
                      <button
                        onClick={() => handleClaim(step.id)}
                        className="w-1/2 py-2 bg-blue-500 hover:bg-blue-600 text-white font-bold rounded-xl text-xs flex items-center justify-center space-x-1 transition-colors cursor-pointer"
                      >
                        <RefreshCw size={13} />
                        <span>Claim Claim</span>
                      </button>
                      <button
                        onClick={() => openReviewModal(step)}
                        className="w-1/2 py-2 bg-slate-100 hover:bg-slate-250 dark:bg-slate-800 dark:hover:bg-slate-750 text-slate-600 dark:text-slate-200 font-bold rounded-xl text-xs flex items-center justify-center space-x-1 transition-colors cursor-pointer"
                      >
                        <Eye size={13} />
                        <span>Inspect</span>
                      </button>
                    </div>
                  </GlassCard>
                ))}
              </div>
            </div>
          )}
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold">
          No pending approvals in your queue.
        </div>
      )}

      {/* Review Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Perform Expense Claim Review">
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold flex items-center space-x-2">
            <AlertTriangle size={14} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        {selectedStep && (
          <div className="space-y-4">
            {/* Details Summary */}
            <div className="bg-slate-500/5 p-4 rounded-xl space-y-2.5 text-xs font-medium text-slate-600 dark:text-slate-400">
              <div className="flex justify-between">
                <span>Employee Filer:</span>
                <span className="font-bold text-slate-800 dark:text-white">{selectedStep.expenseUserFullName}</span>
              </div>
              <div className="flex justify-between">
                <span>Expense Item:</span>
                <span className="font-bold text-slate-800 dark:text-white">{selectedStep.expenseTitle}</span>
              </div>
              <div className="flex justify-between">
                <span>Amount:</span>
                <span className="font-extrabold text-emerald-500">{fmt(selectedStep.expenseAmount)}</span>
              </div>
            </div>

            {/* Decision Comments */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Review Notes / Remarks</label>
              <textarea
                placeholder="Provide reasoning for approvals or details for rejections..."
                rows={3}
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all resize-none"
              />
            </div>

            {/* Buttons */}
            <div className="flex justify-end space-x-3 pt-4 border-t border-slate-200/50 dark:border-slate-850">
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 rounded-xl text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 text-sm font-semibold transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleReject}
                className="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white font-bold rounded-xl text-sm flex items-center space-x-1 transition-colors cursor-pointer"
              >
                <X size={16} />
                <span>Reject Claim</span>
              </button>
              <button
                onClick={handleApprove}
                className="px-6 py-2 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-xl text-sm flex items-center space-x-1 transition-colors cursor-pointer"
              >
                <Check size={16} />
                <span>Approve Claim</span>
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
