import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getCart, checkout } from "../services/api";
import ErrorPage from "../components/ErrorPage";

function Checkout() {
  const { cartId } = useParams();
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({
    fullName: "",
    street: "",
    city: "",
    zip: "",
    country: "",
    cardNumber: "",
    expiry: "",
    cvv: "",
  });

  useEffect(() => {
    const fetchCart = async () => {
      try {
        const response = await getCart(cartId);
        setCart(response.data);
        setForm((f) => ({ ...f, fullName: response.data.customerName }));
      } catch {
        setError("Failed to load cart");
      } finally {
        setLoading(false);
      }
    };
    fetchCart();
  }, [cartId]);

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const shippingAddress = `${form.street}, ${form.city}, ${form.zip}, ${form.country}`;
      const response = await checkout(cartId, { shippingAddress });
      navigate(`/order/${response.data.id}`);
    } catch {
      setError("Checkout failed. Please try again.");
      setSubmitting(false);
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-amber-50">
      <p className="text-amber-900 text-xl animate-pulse">Loading...</p>
    </div>
  );

  if (error && !cart) return <ErrorPage message={error} />;

  return (
    <div className="bg-amber-50 min-h-screen p-8">
      <h1 className="text-4xl font-bold text-amber-900 mb-8 text-center">Checkout</h1>

      <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Order Summary */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <h2 className="text-2xl font-semibold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">
            Order Summary
          </h2>
          {cart.items.length === 0 ? (
            <p className="text-gray-500 text-center py-4">Your cart is empty</p>
          ) : (
            cart.items.map((item) => (
              <div key={item.id} className="flex justify-between items-center border-b border-amber-100 pb-3 mb-3">
                <div>
                  <p className="font-semibold text-amber-900">{item.itemName}</p>
                  <p className="text-gray-500 text-sm">Qty: {item.quantity}</p>
                </div>
                <p className="text-amber-700 font-semibold">
                  ${(item.price * item.quantity).toFixed(2)}
                </p>
              </div>
            ))
          )}
          <div className="mt-4 text-right">
            <h3 className="text-xl font-bold text-amber-900">
              Total: ${cart.totalPrice.toFixed(2)}
            </h3>
          </div>
        </div>

        {/* Checkout Form */}
        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-md p-6 flex flex-col gap-4">
          <h2 className="text-2xl font-semibold text-amber-800 border-b-2 border-amber-300 pb-2">
            Shipping & Payment
          </h2>

          {error && <p className="text-red-600 text-sm">{error}</p>}

          <div>
            <label className="block text-amber-900 font-semibold mb-1">Full Name</label>
            <input
              name="fullName"
              value={form.fullName}
              onChange={handleChange}
              required
              className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>

          <div>
            <label className="block text-amber-900 font-semibold mb-1">Street Address</label>
            <input
              name="street"
              value={form.street}
              onChange={handleChange}
              required
              placeholder="123 Coffee Lane"
              className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-amber-900 font-semibold mb-1">City</label>
              <input
                name="city"
                value={form.city}
                onChange={handleChange}
                required
                className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
            <div>
              <label className="block text-amber-900 font-semibold mb-1">ZIP Code</label>
              <input
                name="zip"
                value={form.zip}
                onChange={handleChange}
                required
                className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-amber-900 font-semibold mb-1">Country</label>
            <input
              name="country"
              value={form.country}
              onChange={handleChange}
              required
              placeholder="USA"
              className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>

          <h3 className="text-lg font-semibold text-amber-800 border-t border-amber-200 pt-3 mt-1">
            Payment Details
          </h3>

          <div>
            <label className="block text-amber-900 font-semibold mb-1">Card Number</label>
            <input
              name="cardNumber"
              value={form.cardNumber}
              onChange={handleChange}
              required
              placeholder="1234 5678 9012 3456"
              maxLength={19}
              className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-amber-900 font-semibold mb-1">Expiry</label>
              <input
                name="expiry"
                value={form.expiry}
                onChange={handleChange}
                required
                placeholder="MM/YY"
                maxLength={5}
                className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
            <div>
              <label className="block text-amber-900 font-semibold mb-1">CVV</label>
              <input
                name="cvv"
                value={form.cvv}
                onChange={handleChange}
                required
                placeholder="123"
                maxLength={4}
                className="w-full border border-amber-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting || cart.items.length === 0}
            className="mt-4 w-full bg-amber-800 hover:bg-amber-700 disabled:bg-amber-300 text-white py-3 rounded-lg font-semibold text-lg transition"
          >
            {submitting ? "Placing Order..." : `Place Order — $${cart.totalPrice.toFixed(2)}`}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Checkout;
