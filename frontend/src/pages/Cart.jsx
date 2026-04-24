import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCart, getMenu, getBeans, addBeanToCart, addDrinkToCart } from '../services/api';
import ErrorPage from '../components/ErrorPage';

export default function Cart() {
  const { cartId } = useParams();
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [menu, setMenu] = useState([]);
  const [beans, setBeans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cartRes, menuRes, beansRes] = await Promise.all([getCart(cartId), getMenu(), getBeans()]);
        setCart(cartRes.data);
        setMenu(menuRes.data);
        setBeans(beansRes.data);
      } catch {
        setError('Failed to load cart');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [cartId]);

  const showToast = (msg) => { setToast(msg); setTimeout(() => setToast(null), 2000); };

  const handleAddDrink = async (drinkId) => {
    try { const res = await addDrinkToCart(cartId, { drinkId, quantity: 1 }); setCart(res.data); showToast('Added'); }
    catch { setError('Failed to add to cart'); }
  };

  const handleAddBean = async (beansId) => {
    try { const res = await addBeanToCart(cartId, { beansId, quantity: 1 }); setCart(res.data); showToast('Added'); }
    catch { setError('Failed to add to cart'); }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-cream-100">
      <p className="text-brown-400 text-xs tracking-widest uppercase">Loading cart…</p>
    </div>
  );
  if (error) return <ErrorPage message={error} />;

  return (
    <div className="bg-cream-100 min-h-screen">
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 bg-forest-900 text-cream-100 text-sm px-5 py-3 rounded-xl shadow-warm-md">
          {toast}
        </div>
      )}

      <div className="max-w-5xl mx-auto px-8 py-12">
        <h1 className="text-4xl font-bold text-brown-900 mb-10" style={{ fontFamily: 'var(--font-family-serif)' }}>
          Your Cart
        </h1>

        {/* Summary */}
        <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-8 mb-10">
          <p className="text-xs text-brown-400 uppercase tracking-widest mb-6">{cart.customerName}'s order</p>

          {cart.items.length === 0 ? (
            <p className="text-brown-400 text-center py-8">Your cart is empty — add something from below.</p>
          ) : (
            <div className="divide-y divide-cream-200">
              {cart.items.map((item) => (
                <div key={item.id} className="flex justify-between items-center py-4">
                  <div>
                    <p className="font-medium text-brown-900">{item.itemName}</p>
                    <p className="text-brown-400 text-sm">Qty {item.quantity}</p>
                  </div>
                  <p className="text-brown-700 font-medium">${(item.price * item.quantity).toFixed(2)}</p>
                </div>
              ))}
            </div>
          )}

          <div className="flex justify-between items-center mt-6 pt-6 border-t border-cream-300">
            <div>
              <p className="text-xs text-brown-400 uppercase tracking-widest mb-0.5">Total</p>
              <p className="text-2xl font-bold text-forest-800">${cart.totalPrice.toFixed(2)}</p>
            </div>
            <button
              onClick={() => navigate(`/checkout/${cartId}`)}
              disabled={cart.items.length === 0}
              className="bg-forest-800 hover:bg-forest-700 disabled:bg-cream-300 disabled:text-brown-400 text-cream-50 px-7 py-2.5 rounded-xl text-sm font-medium transition-colors shadow-warm"
            >
              Proceed to checkout
            </button>
          </div>
        </div>

        {/* Add more */}
        <AddSection title="Add Drinks" items={menu} onAdd={handleAddDrink} />
        <AddSection title="Add Beans" items={beans} onAdd={handleAddBean} />
      </div>
    </div>
  );
}

function AddSection({ title, items, onAdd }) {
  return (
    <div className="mb-10">
      <div className="flex items-center gap-5 mb-5">
        <h2 className="text-xl font-bold text-brown-900 whitespace-nowrap" style={{ fontFamily: 'var(--font-family-serif)' }}>
          {title}
        </h2>
        <div className="flex-1 h-px bg-cream-300" />
      </div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {items.map((item) => (
          <div key={item.id} className="bg-cream-50 border border-cream-300 rounded-2xl p-5 flex justify-between items-center shadow-warm hover:shadow-warm-md transition-shadow">
            <div>
              <p className="font-medium text-brown-900 text-sm">{item.name}</p>
              <p className="text-forest-700 text-sm font-medium mt-0.5">${item.price.toFixed(2)}</p>
            </div>
            <button
              onClick={() => onAdd(item.id)}
              className="text-xs bg-cream-200 hover:bg-forest-800 hover:text-cream-50 text-brown-700 border border-cream-300 hover:border-forest-800 px-4 py-1.5 rounded-lg transition-colors font-medium"
            >
              + Add
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
