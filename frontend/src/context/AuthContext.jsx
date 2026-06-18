import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [loading, setLoading] = useState(true);

  const login = async (email, password) => {
    try {
      const response = await authAPI.login({ email, password });
      const { accessToken, refreshToken, user: userData } = response.data;
      
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      return { success: true };
    } catch (err) {
      return { success: false, error: err.message || 'Login failed' };
    }
  };

  const register = async (data) => {
    try {
      await authAPI.register(data);
      return { success: true };
    } catch (err) {
      return { success: false, error: err.message || 'Registration failed' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  const changePassword = async (oldPassword, newPassword) => {
    try {
      await authAPI.changePassword({ oldPassword, newPassword });
      return { success: true };
    } catch (err) {
      return { success: false, error: err.message || 'Password update failed' };
    }
  };

  useEffect(() => {
    // Check if token exists on load
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
    } else {
      logout();
    }
    setLoading(false);

    // Global listener for automatic logouts triggered by Axios interceptor
    const handleLogoutEvent = () => {
      logout();
    };

    window.addEventListener('auth-logout', handleLogoutEvent);
    return () => {
      window.removeEventListener('auth-logout', handleLogoutEvent);
    };
  }, []);

  const hasRole = (allowedRoles) => {
    if (!user) return false;
    return allowedRoles.includes(user.role);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, changePassword, loading, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
