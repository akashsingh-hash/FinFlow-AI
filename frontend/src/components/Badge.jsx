import React from 'react';

const statusStyles = {
  // Expense Statuses
  DRAFT: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
  SUBMITTED: 'bg-blue-50 text-blue-700 dark:bg-blue-950/30 dark:text-blue-300 border border-blue-200 dark:border-blue-900',
  UNDER_REVIEW: 'bg-amber-50 text-amber-700 dark:bg-amber-950/30 dark:text-amber-300 border border-amber-200 dark:border-amber-900',
  APPROVED: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/30 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-900',
  REJECTED: 'bg-rose-50 text-rose-700 dark:bg-rose-950/30 dark:text-rose-300 border border-rose-200 dark:border-rose-900',
  REIMBURSED: 'bg-teal-50 text-teal-700 dark:bg-teal-950/30 dark:text-teal-300 border border-teal-200 dark:border-teal-900',

  // Payout Statuses
  PENDING: 'bg-amber-50 text-amber-700 dark:bg-amber-950/30 dark:text-amber-300 border border-amber-200 dark:border-amber-900',
  PAID: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/30 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-900',

  // Invoice Statuses
  CREATED: 'bg-cyan-50 text-cyan-700 dark:bg-cyan-950/30 dark:text-cyan-300 border border-cyan-200 dark:border-cyan-900',
  OVERDUE: 'bg-rose-100 text-rose-800 dark:bg-rose-950/40 dark:text-rose-300 border border-rose-300 dark:border-rose-900',

  // User Statuses
  ACTIVE: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/30 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-900',
  INACTIVE: 'bg-rose-50 text-rose-700 dark:bg-rose-950/30 dark:text-rose-300 border border-rose-200 dark:border-rose-900',
  PENDING_USER: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border border-slate-200 dark:border-slate-700',
};

export default function Badge({ status }) {
  const normalizedStatus = status ? status.toUpperCase() : 'DRAFT';
  const displayStatus = normalizedStatus.replace('_', ' ');
  
  // Custom lookup fallback to cover invite states
  const style = statusStyles[normalizedStatus] || 'bg-slate-100 text-slate-700';

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold capitalize tracking-wide ${style}`}>
      {displayStatus.toLowerCase()}
    </span>
  );
}
