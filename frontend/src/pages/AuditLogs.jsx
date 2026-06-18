import React, { useState, useEffect } from 'react';
import { auditAPI } from '../services/api';
import GlassCard from '../components/GlassCard';
import { ShieldAlert, Terminal, Eye, EyeOff, Search } from 'lucide-react';
import Skeleton from '../components/Skeleton';

export default function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  const fetchLogs = async () => {
    try {
      const res = await auditAPI.getLogs();
      setLogs(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const toggleExpand = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const formatJson = (str) => {
    if (!str) return 'N/A';
    try {
      const obj = JSON.parse(str);
      return JSON.stringify(obj, null, 2);
    } catch (e) {
      return str;
    }
  };

  const filteredLogs = logs.filter(log => 
    log.userEmail.toLowerCase().includes(search.toLowerCase()) ||
    log.action.toLowerCase().includes(search.toLowerCase()) ||
    log.entityType.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header Search Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200/50 dark:border-slate-800/50">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-3 text-slate-400" size={16} />
          <input
            type="text"
            placeholder="Filter audit logs..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
          />
        </div>
      </div>

      {/* Directory Table */}
      {loading ? (
        <Skeleton type="table" count={8} />
      ) : filteredLogs.length > 0 ? (
        <GlassCard className="overflow-hidden p-0 border border-slate-200 dark:border-slate-800">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-500/5 border-b border-slate-200/60 dark:border-slate-800/80 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-3.5 px-6">Timestamp</th>
                  <th className="py-3.5 px-6">User Context</th>
                  <th className="py-3.5 px-6">Action Triggered</th>
                  <th className="py-3.5 px-6">Target Entity</th>
                  <th className="py-3.5 px-6">Entity ID</th>
                  <th className="py-3.5 px-6 text-right">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200/40 dark:divide-slate-800/40 text-sm text-slate-750 dark:text-slate-300">
                {filteredLogs.map((log) => (
                  <React.Fragment key={log.id}>
                    <tr className="hover:bg-slate-500/5 transition-colors">
                      <td className="py-4 px-6 text-xs text-slate-400 font-mono">
                        {log.timestamp ? log.timestamp.replace('T', ' ').substring(0, 19) : 'N/A'}
                      </td>
                      <td className="py-4 px-6 font-semibold">{log.userEmail}</td>
                      <td className="py-4 px-6">
                        <span className="px-2.5 py-0.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-xs font-semibold uppercase tracking-wide">
                          {log.action}
                        </span>
                      </td>
                      <td className="py-4 px-6 font-medium">{log.entityType}</td>
                      <td className="py-4 px-6 font-mono text-xs">{log.entityId || 'N/A'}</td>
                      <td className="py-4 px-6 text-right">
                        <button
                          onClick={() => toggleExpand(log.id)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-950/20 transition-all cursor-pointer"
                          title="Toggle JSON details"
                        >
                          {expandedId === log.id ? <EyeOff size={15} /> : <Eye size={15} />}
                        </button>
                      </td>
                    </tr>

                    {/* Collapsible details layout */}
                    {expandedId === log.id && (
                      <tr className="bg-slate-50 dark:bg-slate-950/30">
                        <td colSpan={6} className="py-4 px-8 border-b border-slate-200 dark:border-slate-800">
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs font-mono">
                            {/* Old State */}
                            <div>
                              <p className="font-sans font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2 flex items-center"><Terminal size={12} className="mr-1" /> Original State (Pre Change)</p>
                              <pre className="p-4 rounded-xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 overflow-x-auto text-slate-650 dark:text-slate-350 max-h-48">
                                {formatJson(log.oldValue)}
                              </pre>
                            </div>
                            {/* New State */}
                            <div>
                              <p className="font-sans font-bold text-emerald-500/80 uppercase tracking-wider mb-2 flex items-center"><Terminal size={12} className="mr-1" /> Updated State (Post Change)</p>
                              <pre className="p-4 rounded-xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 overflow-x-auto text-slate-650 dark:text-slate-350 max-h-48">
                                {formatJson(log.newValue)}
                              </pre>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>
        </GlassCard>
      ) : (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-400 dark:text-slate-500 text-sm font-semibold flex flex-col items-center space-y-2">
          <ShieldAlert size={24} className="text-slate-450" />
          <span>No audit trail entries recorded yet.</span>
        </div>
      )}
    </div>
  );
}
