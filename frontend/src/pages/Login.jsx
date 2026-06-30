import React, { useState, Suspense, lazy } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Eye, EyeOff, Lock, Mail, AlertCircle } from 'lucide-react';

const Spline = lazy(() => import('@splinetool/react-spline'));

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
    <div className="min-h-screen flex items-center justify-center bg-hero-bg px-4 relative overflow-hidden font-sora">
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

      {/* Login Page Content Wrapper */}
      <div className="w-full max-w-md relative z-10 pointer-events-none">
        {/* Brand Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-primary-foreground font-extrabold text-xl shadow-md shadow-primary/20">
              F
            </div>
            <span className="font-extrabold text-2xl tracking-tight text-foreground">
              FinFlow<span className="text-primary">AI</span>
            </span>
          </div>
          <p className="text-xs text-muted-foreground uppercase tracking-widest font-light">Financial Operating System</p>
        </div>

        {/* Card - glows on hover */}
        <div className="glass-panel p-8 rounded-2xl border border-border pointer-events-auto hover:border-primary/30 hover:shadow-[0_0_30px_rgba(119,252,117,0.15)] transition-all duration-300">
          <h2 className="text-xl font-bold text-foreground tracking-tight mb-6">Sign In</h2>
          
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-950/20 border border-rose-900/50 text-rose-400 text-sm flex items-start space-x-2.5">
              <AlertCircle size={18} className="shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-3.5 text-muted-foreground" size={18} />
                <input
                  type="email"
                  required
                  placeholder="name@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all duration-200"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-3.5 text-muted-foreground" size={18} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-11 pr-11 py-3 rounded-xl bg-secondary/80 border border-border text-foreground placeholder-muted-foreground/60 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary hover:border-primary hover:shadow-[0_0_15px_rgba(119,252,117,0.25)] transition-all duration-200"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3.5 text-muted-foreground hover:text-foreground transition-colors"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-3 px-4 rounded-xl bg-primary hover:brightness-110 text-primary-foreground font-bold text-sm shadow-md shadow-primary/10 hover:shadow-[0_0_20px_rgba(119,252,117,0.4)] focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50 transition-all cursor-pointer active:scale-[0.97]"
            >
              {submitting ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-muted-foreground">
            Don't have a corporate tenant?{' '}
            <Link to="/register" className="font-bold text-primary hover:underline">
              Create an organization
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
