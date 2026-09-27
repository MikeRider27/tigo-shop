import { api } from './client';

export const authApi = {
  register: (data) => api('/auth/register', { method: 'POST', body: data, auth: false }),
  login: (email, password) => api('/auth/login', { method: 'POST', body: { email, password }, auth: false }),
  forgotPassword: (email) => api('/auth/forgot-password', { method: 'POST', body: { email }, auth: false }),
  resetPassword: (token, newPassword) =>
    api('/auth/reset-password', { method: 'POST', body: { token, newPassword }, auth: false }),
};

export const userApi = {
  me: () => api('/users/me'),
  update: (data) => api('/users/me', { method: 'PUT', body: data }),
  changePassword: (currentPassword, newPassword) =>
    api('/users/me/password', { method: 'PUT', body: { currentPassword, newPassword } }),
  remove: () => api('/users/me', { method: 'DELETE' }),
};

export const catalogApi = {
  search: ({ q = '', category = '', page = 0, size = 12 } = {}) => {
    const params = new URLSearchParams({ page, size });
    if (q) params.set('q', q);
    if (category) params.set('category', category);
    return api(`/products?${params}`, { auth: false });
  },
  categories: () => api('/products/categories', { auth: false }),
};

export const cartApi = {
  get: () => api('/cart'),
  add: (productId, quantity = 1) => api('/cart/items', { method: 'POST', body: { productId, quantity } }),
  update: (productId, quantity) => api(`/cart/items/${productId}`, { method: 'PUT', body: { quantity } }),
  remove: (productId) => api(`/cart/items/${productId}`, { method: 'DELETE' }),
};

export const orderApi = {
  checkout: (shippingAddress) => api('/orders', { method: 'POST', body: { shippingAddress } }),
  list: () => api('/orders'),
  get: (id) => api(`/orders/${id}`),
  cancel: (id) => api(`/orders/${id}/cancel`, { method: 'PATCH' }),
};
