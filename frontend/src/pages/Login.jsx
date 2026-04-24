import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login, getMyCart, getProfile } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loggedIn, setLoggedIn] = useState(false);
  const navigate = useNavigate();
  const { login: authLogin } = useAuth();

  useEffect(() => { if (loggedIn) navigate('/'); }, [loggedIn]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await login(credentials);
      sessionStorage.setItem('token', res.data.token);
      const [profile, cart] = await Promise.all([getProfile(), getMyCart()]);
      sessionStorage.setItem('cartId', cart.data.id);
      authLogin(profile.data, res.data.token);
      setLoggedIn(true);
    } catch {
      setError('Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-cream-100 flex">
      {/* Left panel */}
      <div className="hidden md:flex md:w-1/2 bg-forest-900 flex-col items-center justify-center p-14 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10"
          style={{ backgroundImage: 'radial-gradient(circle at 30% 70%, #8cbfa4 0%, transparent 60%), radial-gradient(circle at 70% 20%, #d4703e 0%, transparent 50%)' }}
        />
        <div className="relative text-center max-w-xs">
          <p className="text-forest-300 text-xs tracking-widest uppercase mb-5 font-medium">Welcome back</p>
          <h2 className="text-4xl font-bold text-cream-50 mb-4 leading-snug" style={{ fontFamily: 'var(--font-family-serif)' }}>
            Good to see you again.
          </h2>
          <p className="text-cream-400 text-sm leading-relaxed">
            Your next perfect cup is waiting. Settle in.
          </p>
        </div>
      </div>

      {/* Form */}
      <div className="w-full md:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-sm">
          <h1 className="text-3xl font-bold text-brown-900 mb-2" style={{ fontFamily: 'var(--font-family-serif)' }}>
            Sign in
          </h1>
          <p className="text-brown-400 text-sm mb-8">
            New here?{' '}
            <Link to="/register" className="text-forest-700 hover:text-forest-600 font-medium">
              Create an account
            </Link>
          </p>

          {error && (
            <div className="bg-terra-100 border border-terra-400 text-terra-700 text-sm px-4 py-3 rounded-xl mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Field label="Username">
              <input
                type="text"
                name="username"
                value={credentials.username}
                onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
                required
                className={inputCls}
              />
            </Field>
            <Field label="Password">
              <input
                type="password"
                name="password"
                value={credentials.password}
                onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
                required
                className={inputCls}
              />
            </Field>
            <button type="submit" disabled={loading} className={primaryBtn}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

const inputCls = 'w-full border border-cream-400 bg-cream-50 rounded-xl px-4 py-2.5 text-sm text-brown-900 placeholder-brown-300 focus:outline-none focus:ring-2 focus:ring-forest-600 focus:border-transparent transition';
const primaryBtn = 'w-full bg-forest-800 hover:bg-forest-700 disabled:bg-cream-300 disabled:text-brown-400 text-cream-50 py-2.5 rounded-xl text-sm font-medium transition-colors shadow-warm';

function Field({ label, children }) {
  return (
    <div>
      <label className="block text-brown-700 text-sm font-medium mb-1.5">{label}</label>
      {children}
    </div>
  );
}
