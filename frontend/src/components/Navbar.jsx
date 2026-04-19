import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { logout } from '../services/api';

function Navbar() {
  const { user, logout: authLogout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    authLogout();
    navigate('/login');
  };

  return (
    <nav className='bg-amber-900 text-white px-6 py-4 flex justify-between items-center shadow-lg'>
      <Link to="/" className='text-2xl font-bold hover:text-amber-200'>Wespresso World</Link>
      <div className="flex items-center gap-4">
        {user && user.role === 'admin' && (
          <Link to="/admin" className="bg-amber-700 hover:bg-amber-600 px-4 py-2 rounded-lg transition">
            Admin Panel
          </Link>
      )}
        {user ? (
          <>
            <span className='text-amber-200'>Welcome, {user.username}</span>
            <Link to={`/cart/${sessionStorage.getItem("cartId")}`} className='bg-amber-700 hover:bg-amber-600 px-4 py-2 rounded-lg transition'>
              View Cart
            </Link>
            <button onClick={handleLogout} className='bg-amber-700 hover:bg-amber-600 px-4 py-2 rounded-lg transition'>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className='hover:text-amber-200 transition'>Login</Link>
            <Link to="/register" className='hover:text-amber-200 transition'>Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;