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

// Carts endpoints
export const createCart = (cartData) => api.post('/cart', cartData);
export const getCart = (cartId) => api.get(`/cart/${cartId}`);
export const getCartOwner = (cartId) => api.get(`/cart/${cartId}/owner`);
export const addDrinkToCart = (cartId, itemData) => api.post(`/cart/${cartId}/add-drink`, itemData);
export const addBeanToCart = (cartId, beanData) => api.post(`/cart/${cartId}/add-beans`, beanData);

// Logout function to clear the token from session storage
export const logout = () => {
  sessionStorage.removeItem('token');
};

export default api;