import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../services/api';

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

export default function Register() {
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await register(formData);
      navigate('/login');
    } catch {
      setError('Username or email may already be in use.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-cream-100 flex">
      {/* Left panel */}
      <div className="hidden md:flex md:w-1/2 bg-forest-900 flex-col items-center justify-center p-14 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10"
          style={{ backgroundImage: 'radial-gradient(circle at 70% 30%, #8cbfa4 0%, transparent 60%), radial-gradient(circle at 20% 80%, #d4703e 0%, transparent 50%)' }}
        />
        <div className="relative text-center max-w-xs">
          <p className="text-forest-300 text-xs tracking-widest uppercase mb-5 font-medium">Join us</p>
          <h2 className="text-4xl font-bold text-cream-50 mb-4 leading-snug" style={{ fontFamily: 'var(--font-family-serif)' }}>
            Your first cup starts here.
          </h2>
          <p className="text-cream-400 text-sm leading-relaxed">
            Single-origin roasts, ethically sourced, delivered from the heart of the PNW.
          </p>
        </div>
      </div>

      {/* Form */}
      <div className="w-full md:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-sm">
          <h1 className="text-3xl font-bold text-brown-900 mb-2" style={{ fontFamily: 'var(--font-family-serif)' }}>
            Create account
          </h1>
          <p className="text-brown-400 text-sm mb-8">
            Already have an account?{' '}
            <Link to="/login" className="text-forest-700 hover:text-forest-600 font-medium">
              Sign in
            </Link>
          </p>

          {error && (
            <div className="bg-terra-100 border border-terra-400 text-terra-700 text-sm px-4 py-3 rounded-xl mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Field label="Username">
              <input type="text" name="username" value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                required className={inputCls} />
            </Field>
            <Field label="Email">
              <input type="email" name="email" value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required className={inputCls} />
            </Field>
            <Field label="Password">
              <input type="password" name="password" value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required className={inputCls} />
            </Field>
            <button type="submit" disabled={loading} className={primaryBtn}>
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
