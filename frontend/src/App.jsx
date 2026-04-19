import { BrowswerRouter, Routes, Route } from "react-router-dom";
import Menu from './pages/Menu';
import Cart from './pages/Cart';
import Login from './pages/Login';
import Register from './pages/Register';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Menu />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/cart/:cartId" element={<Cart />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;