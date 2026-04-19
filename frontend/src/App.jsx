import { BrowserRouter, Routes, Route } from "react-router-dom";
import Menu from './pages/Menu';
import Cart from './pages/Cart';
import Login from './pages/Login';
import Register from './pages/Register';
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";
import Admin from "./pages/Admin";

function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<Menu />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/admin" element={
          <ProtectedRoute adminOnly>
            <Admin />
          </ProtectedRoute>
        } />
        <Route path="/cart/:cartId" element={
          <ProtectedRoute>
            <Cart />
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}

export default App;