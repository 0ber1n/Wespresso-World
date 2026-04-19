import axios from 'axios';

const API_URL = 'http://localhost:1337/api/v1';

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
export const getMenus = () => api.get('/menus');
export const getMenuById = (id) => api.get(`/menus/${id}`);
export const createMenu = (menuData) => api.post('/menus', menuData);
export const updateMenu = (id, menuData) => api.put(`/menus/${id}`, menuData);
export const deleteMenu = (id) => api.delete(`/menus/${id}`);

// Beans endpoints
export const getBeans = () => api.get('/beans');
export const getBeanById = (id) => api.get(`/beans/${id}`);
export const createBean = (beanData) => api.post('/beans', beanData);
export const updateBean = (id, beanData) => api.put(`/beans/${id}`, beanData);
export const deleteBean = (id) => api.delete(`/beans/${id}`);

// Carts endpoints
export const getCart = () => api.get('/carts');
export const addToCart = (itemData) => api.post('/carts', itemData);
export const updateCartItem = (id, itemData) => api.put(`/carts/${id}`, itemData);
export const removeFromCart = (id) => api.delete(`/carts/${id}`);