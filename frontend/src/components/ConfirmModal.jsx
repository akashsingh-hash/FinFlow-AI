import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, Info, CheckCircle, X } from 'lucide-react';

export default function ConfirmModal({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title, 
  message, 
  confirmText = 'Confirm', 
  cancelText = 'Cancel',
  variant = 'danger' // 'danger' | 'info' | 'success'
}) {
  
  // Close on Escape key
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      window.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden'; // Lock background scroll
    }
    return () => {
      window.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, onClose]);

  const getVariantStyles = () => {
    switch (variant) {
      case 'danger':
        return {
          icon: <AlertTriangle className="text-rose-500" size={24} />,
          bgIcon: 'bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/50',
          btnConfirm: 'bg-rose-500 hover:bg-rose-600 text-white shadow-rose-500/15',
        };
      case 'success':
        return {
          icon: <CheckCircle className="text-emerald-500" size={24} />,
          bgIcon: 'bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/50',
          btnConfirm: 'bg-emerald-500 hover:bg-emerald-600 text-white shadow-emerald-500/15',
        };
      default: // info
        return {
          icon: <Info className="text-blue-500" size={24} />,
          bgIcon: 'bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-900/50',
          btnConfirm: 'bg-blue-500 hover:bg-blue-600 text-white shadow-blue-500/15',
        };
    }
  };

  const styles = getVariantStyles();

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Animated Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.5 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm"
          />

          {/* Modal Card */}
          <motion.div
            initial={{ scale: 0.95, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.95, opacity: 0, y: 20 }}
            transition={{ type: 'spring', duration: 0.3 }}
            className="w-full max-w-md glass-panel p-6 rounded-2xl shadow-2xl relative z-10 bg-white/95 dark:bg-slate-900/95 border border-slate-200/50 dark:border-slate-800/80"
          >
            <div className="flex items-start space-x-4">
              <div className={`p-3 rounded-xl shrink-0 ${styles.bgIcon}`}>
                {styles.icon}
              </div>
              <div className="flex-1 space-y-1">
                <h3 className="text-base font-bold text-slate-850 dark:text-white tracking-tight">
                  {title}
                </h3>
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">
                  {message}
                </p>
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6 pt-4 border-t border-slate-200/40 dark:border-slate-800/55">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 rounded-xl text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 text-xs font-bold transition-all cursor-pointer"
              >
                {cancelText}
              </button>
              <button
                type="button"
                onClick={() => {
                  onConfirm();
                  onClose();
                }}
                className={`px-5 py-2 rounded-xl text-xs font-bold shadow-md transition-all cursor-pointer ${styles.btnConfirm}`}
              >
                {confirmText}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}
