import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyOrders } from '../services/api';
import ErrorPage from '../components/ErrorPage';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedId, setExpandedId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    getMyOrders()
      .then((res) => setOrders([...res.data].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))))
      .catch(() => setError('Failed to load orders'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-cream-100">
      <p className="text-brown-400 text-xs tracking-widest uppercase">Loading orders…</p>
    </div>
  );
  if (error) return <ErrorPage message={error} />;

  return (
    <div className="bg-cream-100 min-h-screen">
      <div className="max-w-3xl mx-auto px-8 py-12">
        <h1 className="text-4xl font-bold text-brown-900 mb-10" style={{ fontFamily: 'var(--font-family-serif)' }}>
          Order History
        </h1>

        {orders.length === 0 ? (
          <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-14 text-center">
            <p className="text-brown-400 mb-6 text-sm">No orders yet — time to treat yourself.</p>
            <button
              onClick={() => navigate('/')}
              className="bg-forest-800 hover:bg-forest-700 text-cream-50 px-6 py-2.5 rounded-xl text-sm font-medium transition-colors shadow-warm"
            >
              Browse menu
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {orders.map((order) => (
              <div key={order.id} className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm overflow-hidden">
                <button
                  className="w-full text-left px-6 py-5 flex justify-between items-center hover:bg-cream-100 transition-colors"
                  onClick={() => setExpandedId(expandedId === order.id ? null : order.id)}
                >
                  <div>
                    <p className="font-semibold text-brown-900 text-sm" style={{ fontFamily: 'var(--font-family-serif)' }}>
                      Order #{order.id}
                    </p>
                    <p className="text-brown-400 text-xs mt-0.5">
                      {new Date(order.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                    </p>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <p className="font-bold text-forest-800">${order.totalPrice.toFixed(2)}</p>
                      <span className="text-xs text-brown-400 bg-cream-200 px-2 py-0.5 rounded-full border border-cream-300">
                        {order.status}
                      </span>
                    </div>
                    <svg
                      className={`w-4 h-4 text-brown-300 transition-transform ${expandedId === order.id ? 'rotate-180' : ''}`}
                      fill="none" viewBox="0 0 24 24" stroke="currentColor"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                </button>

                {expandedId === order.id && (
                  <div className="px-6 pb-6 border-t border-cream-200">
                    <p className="text-brown-400 text-xs mt-4 mb-4">Ships to: {order.shippingAddress}</p>
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
                    <div className="flex justify-between items-end mt-4 pt-4 border-t border-cream-200">
                      <p className="text-xs text-brown-400 uppercase tracking-widest">Total</p>
                      <p className="font-bold text-forest-800">${order.totalPrice.toFixed(2)}</p>
                    </div>

                    <div className="flex gap-2 mt-4">
  
                        href={`/api/v1/order/${order.id}/receipt`}
                        target="_blank"
                        rel="noreferrer"
                        className="text-xs text-forest-700 border border-forest-300 bg-forest-50 hover:bg-forest-100 px-4 py-2 rounded-xl transition-colors"
                      <a>
                        View Receipt
                      </a>
                      
                        href={`/api/v1/order/${order.id}/receipt-beta`}
                        target="_blank"
                        rel="noreferrer"
                        className="text-xs text-brown-400 border border-cream-300 bg-cream-100 hover:bg-cream-200 px-4 py-2 rounded-xl transition-colors"
                      <a>
                        Receipt v2 <span className="text-brown-300">[beta]</span>
                      </a>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
