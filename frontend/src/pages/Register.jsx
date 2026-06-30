import React, { useState, Suspense, lazy } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User as UserIcon, Building2, Eye, EyeOff, AlertCircle, Sparkles } from 'lucide-react';

const Spline = lazy(() => import('@splinetool/react-spline'));

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
    <div className="min-h-screen flex items-center justify-center bg-hero-bg px-4 py-12 relative overflow-hidden font-sora">
      {/* Spline 3D Background */}
      <div className="absolute inset-0 z-0">
        <Suspense fallback={<div className="absolute inset-0 bg-hero-bg" />}>
          <Spline
            scene="https://prod.spline.design/Slk6b8kz3LRlKiyk/scene.splinecode"
            className="w-full h-full"
          />
        </Suspense>
      </div>

      {/* Dark overlay */}
      <div className="absolute inset-0 bg-black/40 z-[1] pointer-events-none" />

      {/* Register Page Content Wrapper */}
      <div className="w-full max-w-lg relative z-10 pointer-events-none">
        {/* Brand Logo */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-primary-foreground font-extrabold text-xl shadow-md shadow-primary/20">
              F
            </div>
            <span className="font-extrabold text-2xl tracking-tight text-foreground">
              FinFlow<span className="text-primary">AI</span>
            </span>
          </div>
          <p className="text-xs text-muted-foreground uppercase tracking-widest font-light">Create Corporate Tenant Directory</p>
        </div>

        {/* Card - glows on hover */}
        <div className="glass-panel p-8 rounded-2xl border border-border pointer-events-auto hover:border-primary/30 hover:shadow-[0_0_30px_rgba(119,252,117,0.15)] transition-all duration-300">
          <h2 className="text-xl font-bold text-foreground tracking-tight mb-6">Register Corporate Tenant</h2>
          
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-950/20 border border-rose-900/50 text-rose-400 text-sm flex items-start space-x-2.5">
              <AlertCircle size={18} className="shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Grid for Name */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">First Name</label>
                <div className="relative">
                  <UserIcon className="absolute left-3.5 top-3.5 text-muted-foreground" size={16} />
                  <input
                    type="text"
                    required
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    placeholder="Alice"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">Last Name</label>
                <div className="relative">
                  <UserIcon className="absolute left-3.5 top-3.5 text-muted-foreground" size={16} />
                  <input
                    type="text"
                    required
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    placeholder="Smith"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
                  />
                </div>
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">Admin Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-3.5 text-muted-foreground" size={16} />
                <input
                  type="email"
                  required
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="admin@company.com"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-3.5 text-muted-foreground" size={16} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-3 text-muted-foreground hover:text-foreground transition-colors"
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            {/* Divider */}
            <div className="relative py-2">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-border"></div>
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-secondary px-2 text-muted-foreground font-semibold tracking-wider flex items-center border border-border rounded-md text-[10px]">
                  <Building2 size={10} className="mr-1" /> Company Profile
                </span>
              </div>
            </div>

            {/* Company Name */}
            <div>
              <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">Company / SMB Name</label>
              <div className="relative">
                <Building2 className="absolute left-3.5 top-3.5 text-muted-foreground" size={16} />
                <input
                  type="text"
                  required
                  name="companyName"
                  value={formData.companyName}
                  onChange={handleChange}
                  placeholder="Acme Corporation"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
                />
              </div>
            </div>

            {/* Grid for Tax ID and Department */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">Tax ID / PAN</label>
                <input
                  type="text"
                  name="taxId"
                  value={formData.taxId}
                  onChange={handleChange}
                  placeholder="US-998877-A"
                  className="w-full px-4 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">Department</label>
                <select
                  name="department"
                  value={formData.department}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 rounded-xl bg-secondary/80 border border-border text-foreground text-sm focus:ring-2 focus:ring-primary focus:outline-none hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all"
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
              className="w-full mt-2 py-3 px-4 rounded-xl bg-primary hover:brightness-110 text-primary-foreground font-bold text-sm shadow-md shadow-primary/10 hover:shadow-[0_0_20px_rgba(119,252,117,0.4)] focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50 transition-all flex items-center justify-center space-x-1.5 cursor-pointer active:scale-[0.97]"
            >
              <Sparkles size={16} />
              <span>{submitting ? 'Bootstrapping...' : 'Create Organization'}</span>
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-muted-foreground">
            Already have an account?{' '}
            <Link to="/login" className="font-bold text-primary hover:underline">
              Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
