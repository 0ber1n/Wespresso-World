import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getOrder } from '../services/api';
import ErrorPage from '../components/ErrorPage';

export default function OrderConfirmation() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getOrder(orderId)
      .then((res) => setOrder(res.data))
      .catch(() => setError('Failed to load order'))
      .finally(() => setLoading(false));
  }, [orderId]);

  const openReceipt = async (beta = false) => {
    const token = sessionStorage.getItem('token');
    const url = `/api/v1/order/${order.id}/receipt${beta ? '-beta' : ''}`;
    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const html = await res.text();
    const win = window.open('', '_blank');
    win.document.write(html);
    win.document.close();
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-cream-100">
      <p className="text-brown-400 text-xs tracking-widest uppercase">Loading…</p>
    </div>
  );
  if (error) return <ErrorPage message={error} />;

  return (
    <div className="bg-cream-100 min-h-screen">
      <div className="max-w-xl mx-auto px-8 py-16">
        {/* Confirmation header */}
        <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-10 text-center mb-5">
          <div className="w-14 h-14 bg-forest-100 rounded-full flex items-center justify-center mx-auto mb-5">
            <svg className="w-7 h-7 text-forest-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <p className="text-forest-600 text-xs tracking-widest uppercase font-medium mb-3">Order confirmed</p>
          <h1 className="text-3xl font-bold text-brown-900 mb-3" style={{ fontFamily: 'var(--font-family-serif)' }}>
            We're on it.
          </h1>
          <p className="text-brown-500 text-sm">
            Thanks, <span className="font-medium text-brown-800">{order.customerName}</span>. Your order is being prepared with care.
          </p>
          <p className="text-brown-300 text-xs mt-2">Order #{order.id}</p>
        </div>

        {/* Items */}
        <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-8 mb-4">
          <p className="text-xs text-brown-400 uppercase tracking-widest mb-5">Items</p>
          <div className="divide-y divide-cream-200">
            {order.items.map((item) => (
              <div key={item.id} className="flex justify-between items-center py-3">
                <div>
                  <p className="font-medium text-brown-900 text-sm">{item.itemName}</p>
                  <p className="text-brown-400 text-xs capitalize">{item.category} · Qty {item.quantity}</p>
                </div>
                <p className="text-brown-700 text-sm font-medium">${(item.price * item.quantity).toFixed(2)}</p>
              </div>
            ))}
          </div>
          <div className="flex justify-between items-end mt-5 pt-5 border-t border-cream-300">
            <p className="text-xs text-brown-400 uppercase tracking-widest">Total</p>
            <p className="text-xl font-bold text-forest-800">${order.totalPrice.toFixed(2)}</p>
          </div>
        </div>

        {/* Shipping */}
        <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-6 mb-8">
          <p className="text-xs text-brown-400 uppercase tracking-widest mb-2">Ships to</p>
          <p className="text-brown-700 text-sm">{order.shippingAddress}</p>
        </div>

        {/* Receipt links */}
        <div className="flex gap-2 mb-3">
          <button
            onClick={() => openReceipt()}
            className="flex-1 text-center text-sm text-forest-700 border border-forest-300 bg-forest-50 hover:bg-forest-100 py-3 rounded-xl transition-colors"
          >
            View Receipt
          </button>
          <button
            onClick={() => openReceipt(true)}
            className="flex-1 text-center text-sm text-brown-400 border border-cream-300 bg-cream-100 hover:bg-cream-200 py-3 rounded-xl transition-colors"
          >
            Receipt v2 <span className="text-brown-300">[beta]</span>
          </button>
        </div>

        <button
          onClick={() => navigate('/')}
          className="w-full bg-forest-800 hover:bg-forest-700 text-cream-50 py-3 rounded-xl text-sm font-medium transition-colors shadow-warm"
        >
          Continue shopping
        </button>
      </div>
    </div>
  );
}
