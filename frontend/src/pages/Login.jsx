import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Eye, EyeOff, Lock, Mail, AlertCircle } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    const result = await login(email, password);
    setSubmitting(false);

    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950 px-4 transition-colors duration-250 relative overflow-hidden">
      {/* Dynamic Background Gradients */}
      <div className="absolute top-0 -left-4 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl dark:bg-emerald-500/5"></div>
      <div className="absolute bottom-0 -right-4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl dark:bg-blue-500/5"></div>

      <div className="w-full max-w-md relative z-10">
        {/* Brand Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-emerald-500 flex items-center justify-center text-white font-extrabold text-xl shadow-md shadow-emerald-500/20">
              F
            </div>
            <span className="font-extrabold text-2xl tracking-tight text-slate-800 dark:text-slate-100">
              FinFlow<span className="text-emerald-500">AI</span>
            </span>
          </div>
          <p className="text-sm text-slate-500 dark:text-slate-400">Financial Operating System for SMBs</p>
        </div>

        {/* Card */}
        <div className="bg-white dark:bg-slate-900/80 backdrop-blur-md p-8 rounded-2xl shadow-xl border border-slate-200/50 dark:border-slate-850">
          <h2 className="text-xl font-bold text-slate-800 dark:text-white tracking-tight mb-6">Sign In</h2>
          
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-400 text-sm flex items-start space-x-2.5">
              <AlertCircle size={18} className="shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-3.5 text-slate-400" size={18} />
                <input
                  type="email"
                  required
                  placeholder="name@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-3.5 text-slate-400" size={18} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-11 pr-11 py-3 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-3 px-4 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm shadow-md shadow-emerald-500/10 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 transition-all cursor-pointer"
            >
              {submitting ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500 dark:text-slate-400">
            Don't have a corporate tenant?{' '}
            <Link to="/register" className="font-bold text-emerald-500 hover:underline">
              Create an organization
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
