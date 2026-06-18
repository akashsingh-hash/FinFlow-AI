import React from 'react';

export default function GlassCard({ children, className = '', hoverEffect = false }) {
  return (
    <div 
      className={`
        glass-card p-6 rounded-2xl transition-all duration-350 
        ${hoverEffect 
          ? 'hover:shadow-xl hover:shadow-slate-500/5 dark:hover:shadow-black/20 hover:-translate-y-0.5' 
          : 'shadow-sm'
        } 
        ${className}
      `}
    >
      {children}
    </div>
  );
}
