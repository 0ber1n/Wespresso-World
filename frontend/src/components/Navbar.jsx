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
    <nav>
      <Link to="/">Wespresso World</Link>
      <div>
        {user ? (
          <>
            <span>Welcome, {user.username}</span>
            <Link to={`/cart/${sessionStorage.getItem("cartId")}`}>View Cart</Link>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;