import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login, createCart, getProfile } from "../services/api";
import { useAuth } from "../context/AuthContext";

function Login() {
  const [credentials, setCredentials] = useState({ username: "", password: "" });
  const [error, setError] = useState(null);
  const [loggedIn, setLoggedIn] = useState(false);
  const navigate = useNavigate();
  const { login: authLogin } = useAuth();

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  useEffect(() => {
    if (loggedIn) navigate("/");
  }, [loggedIn]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await login(credentials);
      sessionStorage.setItem("token", response.data.token);
      const profileResponse = await getProfile();
      const cartResponse = await createCart({ customerName: credentials.username });
      sessionStorage.setItem("cartId", cartResponse.data.id);
      authLogin(profileResponse.data, response.data.token);
      setLoggedIn(true);
    } catch (err) {
      setError("Invalid username or password");
    }
  };

  return (
    <div className="min-h-screen bg-amber-50 flex items-center justify-center">
      <div className="bg-white rounded-xl shadow-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-amber-900 mb-6 text-center">
          Welcome Back
        </h1>
        {error && (
          <p className="bg-red-100 text-red-600 p-3 rounded-lg mb-4 text-center">
            {error}
          </p>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-amber-900 font-semibold mb-1">Username</label>
            <input
              type="text"
              name="username"
              value={credentials.username}
              onChange={handleChange}
              required
              className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>
          <div>
            <label className="block text-amber-900 font-semibold mb-1">Password</label>
            <input
              type="password"
              name="password"
              value={credentials.password}
              onChange={handleChange}
              required
              className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>
          <button
            type="submit"
            className="w-full bg-amber-800 hover:bg-amber-700 text-white py-2 rounded-lg font-semibold transition"
          >
            Login
          </button>
        </form>
        <p className="text-center text-gray-600 mt-4">
          Don't have an account?{" "}
          <Link to="/register" className="text-amber-700 hover:underline font-semibold">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;