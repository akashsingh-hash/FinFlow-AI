import React from 'react';

export default function Skeleton({ type = 'table', count = 5 }) {
  if (type === 'dashboard') {
    return (
      <div className="space-y-6">
        {/* KPI Stats cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="glass-panel p-6 rounded-2xl border border-slate-200/40 dark:border-slate-800/40 bg-white/50 dark:bg-slate-900/50 animate-pulse space-y-4">
              <div className="flex justify-between items-center">
                <div className="space-y-2 flex-1">
                  <div className="h-3 w-2/3 bg-slate-205 dark:bg-slate-800 rounded"></div>
                  <div className="h-7 w-1/2 bg-slate-300 dark:bg-slate-700 rounded"></div>
                </div>
                <div className="w-10 h-10 rounded-xl bg-slate-200 dark:bg-slate-800"></div>
              </div>
              <div className="h-3 w-3/4 bg-slate-200 dark:bg-slate-800 rounded"></div>
            </div>
          ))}
        </div>

        {/* Charts skeletons */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 glass-panel p-6 rounded-2xl border border-slate-200/40 dark:border-slate-800/40 bg-white/50 dark:bg-slate-900/50 animate-pulse space-y-4">
            <div className="h-5 w-40 bg-slate-200 dark:bg-slate-800 rounded"></div>
            <div className="h-72 w-full bg-slate-200 dark:bg-slate-800/50 rounded-xl"></div>
          </div>
          <div className="glass-panel p-6 rounded-2xl border border-slate-200/40 dark:border-slate-800/40 bg-white/50 dark:bg-slate-900/50 animate-pulse space-y-4">
            <div className="h-5 w-40 bg-slate-200 dark:bg-slate-800 rounded"></div>
            <div className="h-64 w-full rounded-full bg-slate-200 dark:bg-slate-800/50 max-w-[200px] aspect-square mx-auto"></div>
          </div>
        </div>
      </div>
    );
  }

  if (type === 'cards') {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[...Array(count)].map((_, i) => (
          <div key={i} className="glass-panel p-6 rounded-2xl border border-slate-200/40 dark:border-slate-800/40 bg-white/50 dark:bg-slate-900/50 animate-pulse flex flex-col justify-between h-56">
            <div className="space-y-4">
              <div className="flex justify-between items-start">
                <div className="h-5 w-24 bg-slate-205 dark:bg-slate-800 rounded"></div>
                <div className="h-5 w-16 bg-slate-200 dark:bg-slate-800 rounded-full"></div>
              </div>
              <div className="space-y-2">
                <div className="h-3 w-16 bg-slate-200 dark:bg-slate-850 rounded"></div>
                <div className="h-5 w-40 bg-slate-300 dark:bg-slate-700 rounded"></div>
                <div className="h-4 w-3/4 bg-slate-200 dark:bg-slate-800 rounded"></div>
              </div>
            </div>
            <div className="h-8 w-full bg-slate-200 dark:bg-slate-800 rounded-xl mt-4"></div>
          </div>
        ))}
      </div>
    );
  }

  // Renders a generic Table Skeleton
  return (
    <div className="w-full bg-white/50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden animate-pulse">
      <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-500/5 flex items-center justify-between">
        <div className="h-4 w-32 bg-slate-200 dark:bg-slate-800 rounded"></div>
        <div className="h-4 w-20 bg-slate-200 dark:bg-slate-800 rounded"></div>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-200 dark:border-slate-800">
              <th className="py-4 px-6"><div className="h-3 w-24 bg-slate-200 dark:bg-slate-800 rounded"></div></th>
              <th className="py-4 px-6"><div className="h-3 w-32 bg-slate-200 dark:bg-slate-800 rounded"></div></th>
              <th className="py-4 px-6"><div className="h-3 w-20 bg-slate-200 dark:bg-slate-800 rounded"></div></th>
              <th className="py-4 px-6"><div className="h-3 w-16 bg-slate-200 dark:bg-slate-800 rounded"></div></th>
              <th className="py-4 px-6 text-right"><div className="h-3 w-16 bg-slate-200 dark:bg-slate-800 rounded ml-auto"></div></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200/20 dark:divide-slate-800/20">
            {[...Array(count)].map((_, i) => (
              <tr key={i} className="hover:bg-slate-500/5">
                <td className="py-5 px-6"><div className="h-4 w-36 bg-slate-200 dark:bg-slate-800 rounded"></div></td>
                <td className="py-5 px-6"><div className="h-4 w-48 bg-slate-200 dark:bg-slate-800 rounded"></div></td>
                <td className="py-5 px-6"><div className="h-4 w-24 bg-slate-200 dark:bg-slate-800 rounded"></div></td>
                <td className="py-5 px-6"><div className="h-5 w-16 bg-slate-200 dark:bg-slate-750 rounded-full"></div></td>
                <td className="py-5 px-6 text-right"><div className="h-8 w-16 bg-slate-200 dark:bg-slate-800 rounded ml-auto"></div></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
