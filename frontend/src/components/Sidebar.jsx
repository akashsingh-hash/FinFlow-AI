import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  Receipt, 
  ClipboardCheck, 
  DollarSign, 
  FileText, 
  Briefcase, 
  PiggyBank, 
  Users, 
  ShieldCheck, 
  Settings, 
  LogOut 
} from 'lucide-react';

export default function Sidebar() {
  const { user, logout, hasRole } = useAuth();

  if (!user) return null;

  const menuItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'EMPLOYEE', 'AUDITOR'] },
    { name: 'Expenses', path: '/expenses', icon: Receipt, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'EMPLOYEE', 'AUDITOR'] },
    
    // Approval Workflow: Managers and Finance Managers review claims
    { name: 'Approvals', path: '/approvals', icon: ClipboardCheck, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER'] },
    
    // Financial Controllers: Budgets, Vendors, Invoices, Reimbursements
    { name: 'Reimbursements', path: '/reimbursements', icon: DollarSign, roles: ['ADMIN', 'FINANCE_MANAGER', 'AUDITOR'] },
    { name: 'Invoices', path: '/invoices', icon: FileText, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'AUDITOR'] },
    { name: 'Vendors', path: '/vendors', icon: Briefcase, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'AUDITOR'] },
    { name: 'Budgets', path: '/budgets', icon: PiggyBank, roles: ['ADMIN', 'FINANCE_MANAGER', 'AUDITOR'] },
    
    // User Directory: Admin and Finance Managers manage employees
    { name: 'Team Directory', path: '/users', icon: Users, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'EMPLOYEE', 'AUDITOR'] },
    
    // Compliance & Auditing: Admin and Auditors view audit trail
    { name: 'Audit Trail', path: '/audit-logs', icon: ShieldCheck, roles: ['ADMIN', 'AUDITOR'] },
    { name: 'Settings', path: '/settings', icon: Settings, roles: ['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'EMPLOYEE', 'AUDITOR'] },
  ];

  const filteredItems = menuItems.filter(item => hasRole(item.roles));

  return (
    <aside className="fixed inset-y-0 left-0 z-20 w-64 glass-panel border-r border-slate-200 dark:border-slate-800 flex flex-col justify-between">
      <div>
        {/* Brand Header */}
        <div className="h-16 flex items-center px-6 border-b border-slate-200/50 dark:border-slate-800/50">
          <div className="flex items-center space-x-2.5">
            <div className="w-8 h-8 rounded-lg bg-emerald-500 flex items-center justify-center text-white font-bold text-lg shadow-sm shadow-emerald-500/20">
              F
            </div>
            <span className="font-extrabold text-xl tracking-tight text-slate-800 dark:text-slate-100">
              FinFlow<span className="text-emerald-500">AI</span>
            </span>
          </div>
        </div>

        {/* User Quick Info */}
        <div className="p-4 mx-3 my-4 rounded-xl bg-slate-500/5 border border-slate-500/10">
          <p className="text-xs text-slate-400 dark:text-slate-500 uppercase tracking-wider font-semibold">Logged in as</p>
          <p className="font-bold text-sm text-slate-700 dark:text-slate-200 truncate mt-0.5">{user.firstName} {user.lastName}</p>
          <div className="flex items-center space-x-1.5 mt-1">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-500"></span>
            <span className="text-xs text-slate-500 capitalize">{user.role.toLowerCase().replace('_', ' ')}</span>
          </div>
        </div>

        {/* Navigation List */}
        <nav className="px-3 space-y-1 overflow-y-auto max-h-[calc(100vh-250px)]">
          {filteredItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) => `
                  flex items-center space-x-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200
                  ${isActive 
                    ? 'bg-emerald-500/10 dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-l-4 border-emerald-500 pl-3' 
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-500/5 hover:text-slate-900 dark:hover:text-slate-200'
                  }
                `}
              >
                <Icon size={18} />
                <span>{item.name}</span>
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Logout Action */}
      <div className="p-4 border-t border-slate-200/50 dark:border-slate-800/50">
        <button
          onClick={logout}
          className="w-full flex items-center space-x-3 px-4 py-2.5 rounded-xl text-sm font-medium text-rose-600 dark:text-rose-400 hover:bg-rose-500/5 dark:hover:bg-rose-500/10 transition-colors"
        >
          <LogOut size={18} />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
}
