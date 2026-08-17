import axios from 'axios';

const API_URL = '/api/v1';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach the token to every request if it exists
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Auth endpoints
export const register = (userData) => api.post('/auth/register', userData);

export const login = (credentials) => api.post('/auth/login', credentials);

export const getProfile = () => api.get('/auth/profile');

// Menu endpoints
export const getMenu = () => api.get('/menu');
export const getMenuById = (id) => api.get(`/menu/${id}`);
export const createMenu = (menuData) => api.post('/menu', menuData);
export const updateMenu = (id, menuData) => api.put(`/menu/${id}`, menuData);
export const deleteMenu = (id) => api.delete(`/menu/${id}`);

// Beans endpoints
export const getBeans = () => api.get('/beans');
export const getBeanById = (id) => api.get(`/beans/${id}`);
export const createBean = (beanData) => api.post('/beans', beanData);
export const updateBean = (id, beanData) => api.put(`/beans/${id}`, beanData);
export const deleteBean = (id) => api.delete(`/beans/${id}`);

// Vuln flags endpoint
export const getVulnFlags = () => api.get('/vuln-flags');

// Reviews endpoints
export const getReviews = (beansId) => api.get(`/beans/${beansId}/reviews`);
export const submitReview = (beansId, data) => api.post(`/beans/${beansId}/reviews`, data);
export const deleteReview = (beansId, reviewId) => api.delete(`/beans/${beansId}/reviews/${reviewId}`);

// Carts endpoints
export const getMyCart = () => api.get('/cart/my-cart');
export const getCart = (cartId) => api.get(`/cart/${cartId}`);
export const getCartOwner = (cartId) => api.get(`/cart/${cartId}/owner`);
export const addDrinkToCart = (cartId, itemData) => api.post(`/cart/${cartId}/add-drink`, itemData);
export const addBeanToCart = (cartId, beanData) => api.post(`/cart/${cartId}/add-beans`, beanData);

// Profile endpoints
export const updateEmail = (data) => api.patch('/auth/profile/email', data);
export const updatePassword = (data) => api.patch('/auth/profile/password', data);
export const uploadAvatar = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/auth/profile/avatar', formData, {headers: { 'Content-Type': undefined }});
};
export const getAvatarUrl = (userId) => `/api/v1/auth/profile/avatar/${userId}`;

// Order endpoints
export const checkout = (cartId, orderData) => api.post(`/order/checkout/${cartId}`, orderData);
export const getOrder = (orderId) => api.get(`/order/${orderId}`);
export const getMyOrders = () => api.get('/order/my-orders');
export const exportOrders = (fields) => api.post('/order/export', { fields });

// Gift card endpoints
export const redeemGiftCard = (code) => api.post('/gift-card/redeem', { code });
export const getGiftCardBalance = () => api.get('/gift-card/balance');

// Logout function to clear the token from session storage
export const logout = () => {
  api.post('/auth/logout').catch(() => {});
  sessionStorage.removeItem('token');
};
export default api;