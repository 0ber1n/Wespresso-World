import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMenu, getBeans, addBeanToCart, addDrinkToCart, getReviews, submitReview, deleteReview, getVulnFlags } from '../services/api';
import { useAuth } from '../context/AuthContext';
import ErrorPage from '../components/ErrorPage';

function Menu() {
  const [drinks, setDrinks] = useState([]);
  const [beans, setBeans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const [openBeanId, setOpenBeanId] = useState(null);
  const [reviewsCache, setReviewsCache] = useState({});
  const [vulnFlags, setVulnFlags] = useState({});
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [drinksRes, beansRes, flagsRes] = await Promise.all([getMenu(), getBeans(), getVulnFlags()]);
        setDrinks(drinksRes.data);
        setBeans(beansRes.data);
        setVulnFlags(flagsRes.data);
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

  const handleCacheUpdate = useCallback((beanId, reviews) => {
    setReviewsCache((prev) => ({ ...prev, [beanId]: reviews }));
  }, []);

  const openBean = beans.find((b) => b.id === openBeanId) ?? null;

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
            <BeanCard
              key={bean.id}
              bean={bean}
              user={user}
              cachedReviews={reviewsCache[bean.id] ?? []}
              onAdd={() => handleAddBean(bean.id)}
              onSignIn={() => navigate('/login')}
              onOpenReviews={() => setOpenBeanId(bean.id)}
            />
          ))}
        </Section>
      </div>

      {openBean && (
        <ReviewModal
          bean={openBean}
          user={user}
          vulnFlags={vulnFlags}
          onClose={() => setOpenBeanId(null)}
          onCacheUpdate={handleCacheUpdate}
        />
      )}
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

function CoffeeBeanIcon({ filled, size = 20 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
      <ellipse
        cx="10" cy="10" rx="4.5" ry="7.5"
        transform="rotate(30 10 10)"
        fill={filled ? '#6b3e26' : 'none'}
        stroke={filled ? '#6b3e26' : '#c4a882'}
        strokeWidth="1.5"
      />
      <path
        d="M14 4 C10 8 10 12 6 17"
        stroke="#c4a882"
        strokeWidth="1"
        fill="none"
        strokeLinecap="round"
      />
    </svg>
  );
}

function BeanCard({ bean, cachedReviews, user, onAdd, onSignIn, onOpenReviews }) {
  const avgRating = cachedReviews.length
    ? (cachedReviews.reduce((sum, r) => sum + r.rating, 0) / cachedReviews.length).toFixed(1)
    : null;

  return (
    <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-6 flex flex-col hover:shadow-warm-md transition-shadow">
      <div className="flex-1">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="text-base font-semibold text-brown-900" style={{ fontFamily: 'var(--font-family-serif)' }}>
            {bean.name}
          </h3>
          {bean.roastLevel && (
            <span className="shrink-0 text-xs bg-brown-100 text-brown-600 px-2 py-0.5 rounded-full border border-brown-200">
              {bean.roastLevel}
            </span>
          )}
        </div>
        <p className="text-brown-500 text-sm leading-relaxed">{bean.description}</p>
        {bean.origin && <p className="text-brown-400 text-xs mt-2 uppercase tracking-wide">Origin: {bean.origin}</p>}

        {avgRating && (
          <div className="flex items-center gap-1.5 mt-2">
            <CoffeeBeanIcon filled size={14} />
            <span className="text-xs text-brown-600 font-medium">{avgRating} / 5</span>
            <span className="text-xs text-brown-400">({cachedReviews.length} review{cachedReviews.length !== 1 ? 's' : ''})</span>
          </div>
        )}
      </div>

      <div className="flex items-center justify-between mt-5 pt-4 border-t border-cream-300">
        <span className="text-forest-700 font-semibold">${bean.price.toFixed(2)}</span>
        <div className="flex items-center gap-2">
          <button
            onClick={onOpenReviews}
            className="text-xs text-brown-500 hover:text-brown-800 border border-brown-200 px-3 py-1.5 rounded-lg transition-colors"
          >
            Reviews
          </button>
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
    </div>
  );
}

function ReviewModal({ bean, user, vulnFlags, onClose, onCacheUpdate }) {
  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(true);
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [reviewError, setReviewError] = useState(null);

  const fetchReviews = useCallback(async () => {
    setLoadingReviews(true);
    try {
      const res = await getReviews(bean.id);
      setReviews(res.data);
      onCacheUpdate(bean.id, res.data);
    } catch {
      // silent
    } finally {
      setLoadingReviews(false);
    }
  }, [bean.id, onCacheUpdate]);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (rating === 0) { setReviewError('Please select a rating'); return; }
    setSubmitting(true);
    setReviewError(null);
    try {
      await submitReview(bean.id, { rating, comment });
      await fetchReviews();
      setRating(0);
      setComment('');
    } catch (err) {
      const msg = err?.response?.data?.error;
      setReviewError(msg || 'Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (reviewId) => {
    try {
      await deleteReview(bean.id, reviewId);
      const updated = reviews.filter((r) => r.id !== reviewId);
      setReviews(updated);
      onCacheUpdate(bean.id, updated);
    } catch {
      // silent
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      onClick={onClose}
    >
      <div className="absolute inset-0 bg-brown-900 opacity-60" />

      <div
        className="relative bg-cream-50 rounded-2xl shadow-warm-md w-full max-w-lg mx-4 flex flex-col"
        style={{ maxHeight: '82vh' }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-cream-300 shrink-0">
          <h2 className="text-lg font-bold text-brown-900" style={{ fontFamily: 'var(--font-family-serif)' }}>
            {bean.name}
          </h2>
          <button
            onClick={onClose}
            className="text-brown-400 hover:text-brown-700 transition-colors text-lg leading-none ml-4"
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        {/* Scrollable body */}
        <div className="flex-1 overflow-y-auto px-6 py-5">
          {user && (
            <form onSubmit={handleSubmitReview} className="mb-6 pb-5 border-b border-cream-200">
              <p className="text-xs font-semibold text-brown-700 mb-2 uppercase tracking-wide">Leave a review</p>
              <div className="flex items-center gap-1 mb-3">
                {[1, 2, 3, 4, 5].map((n) => (
                  <button
                    key={n}
                    type="button"
                    onClick={() => setRating(n)}
                    onMouseEnter={() => setHoverRating(n)}
                    onMouseLeave={() => setHoverRating(0)}
                    className="focus:outline-none"
                  >
                    <CoffeeBeanIcon filled={(hoverRating || rating) >= n} size={24} />
                  </button>
                ))}
              </div>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Share your thoughts…"
                rows={3}
                className="w-full text-sm border border-cream-300 rounded-lg px-3 py-2 bg-cream-100 text-brown-800 placeholder-brown-300 resize-none focus:outline-none focus:border-brown-400"
              />
              {reviewError && <p className="text-red-500 text-xs mt-1">{reviewError}</p>}
              <button
                type="submit"
                disabled={submitting}
                className="mt-2 text-sm bg-brown-600 hover:bg-brown-700 text-cream-50 px-4 py-1.5 rounded-lg transition-colors font-medium disabled:opacity-50"
              >
                {submitting ? 'Posting…' : 'Post Review'}
              </button>
            </form>
          )}

          {loadingReviews ? (
            <p className="text-xs text-brown-400 text-center py-8">Loading reviews…</p>
          ) : reviews.length === 0 ? (
            <p className="text-xs text-brown-400 text-center py-8">No reviews yet. Be the first!</p>
          ) : (
            <div className="space-y-3">
              {reviews.map((review) => (
                <div key={review.id} className="bg-cream-100 rounded-xl p-4">
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold text-brown-800">{review.username}</span>
                      <div className="flex items-center gap-0.5">
                        {[1, 2, 3, 4, 5].map((n) => (
                          <CoffeeBeanIcon key={n} filled={review.rating >= n} size={13} />
                        ))}
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-brown-400">
                        {new Date(review.createdAt).toLocaleDateString()}
                      </span>
                      {user?.role === 'admin' && (
                        <button
                          onClick={() => handleDelete(review.id)}
                          className="text-brown-400 hover:text-red-500 transition-colors text-xs leading-none"
                          title="Delete review"
                        >
                          ✕
                        </button>
                      )}
                    </div>
                  </div>
                  {vulnFlags.storedXss
                    ? <p className="text-sm text-brown-600" dangerouslySetInnerHTML={{ __html: review.comment }} />
                    : <p className="text-sm text-brown-600">{review.comment}</p>
                  }
                </div>
              ))}
            </div>
          )}
        </div>
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
