import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User as UserIcon, Building2, Eye, EyeOff, AlertCircle, Sparkles } from 'lucide-react';

export default function Register() {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    companyName: '',
    taxId: '',
    department: 'Executive',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    const result = await register(formData);
    setSubmitting(false);

    if (result.success) {
      navigate('/login');
    } else {
      setError(result.error);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950 px-4 py-12 transition-colors duration-250 relative overflow-hidden">
      {/* Background Gradients */}
      <div className="absolute top-0 -left-4 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl dark:bg-emerald-500/5"></div>
      <div className="absolute bottom-0 -right-4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl dark:bg-blue-500/5"></div>

      <div className="w-full max-w-lg relative z-10">
        {/* Brand logo */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-emerald-500 flex items-center justify-center text-white font-extrabold text-xl shadow-md shadow-emerald-500/20">
              F
            </div>
            <span className="font-extrabold text-2xl tracking-tight text-slate-800 dark:text-slate-100">
              FinFlow<span className="text-emerald-500">AI</span>
            </span>
          </div>
          <p className="text-sm text-slate-500 dark:text-slate-400">Bootstrap a new corporate tenant directory</p>
        </div>

        {/* Card */}
        <div className="bg-white dark:bg-slate-900/80 backdrop-blur-md p-8 rounded-2xl shadow-xl border border-slate-200/50 dark:border-slate-850">
          <h2 className="text-xl font-bold text-slate-800 dark:text-white tracking-tight mb-6">Register Corporate Tenant</h2>
          
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/50 text-rose-700 dark:text-rose-400 text-sm flex items-start space-x-2.5">
              <AlertCircle size={18} className="shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Grid for Name */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">First Name</label>
                <div className="relative">
                  <UserIcon className="absolute left-3.5 top-3.5 text-slate-400" size={16} />
                  <input
                    type="text"
                    required
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    placeholder="Alice"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Last Name</label>
                <div className="relative">
                  <UserIcon className="absolute left-3.5 top-3.5 text-slate-400" size={16} />
                  <input
                    type="text"
                    required
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    placeholder="Smith"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                  />
                </div>
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Admin Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-3.5 text-slate-400" size={16} />
                <input
                  type="email"
                  required
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="admin@company.com"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-3.5 text-slate-400" size={16} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-3 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            {/* Divider */}
            <div className="relative py-2">
              <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-slate-200 dark:border-slate-800"></div></div>
              <div className="relative flex justify-center text-xs uppercase"><span className="bg-white dark:bg-slate-900 px-2 text-slate-400 font-semibold tracking-wider flex items-center"><Building2 size={12} className="mr-1" /> Company Profile</span></div>
            </div>

            {/* Company Name */}
            <div>
              <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Company / SMB Name</label>
              <div className="relative">
                <Building2 className="absolute left-3.5 top-3.5 text-slate-400" size={16} />
                <input
                  type="text"
                  required
                  name="companyName"
                  value={formData.companyName}
                  onChange={handleChange}
                  placeholder="Acme Corporation"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                />
              </div>
            </div>

            {/* Grid for Tax ID and Department */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Tax ID / PAN</label>
                <input
                  type="text"
                  name="taxId"
                  value={formData.taxId}
                  onChange={handleChange}
                  placeholder="US-998877-A"
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">Department</label>
                <select
                  name="department"
                  value={formData.department}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none transition-all"
                >
                  <option value="Executive">Executive</option>
                  <option value="Finance">Finance</option>
                  <option value="Engineering">Engineering</option>
                  <option value="Marketing">Marketing</option>
                  <option value="Audit">Audit</option>
                </select>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full mt-2 py-3 px-4 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-sm shadow-md shadow-emerald-500/10 focus:outline-none focus:ring-2 focus:ring-emerald-500 disabled:opacity-50 transition-all flex items-center justify-center space-x-1.5 cursor-pointer"
            >
              <Sparkles size={16} />
              <span>{submitting ? 'Bootstrapping...' : 'Create Organization'}</span>
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500 dark:text-slate-400">
            Already have an account?{' '}
            <Link to="/login" className="font-bold text-emerald-500 hover:underline">
              Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
