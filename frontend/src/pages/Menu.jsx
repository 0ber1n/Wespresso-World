import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMenu, getBeans, addBeanToCart, addDrinkToCart } from '../services/api';
import { useAuth } from '../context/AuthContext';
import ErrorPage from '../components/ErrorPage';

function Menu() {
  const [drinks, setDrinks] = useState([]);
  const [beans, setBeans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [drinksRes, beansRes] = await Promise.all([getMenu(), getBeans()]);
        setDrinks(drinksRes.data);
        setBeans(beansRes.data);
      } catch {
        setError('Failed to load menu');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(null), 2500);
  };

  const handleAddDrink = async (drinkId) => {
    try {
      await addDrinkToCart(sessionStorage.getItem('cartId'), { drinkId, quantity: 1 });
      showToast('Added to your order');
    } catch {
      setError('Failed to add to cart');
    }
  };

  const handleAddBean = async (beansId) => {
    try {
      await addBeanToCart(sessionStorage.getItem('cartId'), { beansId, quantity: 1 });
      showToast('Added to your order');
    } catch {
      setError('Failed to add to cart');
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-cream-100">
      <p className="text-brown-400 text-xs tracking-widest uppercase">Brewing the menu…</p>
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

      {/* Hero */}
      <div className="bg-forest-900 px-8 py-20 text-center relative overflow-hidden">
        <div className="absolute inset-0 opacity-5"
          style={{ backgroundImage: 'radial-gradient(circle at 20% 50%, #8cbfa4 0%, transparent 50%), radial-gradient(circle at 80% 20%, #d4703e 0%, transparent 40%)' }}
        />
        <p className="text-forest-300 text-xs tracking-widest uppercase mb-4 font-medium relative">
          Single-origin · Ethically sourced · Pacific Northwest
        </p>
        <h1 className="text-5xl font-bold text-cream-50 mb-5 relative" style={{ fontFamily: 'var(--font-family-serif)' }}>
          Our Menu
        </h1>
        <p className="text-cream-300 max-w-md mx-auto text-sm leading-relaxed relative">
          Crafted with intention. Every cup tells a story rooted in the soil, the season, and the hands that grew it.
        </p>
      </div>

      <div className="max-w-5xl mx-auto px-8 py-14">
        <Section title="Drinks">
          {drinks.map((drink) => (
            <MenuCard
              key={drink.id}
              name={drink.name}
              description={drink.description}
              price={drink.price}
              badge={null}
              user={user}
              onAdd={() => handleAddDrink(drink.id)}
              onSignIn={() => navigate('/login')}
            />
          ))}
        </Section>

        <Section title="Coffee Beans">
          {beans.map((bean) => (
            <MenuCard
              key={bean.id}
              name={bean.name}
              description={bean.description}
              price={bean.price}
              badge={bean.roastLevel}
              meta={bean.origin ? `Origin: ${bean.origin}` : null}
              user={user}
              onAdd={() => handleAddBean(bean.id)}
              onSignIn={() => navigate('/login')}
            />
          ))}
        </Section>
      </div>
    </div>
  );
}

function Section({ title, children }) {
  return (
    <div className="mb-16">
      <div className="flex items-center gap-5 mb-8">
        <h2 className="text-2xl font-bold text-brown-900 whitespace-nowrap" style={{ fontFamily: 'var(--font-family-serif)' }}>
          {title}
        </h2>
        <div className="flex-1 h-px bg-cream-300" />
      </div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {children}
      </div>
    </div>
  );
}

function MenuCard({ name, description, price, badge, meta, user, onAdd, onSignIn }) {
  return (
    <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-6 flex flex-col hover:shadow-warm-md transition-shadow">
      <div className="flex-1">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="text-base font-semibold text-brown-900" style={{ fontFamily: 'var(--font-family-serif)' }}>
            {name}
          </h3>
          {badge && (
            <span className="shrink-0 text-xs bg-brown-100 text-brown-600 px-2 py-0.5 rounded-full border border-brown-200">
              {badge}
            </span>
          )}
        </div>
        <p className="text-brown-500 text-sm leading-relaxed">{description}</p>
        {meta && <p className="text-brown-400 text-xs mt-2 uppercase tracking-wide">{meta}</p>}
      </div>
      <div className="flex items-center justify-between mt-5 pt-4 border-t border-cream-300">
        <span className="text-forest-700 font-semibold">${price.toFixed(2)}</span>
        {user ? (
          <button
            onClick={onAdd}
            className="text-sm bg-forest-800 hover:bg-forest-700 text-cream-50 px-4 py-1.5 rounded-lg transition-colors font-medium"
          >
            Add to cart
          </button>
        ) : (
          <button onClick={onSignIn} className="text-sm text-brown-400 hover:text-brown-700 transition-colors">
            Sign in to order →
          </button>
        )}
      </div>
    </div>
  );
}

export default Menu;
