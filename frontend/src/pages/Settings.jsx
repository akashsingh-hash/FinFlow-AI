import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import GlassCard from '../components/GlassCard';
import { Key, User as UserIcon, Check, AlertTriangle } from 'lucide-react';

export default function Settings() {
  const { user, changePassword } = useAuth();
  const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [status, setStatus] = useState({ type: '', message: '' });
  const [submitting, setSubmitting] = useState(false);

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setStatus({ type: '', message: '' });

    if (passwords.newPassword !== passwords.confirmPassword) {
      setStatus({ type: 'error', message: 'New passwords do not match.' });
      return;
    }

    if (passwords.newPassword.length < 6) {
      setStatus({ type: 'error', message: 'Password must be at least 6 characters.' });
      return;
    }

    setSubmitting(true);
    const result = await changePassword(passwords.oldPassword, passwords.newPassword);
    setSubmitting(false);

    if (result.success) {
      setStatus({ type: 'success', message: 'Password updated successfully!' });
      setPasswords({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } else {
      setStatus({ type: 'error', message: result.error });
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {/* Profile Card */}
      <GlassCard className="md:col-span-1 h-fit">
        <h3 className="text-base font-bold text-slate-800 dark:text-white mb-4 flex items-center space-x-2">
          <UserIcon size={18} className="text-emerald-500" />
          <span>Your Profile</span>
        </h3>
        
        <div className="flex flex-col items-center py-4 border-b border-slate-200/50 dark:border-slate-800/50 mb-4">
          <div className="w-16 h-16 rounded-full bg-emerald-500 text-white font-bold text-2xl flex items-center justify-center shadow-lg shadow-emerald-500/10">
            {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
          </div>
          <h4 className="font-bold text-slate-850 dark:text-white mt-3">{user?.firstName} {user?.lastName}</h4>
          <span className="text-xs font-semibold text-slate-400 capitalize">{user?.role?.toLowerCase().replace('_', ' ')}</span>
        </div>

        <div className="space-y-3.5 text-sm">
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Email Address</p>
            <p className="text-slate-700 dark:text-slate-300 font-medium truncate mt-0.5">{user?.email}</p>
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Corporate Tenant</p>
            <p className="text-slate-700 dark:text-slate-300 font-medium mt-0.5">{user?.companyName || 'Not affiliated'}</p>
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Department</p>
            <p className="text-slate-700 dark:text-slate-300 font-medium mt-0.5">{user?.department || 'Executive'}</p>
          </div>
          {user?.managerName && (
            <div>
              <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Direct Manager</p>
              <p className="text-slate-700 dark:text-slate-300 font-medium mt-0.5">{user.managerName}</p>
            </div>
          )}
        </div>
      </GlassCard>

      {/* Password Edit Card */}
      <GlassCard className="md:col-span-2">
        <h3 className="text-base font-bold text-slate-800 dark:text-white mb-4 flex items-center space-x-2">
          <Key size={18} className="text-emerald-500" />
          <span>Security & Credentials</span>
        </h3>

        {status.message && (
          <div className={`mb-6 p-4 rounded-xl border text-sm flex items-start space-x-2.5 ${
            status.type === 'success' 
              ? 'bg-emerald-50 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-900/50 text-emerald-700 dark:text-emerald-400' 
              : 'bg-rose-50 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-400'
          }`}>
            {status.type === 'success' ? <Check size={18} className="shrink-0 mt-0.5" /> : <AlertTriangle size={18} className="shrink-0 mt-0.5" />}
            <span>{status.message}</span>
          </div>
        )}

        <form onSubmit={handlePasswordSubmit} className="space-y-4 max-w-md">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Current Password</label>
            <input
              type="password"
              required
              placeholder="••••••••"
              value={passwords.oldPassword}
              onChange={(e) => setPasswords({ ...passwords, oldPassword: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">New Password</label>
            <input
              type="password"
              required
              placeholder="••••••••"
              value={passwords.newPassword}
              onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Confirm New Password</label>
            <input
              type="password"
              required
              placeholder="••••••••"
              value={passwords.confirmPassword}
              onChange={(e) => setPasswords({ ...passwords, confirmPassword: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm shadow-md shadow-emerald-500/10 transition-all disabled:opacity-50 cursor-pointer"
          >
            {submitting ? 'Updating...' : 'Update Password'}
          </button>
        </form>
      </GlassCard>
    </div>
  );
}
