import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { logout, getAvatarUrl } from '../services/api';

function Navbar() {
  const { user, logout: authLogout } = useAuth();
  const navigate = useNavigate();
  const [avatarError, setAvatarError] = useState(false);

  const handleLogout = () => {
    logout();
    authLogout();
    navigate('/login');
  };

  return (
    <nav className="bg-forest-900 text-cream-100 px-8 py-4 flex justify-between items-center border-b border-forest-800">
      <Link
        to="/"
        className="text-xl text-cream-50 tracking-wide hover:text-forest-300 transition-colors"
        style={{ fontFamily: 'var(--font-family-serif)' }}
      >
        Wespresso World
      </Link>

      <div className="flex items-center gap-1">
        {user?.role === 'admin' && (
          <>
            <NavLink to="/admin">Admin</NavLink>
            <NavLink to="/swagger-ui">API Docs</NavLink>
          </>
        )}

        {user ? (
          <>
            <div className="flex items-center gap-2.5 px-3 py-1.5 mr-1">
              {!avatarError ? (
                <img
                  src={getAvatarUrl(user.id)}
                  onError={() => setAvatarError(true)}
                  alt={user.username}
                  className="w-7 h-7 rounded-full object-cover ring-1 ring-forest-700"
                />
              ) : (
                <div className="w-7 h-7 rounded-full bg-forest-700 flex items-center justify-center text-cream-100 text-xs font-semibold ring-1 ring-forest-600">
                  {user.username[0].toUpperCase()}
                </div>
              )}
              <span className="text-cream-300 text-sm">{user.username}</span>
            </div>

            <NavLink to="/profile">Profile</NavLink>
            <NavLink to="/orders">Orders</NavLink>
            <Link
              to={`/cart/${sessionStorage.getItem('cartId')}`}
              className="ml-2 text-sm bg-terra-600 hover:bg-terra-500 text-cream-50 px-4 py-1.5 rounded-lg transition-colors font-medium"
            >
              Cart
            </Link>
            <button
              onClick={handleLogout}
              className="ml-1 text-sm text-cream-400 hover:text-cream-100 px-3 py-1.5 rounded-lg hover:bg-forest-800 transition-colors"
            >
              Sign out
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login">Sign in</NavLink>
            <Link
              to="/register"
              className="ml-2 text-sm bg-terra-600 hover:bg-terra-500 text-cream-50 px-4 py-1.5 rounded-lg transition-colors font-medium"
            >
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}

function NavLink({ to, children }) {
  return (
    <Link
      to={to}
      className="text-sm text-cream-300 hover:text-cream-50 px-3 py-1.5 rounded-lg hover:bg-forest-800 transition-colors"
    >
      {children}
    </Link>
  );
}

export default Navbar;
