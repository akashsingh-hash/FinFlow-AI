import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
});

// Request Interceptor: Attach JWT Access Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Queue to hold failed requests while refreshing the token
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response Interceptor: Auto Refresh Tokens on 401
api.interceptors.response.use(
  (response) => response.data, // Strip axios envelope, return standardized ApiResponse
  async (error) => {
    const originalRequest = error.config;

    // Do not attempt refresh on auth paths
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url.includes('/auth/login') &&
      !originalRequest.url.includes('/auth/refresh')
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = 'Bearer ' + token;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        isRefreshing = false;
        window.dispatchEvent(new Event('auth-logout'));
        return Promise.reject(error);
      }

      try {
        // Direct axios call to prevent circular interceptor calls
        const response = await axios.post(`${import.meta.env.VITE_API_URL || '/api'}/auth/refresh`, { refreshToken });
        const { accessToken, refreshToken: newRefreshToken, user } = response.data.data;

        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        localStorage.setItem('user', JSON.stringify(user));

        api.defaults.headers.common['Authorization'] = 'Bearer ' + accessToken;
        originalRequest.headers.Authorization = 'Bearer ' + accessToken;

        processQueue(null, accessToken);
        isRefreshing = false;
        
        // Retry original request
        return api(originalRequest);
      } catch (err) {
        processQueue(err, null);
        isRefreshing = false;
        
        // Clean session and log out
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        window.dispatchEvent(new Event('auth-logout'));
        
        return Promise.reject(err);
      }
    }

    return Promise.reject(error.response?.data || { success: false, message: 'Network Connection Error' });
  }
);

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  refresh: (data) => api.post('/auth/refresh', data),
  changePassword: (data) => api.post('/auth/change-password', data),
  forgotPassword: (data) => api.post('/auth/forgot-password', data),
};

export const companyAPI = {
  getProfile: (id) => api.get(`/companies/${id}`),
  updateSettings: (id, data) => api.put(`/companies/${id}`, data),
};

export const userAPI = {
  invite: (data) => api.post('/users/invite', data),
  list: () => api.get('/users'),
  activate: (id) => api.put(`/users/${id}/activate`),
  deactivate: (id) => api.put(`/users/${id}/deactivate`),
  assignRole: (id, role) => api.put(`/users/${id}/role?role=${role}`),
};

export const budgetAPI = {
  create: (data) => api.post('/budgets', data),
  update: (id, data) => api.put(`/budgets/${id}`, data),
  list: () => api.get('/budgets'),
  getById: (id) => api.get(`/budgets/${id}`),
  getActive: (department) => api.get(`/budgets/active?department=${department}`),
};

export const vendorAPI = {
  create: (data) => api.post('/vendors', data),
  update: (id, data) => api.put(`/vendors/${id}`, data),
  delete: (id) => api.delete(`/vendors/${id}`),
  list: () => api.get('/vendors'),
  getById: (id) => api.get(`/vendors/${id}`),
};

export const invoiceAPI = {
  create: (formData) => api.post('/invoices', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  update: (id, data) => api.put(`/invoices/${id}`, data),
  pay: (id) => api.patch(`/invoices/${id}/pay`),
  list: () => api.get('/invoices'),
  getById: (id) => api.get(`/invoices/${id}`),
  listByStatus: (status) => api.get(`/invoices/status?status=${status}`),
};

export const expenseAPI = {
  createDraft: (formData) => api.post('/expenses', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  submit: (id) => api.post(`/expenses/${id}/submit`),
  update: (id, formData) => api.put(`/expenses/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  delete: (id) => api.delete(`/expenses/${id}`),
  getById: (id) => api.get(`/expenses/${id}`),
  getMyExpenses: () => api.get('/expenses/my'),
  getCompanyExpenses: () => api.get('/expenses'),
  getManagerReviewList: () => api.get('/expenses/review'),
};

export const approvalAPI = {
  approve: (id, comments) => api.patch(`/approvals/${id}/approve`, { comments }),
  reject: (id, comments) => api.patch(`/approvals/${id}/reject`, { comments }),
  claim: (id) => api.patch(`/approvals/${id}/claim`),
  getPending: () => api.get('/approvals/pending'),
  getHistory: (expenseId) => api.get(`/approvals/history/${expenseId}`),
};

export const reimbursementAPI = {
  pay: (id, data) => api.patch(`/reimbursements/${id}/pay`, data),
  list: () => api.get('/reimbursements'),
  getMyList: () => api.get('/reimbursements/my'),
  getById: (id) => api.get(`/reimbursements/${id}`),
};

export const dashboardAPI = {
  getStats: () => api.get('/dashboard/stats'),
};

export const analyticsAPI = {
  getMonthlySpend: () => api.get('/analytics/monthly-spend'),
  getDepartmentSpend: () => api.get('/analytics/department-spend'),
  getVendorSpend: () => api.get('/analytics/vendor-spend'),
  getBudgetConsumption: () => api.get('/analytics/budget-consumption'),
};

export const auditAPI = {
  getLogs: () => api.get('/audit-logs'),
};

export default api;
