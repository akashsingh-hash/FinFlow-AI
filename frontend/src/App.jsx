import React, { useEffect } from 'react';
import { Routes, Route, Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';

// Page Imports
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Expenses from './pages/Expenses';
import Approvals from './pages/Approvals';
import Reimbursements from './pages/Reimbursements';
import Invoices from './pages/Invoices';
import Vendors from './pages/Vendors';
import Budgets from './pages/Budgets';
import Users from './pages/Users';
import AuditLogs from './pages/AuditLogs';
import Settings from './pages/Settings';
import Index from './pages/Index';

// Protected Route Shield with Role Guards
function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading, hasRole } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950">
        <div className="w-10 h-10 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !hasRole(allowedRoles)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

// Master Dashboard Frame Layout Shell
function DashboardLayout() {
  const location = useLocation();
  const path = location.pathname.substring(1);
  const displayTitle = path ? path.replace('-', ' ') : 'Overview';

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 transition-colors duration-250">
      <Sidebar />
      <div className="pl-64 min-h-screen flex flex-col">
        <Navbar title={displayTitle} />
        <main className="p-8 flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default function App() {
  useEffect(() => {
    // Force dark mode globally
    document.documentElement.classList.add('dark');
    localStorage.setItem('theme', 'dark');
  }, []);

  return (
    <Routes>
      {/* Public Pages */}
      <Route path="/" element={<Index />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected Dashboard Routes nested under a path-less Layout Route */}
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* All authenticated users */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/expenses" element={<Expenses />} />
        <Route path="/users" element={<Users />} />
        <Route path="/settings" element={<Settings />} />

        {/* Manager/Finance workflow review guards */}
        <Route
          path="/approvals"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'FINANCE_MANAGER', 'MANAGER']}>
              <Approvals />
            </ProtectedRoute>
          }
        />

        {/* Corporate controllers: budgets, invoices, vendors, reimbursements */}
        <Route
          path="/reimbursements"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'FINANCE_MANAGER', 'AUDITOR']}>
              <Reimbursements />
            </ProtectedRoute>
          }
        />
        <Route
          path="/invoices"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'AUDITOR']}>
              <Invoices />
            </ProtectedRoute>
          }
        />
        <Route
          path="/vendors"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'AUDITOR']}>
              <Vendors />
            </ProtectedRoute>
          }
        />
        <Route
          path="/budgets"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'FINANCE_MANAGER', 'AUDITOR']}>
              <Budgets />
            </ProtectedRoute>
          }
        />

        {/* Compliance and audit trail guard */}
        <Route
          path="/audit-logs"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'AUDITOR']}>
              <AuditLogs />
            </ProtectedRoute>
          }
        />
      </Route>

      {/* Wildcard redirection */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
