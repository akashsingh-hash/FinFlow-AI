import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import { useAuth } from '../context/AuthContext';
import { Plus, UserPlus, UserCheck, UserX, Shield, AlertTriangle } from 'lucide-react';
import Skeleton from '../components/Skeleton';

export default function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    role: 'EMPLOYEE',
    managerId: '',
    department: 'Engineering',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { user: currentUser, hasRole } = useAuth();

  const isAdmin = hasRole(['ADMIN']);
  const isFinance = hasRole(['FINANCE_MANAGER']);
  const isEditor = isAdmin || isFinance;

  const fetchUsers = async () => {
    try {
      const res = await userAPI.list();
      setUsers(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const openInviteModal = () => {
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      role: 'EMPLOYEE',
      managerId: '',
      department: 'Engineering',
    });
    setError('');
    setSuccess('');
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      const payload = {
        ...formData,
        managerId: formData.managerId ? parseInt(formData.managerId) : null,
      };
      await userAPI.invite(payload);
      setSuccess('Employee invited successfully!');
      setIsModalOpen(false);
      fetchUsers();
    } catch (err) {
      setError(err.message || 'Failed to invite employee.');
    }
  };

  const toggleStatus = async (user) => {
    try {
      if (user.status === 'ACTIVE') {
        await userAPI.deactivate(user.id);
      } else {
        await userAPI.activate(user.id);
      }
      fetchUsers();
    } catch (err) {
      alert(err.message || 'Status update failed.');
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    try {
      await userAPI.assignRole(userId, newRole);
      fetchUsers();
    } catch (err) {
      alert(err.message || 'Role change failed.');
    }
  };

  // Managers are users with roles ADMIN, FINANCE_MANAGER, or MANAGER
  const potentialManagers = users.filter(u => 
    u.status === 'ACTIVE' && 
    ['ADMIN', 'FINANCE_MANAGER', 'MANAGER'].includes(u.role)
  );

  return (
    <div className="space-y-6">
      {/* Header Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div>
          <h3 className="font-bold text-slate-800 dark:text-white">Team Directory</h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">Manage organization accounts and permissions</p>
        </div>

        {isEditor && (
          <button
            onClick={openInviteModal}
            className="w-full sm:w-auto px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm flex items-center justify-center space-x-1.5 shadow-md shadow-emerald-500/10 transition-all cursor-pointer"
          >
            <UserPlus size={16} />
            <span>Invite Team Member</span>
          </button>
        )}
      </div>

      {/* Directory Table */}
      {loading ? (
        <Skeleton type="table" count={5} />
      ) : (
        <GlassCard className="overflow-hidden p-0 border border-slate-200 dark:border-slate-800">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-500/5 border-b border-slate-200/60 dark:border-slate-800/80 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-3.5 px-6">Name</th>
                  <th className="py-3.5 px-6">Email Address</th>
                  <th className="py-3.5 px-6">Department</th>
                  <th className="py-3.5 px-6">Role</th>
                  <th className="py-3.5 px-6">Account Status</th>
                  {isEditor && <th className="py-3.5 px-6 text-right">Actions</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200/40 dark:divide-slate-800/40 text-sm text-slate-700 dark:text-slate-350">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-500/5 transition-colors">
                    <td className="py-4 px-6 font-bold text-slate-800 dark:text-slate-200">
                      {u.firstName} {u.lastName} {u.id === currentUser.id && <span className="text-[10px] ml-1 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 px-1.5 py-0.5 rounded font-normal">You</span>}
                    </td>
                    <td className="py-4 px-6 font-medium">{u.email}</td>
                    <td className="py-4 px-6">{u.department || 'General'}</td>
                    <td className="py-4 px-6">
                      {isAdmin && u.id !== currentUser.id ? (
                        <select
                          value={u.role}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className="px-2.5 py-1 rounded bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-xs font-semibold focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                        >
                          <option value="EMPLOYEE">Employee</option>
                          <option value="MANAGER">Manager</option>
                          <option value="FINANCE_MANAGER">Finance Manager</option>
                          <option value="ADMIN">Admin</option>
                          <option value="AUDITOR">Auditor</option>
                        </select>
                      ) : (
                        <span className="text-xs font-semibold uppercase">{u.role.replace('_', ' ')}</span>
                      )}
                    </td>
                    <td className="py-4 px-6">
                      <Badge status={u.status} />
                    </td>
                    {isEditor && (
                      <td className="py-4 px-6 text-right">
                        {u.id !== currentUser.id && (
                          <button
                            onClick={() => toggleStatus(u)}
                            className={`p-1.5 rounded-lg border text-xs font-bold transition-all cursor-pointer ${
                              u.status === 'ACTIVE'
                                ? 'bg-rose-50 border-rose-200 text-rose-600 hover:bg-rose-100 dark:bg-rose-950/20 dark:border-rose-900/50 dark:text-rose-400'
                                : 'bg-emerald-50 border-emerald-200 text-emerald-600 hover:bg-emerald-100 dark:bg-emerald-950/20 dark:border-emerald-900/50 dark:text-emerald-400'
                            }`}
                            title={u.status === 'ACTIVE' ? 'Deactivate Account' : 'Activate Account'}
                          >
                            {u.status === 'ACTIVE' ? <UserX size={14} /> : <UserCheck size={14} />}
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </GlassCard>
      )}

      {/* Invite Member Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Invite Team Member">
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-450 text-xs font-semibold flex items-center space-x-2">
            <AlertTriangle size={14} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        <form onSubmit={handleFormSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">First Name</label>
              <input
                type="text"
                required
                placeholder="Edward"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Last Name</label>
              <input
                type="text"
                required
                placeholder="Miller"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Corporate Email Address</label>
            <input
              type="email"
              required
              placeholder="employee@company.com"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Department</label>
              <select
                value={formData.department}
                onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              >
                <option value="Engineering">Engineering</option>
                <option value="Marketing">Marketing</option>
                <option value="Sales">Sales</option>
                <option value="Finance">Finance</option>
                <option value="HR">HR</option>
                <option value="Executive">Executive</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Assigned Role</label>
              <select
                value={formData.role}
                onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
              >
                <option value="EMPLOYEE">Employee</option>
                <option value="MANAGER">Manager</option>
                <option value="FINANCE_MANAGER">Finance Manager</option>
                <option value="AUDITOR">Auditor</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Reporting Manager</label>
            <select
              value={formData.managerId}
              onChange={(e) => setFormData({ ...formData, managerId: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
            >
              <option value="">No Manager Assigned (Direct Executive)</option>
              {potentialManagers.map(mgr => (
                <option key={mgr.id} value={mgr.id}>
                  {mgr.firstName} {mgr.lastName} ({mgr.role.replace('_', ' ').toLowerCase()})
                </option>
              ))}
            </select>
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
              Send Invitation
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
