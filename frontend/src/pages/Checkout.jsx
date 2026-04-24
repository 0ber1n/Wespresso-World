import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCart, checkout } from '../services/api';
import ErrorPage from '../components/ErrorPage';

const inputCls = 'w-full border border-cream-400 bg-cream-50 rounded-xl px-4 py-2.5 text-sm text-brown-900 placeholder-brown-300 focus:outline-none focus:ring-2 focus:ring-forest-600 focus:border-transparent transition';
const labelCls = 'block text-brown-700 text-sm font-medium mb-1.5';

export default function Checkout() {
  const { cartId } = useParams();
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ fullName: '', street: '', city: '', zip: '', country: '', cardNumber: '', expiry: '', cvv: '' });

  useEffect(() => {
    getCart(cartId)
      .then((res) => { setCart(res.data); setForm((f) => ({ ...f, fullName: res.data.customerName })); })
      .catch(() => setError('Failed to load cart'))
      .finally(() => setLoading(false));
  }, [cartId]);

  const set = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const shippingAddress = `${form.street}, ${form.city}, ${form.zip}, ${form.country}`;
      const res = await checkout(cartId, { shippingAddress });
      navigate(`/order/${res.data.id}`);
    } catch {
      setError('Checkout failed. Please try again.');
      setSubmitting(false);
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-cream-100">
      <p className="text-brown-400 text-xs tracking-widest uppercase">Loading…</p>
    </div>
  );
  if (error && !cart) return <ErrorPage message={error} />;

  return (
    <div className="bg-cream-100 min-h-screen">
      <div className="max-w-5xl mx-auto px-8 py-12">
        <h1 className="text-4xl font-bold text-brown-900 mb-10" style={{ fontFamily: 'var(--font-family-serif)' }}>
          Checkout
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Order summary */}
          <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-8 h-fit">
            <p className="text-xs text-brown-400 uppercase tracking-widest mb-5">Order Summary</p>
            <div className="divide-y divide-cream-200">
              {cart.items.map((item) => (
                <div key={item.id} className="flex justify-between items-center py-3">
                  <div>
                    <p className="font-medium text-brown-900 text-sm">{item.itemName}</p>
                    <p className="text-brown-400 text-xs">Qty {item.quantity}</p>
                  </div>
                  <p className="text-brown-700 text-sm font-medium">${(item.price * item.quantity).toFixed(2)}</p>
                </div>
              ))}
            </div>
            <div className="flex justify-between items-end mt-6 pt-5 border-t border-cream-300">
              <p className="text-xs text-brown-400 uppercase tracking-widest">Total</p>
              <p className="text-2xl font-bold text-forest-800">${cart.totalPrice.toFixed(2)}</p>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-8 flex flex-col gap-5">
            <p className="text-xs text-brown-400 uppercase tracking-widest">Shipping</p>

            {error && <p className="text-terra-600 text-sm bg-terra-100 border border-terra-400 px-4 py-2.5 rounded-xl">{error}</p>}

            <div>
              <label className={labelCls}>Full Name</label>
              <input name="fullName" value={form.fullName} onChange={set} required className={inputCls} />
            </div>
            <div>
              <label className={labelCls}>Street Address</label>
              <input name="street" value={form.street} onChange={set} required placeholder="123 Pine St" className={inputCls} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className={labelCls}>City</label>
                <input name="city" value={form.city} onChange={set} required className={inputCls} />
              </div>
              <div>
                <label className={labelCls}>ZIP</label>
                <input name="zip" value={form.zip} onChange={set} required className={inputCls} />
              </div>
            </div>
            <div>
              <label className={labelCls}>Country</label>
              <input name="country" value={form.country} onChange={set} required placeholder="USA" className={inputCls} />
            </div>

            <p className="text-xs text-brown-400 uppercase tracking-widest pt-2 border-t border-cream-300">Payment</p>

            <div>
              <label className={labelCls}>Card Number</label>
              <input name="cardNumber" value={form.cardNumber} onChange={set} required placeholder="•••• •••• •••• ••••" maxLength={19} className={inputCls} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className={labelCls}>Expiry</label>
                <input name="expiry" value={form.expiry} onChange={set} required placeholder="MM/YY" maxLength={5} className={inputCls} />
              </div>
              <div>
                <label className={labelCls}>CVV</label>
                <input name="cvv" value={form.cvv} onChange={set} required placeholder="•••" maxLength={4} className={inputCls} />
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting || cart.items.length === 0}
              className="mt-1 w-full bg-forest-800 hover:bg-forest-700 disabled:bg-cream-300 disabled:text-brown-400 text-cream-50 py-3 rounded-xl text-sm font-medium transition-colors shadow-warm"
            >
              {submitting ? 'Placing order…' : `Place Order — $${cart.totalPrice.toFixed(2)}`}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
