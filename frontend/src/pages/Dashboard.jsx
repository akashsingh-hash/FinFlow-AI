import React, { useState, useEffect } from 'react';
import { dashboardAPI, analyticsAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import Skeleton from '../components/Skeleton';
import { 
  ResponsiveContainer, 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  Tooltip, 
  PieChart, 
  Pie, 
  Cell, 
  BarChart, 
  Bar, 
  Legend, 
  CartesianGrid 
} from 'recharts';
import { 
  TrendingUp, 
  AlertCircle, 
  DollarSign, 
  Activity, 
  Building2, 
  Users 
} from 'lucide-react';

const COLORS = ['#10b981', '#3b82f6', '#f59e0b', '#ec4899', '#8b5cf6', '#6366f1'];

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [monthlySpend, setMonthlySpend] = useState([]);
  const [deptSpend, setDeptSpend] = useState([]);
  const [budgetReport, setBudgetReport] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const [statsRes, monthlyRes, deptRes, budgetRes] = await Promise.all([
          dashboardAPI.getStats(),
          analyticsAPI.getMonthlySpend(),
          analyticsAPI.getDepartmentSpend(),
          analyticsAPI.getBudgetConsumption()
        ]);

        setStats(statsRes.data);
        setMonthlySpend(monthlyRes.data);
        setDeptSpend(deptRes.data);
        setBudgetReport(budgetRes.data);
      } catch (err) {
        console.error('Failed to load dashboard data:', err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  if (loading) {
    return <Skeleton type="dashboard" />;
  }

  // Format currency helper
  const fmt = (val) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0);

  return (
    <div className="space-y-6">
      {/* 4 Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Spent */}
        <GlassCard hoverEffect className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Approved Expenses</p>
              <h3 className="text-2xl font-bold text-slate-800 dark:text-white mt-1">{fmt(stats?.totalExpenses)}</h3>
            </div>
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-500 dark:bg-emerald-500/20 group-hover:scale-110 transition-transform">
              <DollarSign size={20} />
            </div>
          </div>
          <p className="text-xs text-slate-400 dark:text-slate-500 mt-4 flex items-center space-x-1">
            <TrendingUp size={14} className="text-emerald-500" />
            <span>Lifetime approved disbursements</span>
          </p>
        </GlassCard>

        {/* Monthly Spent */}
        <GlassCard hoverEffect className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">This Month's Spending</p>
              <h3 className="text-2xl font-bold text-slate-800 dark:text-white mt-1">{fmt(stats?.monthlyExpenses)}</h3>
            </div>
            <div className="p-3 rounded-xl bg-blue-500/10 text-blue-500 dark:bg-blue-500/20 group-hover:scale-110 transition-transform">
              <Activity size={20} />
            </div>
          </div>
          <p className="text-xs text-slate-400 dark:text-slate-500 mt-4 flex items-center space-x-1">
            <span>Deducted from active budgets</span>
          </p>
        </GlassCard>

        {/* Pending Actions */}
        <GlassCard hoverEffect className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Pending Approvals</p>
              <h3 className="text-2xl font-bold text-slate-800 dark:text-white mt-1">{stats?.pendingApprovalsCount}</h3>
            </div>
            <div className={`p-3 rounded-xl ${stats?.pendingApprovalsCount > 0 ? 'bg-amber-500/10 text-amber-500 dark:bg-amber-500/20 animate-pulse-gentle' : 'bg-slate-100 text-slate-400 dark:bg-slate-800'} group-hover:scale-110 transition-transform`}>
              <AlertCircle size={20} />
            </div>
          </div>
          <p className="text-xs text-slate-400 dark:text-slate-500 mt-4">
            {stats?.pendingApprovalsCount > 0 ? 'Requires immediate action' : 'All reviews caught up'}
          </p>
        </GlassCard>

        {/* Budget Utilization */}
        <GlassCard hoverEffect className="relative overflow-hidden">
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Overall Budget Utilization</p>
            <div className="flex items-baseline space-x-2 mt-1">
              <span className="text-2xl font-bold text-slate-800 dark:text-white">
                {stats?.totalAllocatedBudget > 0 
                  ? Math.round((stats.totalUtilizedBudget / stats.totalAllocatedBudget) * 100) 
                  : 0}%
              </span>
              <span className="text-xs text-slate-400">utilized</span>
            </div>
            {/* Progress Bar */}
            <div className="w-full bg-slate-100 dark:bg-slate-800 h-2 rounded-full mt-4 overflow-hidden">
              <div 
                className="bg-emerald-500 h-full rounded-full transition-all duration-500" 
                style={{ width: `${stats?.totalAllocatedBudget > 0 ? Math.min((stats.totalUtilizedBudget / stats.totalAllocatedBudget) * 100, 100) : 0}%` }}
              />
            </div>
            <div className="flex justify-between text-[10px] text-slate-400 mt-1.5 font-medium">
              <span>Spent: {fmt(stats?.totalUtilizedBudget)}</span>
              <span>Total: {fmt(stats?.totalAllocatedBudget)}</span>
            </div>
          </div>
        </GlassCard>
      </div>

      {/* Grid for Trend Chart & Department Pie Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Trend Area Chart (Col span 2) */}
        <GlassCard className="lg:col-span-2">
          <h4 className="text-base font-bold text-slate-800 dark:text-white tracking-tight mb-4">
            Monthly Spending Trend
          </h4>
          <div className="h-72">
            {monthlySpend.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={monthlySpend} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorSpend" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.2}/>
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.05)" />
                  <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} tickLine={false} />
                  <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: 'none', borderRadius: '12px' }}
                    labelStyle={{ color: '#94a3b8', fontWeight: 'bold' }}
                    itemStyle={{ color: '#fff' }}
                    formatter={(value) => [fmt(value), 'Spent']}
                  />
                  <Area type="monotone" dataKey="amount" stroke="#10b981" strokeWidth={2.5} fillOpacity={1} fill="url(#colorSpend)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 dark:text-slate-500 text-sm">
                No spending data recorded yet.
              </div>
            )}
          </div>
        </GlassCard>

        {/* Department Distribution (Col span 1) */}
        <GlassCard>
          <h4 className="text-base font-bold text-slate-800 dark:text-white tracking-tight mb-4">
            Spending by Department
          </h4>
          <div className="h-64 flex items-center justify-center">
            {deptSpend.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={deptSpend}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="amount"
                    nameKey="department"
                  >
                    {deptSpend.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: 'none', borderRadius: '12px', color: '#fff' }}
                    formatter={(value) => [fmt(value), 'Spent']}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-slate-400 dark:text-slate-500 text-sm">
                No expenses approved yet.
              </div>
            )}
          </div>
          {/* Custom Legends */}
          <div className="flex flex-wrap gap-2.5 justify-center mt-2">
            {deptSpend.map((entry, index) => (
              <div key={entry.department} className="flex items-center space-x-1.5 text-xs text-slate-600 dark:text-slate-400 font-medium">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }}></span>
                <span className="truncate max-w-[80px]">{entry.department}</span>
              </div>
            ))}
          </div>
        </GlassCard>
      </div>

      {/* Budget Consumption Bar Chart */}
      <GlassCard>
        <h4 className="text-base font-bold text-slate-800 dark:text-white tracking-tight mb-4">
          Departmental Budget Consumption
        </h4>
        <div className="h-72">
          {budgetReport.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={budgetReport} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.05)" />
                <XAxis dataKey="department" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip 
                  contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: 'none', borderRadius: '12px' }}
                  labelStyle={{ color: '#94a3b8', fontWeight: 'bold' }}
                  itemStyle={{ color: '#fff' }}
                  formatter={(value) => [fmt(value)]}
                />
                <Legend verticalAlign="top" height={36} wrapperStyle={{ fontSize: '12px' }} />
                <Bar dataKey="allocated" name="Allocated Budget" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                <Bar dataKey="utilized" name="Utilized Budget" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-slate-400 dark:text-slate-500 text-sm">
              No departmental budgets configured yet.
            </div>
          )}
        </div>
      </GlassCard>
    </div>
  );
}
