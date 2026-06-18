import React from 'react';
import { useAuth } from '../context/AuthContext';
import ThemeToggle from './ThemeToggle';
import { Building2, Bell } from 'lucide-react';

export default function Navbar({ title = 'Overview' }) {
  const { user } = useAuth();

  if (!user) return null;

  return (
    <header className="h-16 px-8 flex items-center justify-between border-b border-slate-200/50 dark:border-slate-800/50 bg-white/75 dark:bg-slate-900/75 backdrop-blur-md sticky top-0 z-10">
      {/* Title */}
      <div>
        <h1 className="text-xl font-bold text-slate-800 dark:text-slate-100 capitalize">
          {title}
        </h1>
      </div>

      {/* Corporate Context & Action Buttons */}
      <div className="flex items-center space-x-6">
        {/* Tenant Organization */}
        {user.companyName && (
          <div className="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-slate-100 dark:bg-slate-800 border border-slate-200/50 dark:border-slate-700/50 text-slate-600 dark:text-slate-300">
            <Building2 size={16} className="text-emerald-500" />
            <span className="text-xs font-semibold tracking-wide uppercase">
              {user.companyName}
            </span>
          </div>
        )}

        {/* Notifications & Themes */}
        <div className="flex items-center space-x-3">
          <button className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors relative" title="Notifications">
            <Bell size={18} />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-500"></span>
          </button>
          
          <ThemeToggle />
        </div>

        {/* User Mini Profile Avatar */}
        <div className="flex items-center space-x-3 pl-4 border-l border-slate-200 dark:border-slate-800">
          <div className="w-9 h-9 rounded-full bg-emerald-500 flex items-center justify-center text-white font-bold text-sm">
            {user.firstName.charAt(0)}{user.lastName.charAt(0)}
          </div>
          <div className="hidden md:block text-left">
            <p className="text-sm font-bold text-slate-800 dark:text-slate-200 line-clamp-1">
              {user.firstName} {user.lastName}
            </p>
            <p className="text-xs text-slate-400 dark:text-slate-500">
              {user.department || 'Staff'}
            </p>
          </div>
        </div>
      </div>
    </header>
  );
}
