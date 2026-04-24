import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getOrder } from "../services/api";
import ErrorPage from "../components/ErrorPage";

function OrderConfirmation() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const response = await getOrder(orderId);
        setOrder(response.data);
      } catch {
        setError("Failed to load order details");
      } finally {
        setLoading(false);
      }
    };
    fetchOrder();
  }, [orderId]);

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-amber-50">
      <p className="text-amber-900 text-xl animate-pulse">Loading order...</p>
    </div>
  );

  if (error) return <ErrorPage message={error} />;

  return (
    <div className="bg-amber-50 min-h-screen p-8">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-xl shadow-md p-8 text-center mb-6">
          <div className="text-6xl mb-4">☕</div>
          <h1 className="text-3xl font-bold text-amber-900 mb-2">Order Confirmed!</h1>
          <p className="text-gray-600">
            Thank you, <span className="font-semibold text-amber-800">{order.customerName}</span>. Your order is on its way!
          </p>
          <p className="text-gray-400 text-sm mt-1">Order #{order.id}</p>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">
            Items Ordered
          </h2>
          {order.items.map((item) => (
            <div key={item.id} className="flex justify-between items-center border-b border-amber-100 pb-3 mb-3">
              <div>
                <p className="font-semibold text-amber-900">{item.itemName}</p>
                <p className="text-gray-500 text-sm capitalize">{item.category} &bull; Qty: {item.quantity}</p>
              </div>
              <p className="text-amber-700 font-semibold">
                ${(item.price * item.quantity).toFixed(2)}
              </p>
            </div>
          ))}
          <div className="mt-4 text-right">
            <p className="text-xl font-bold text-amber-900">Total: ${order.totalPrice.toFixed(2)}</p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-amber-800 mb-2">Shipping To</h2>
          <p className="text-gray-700">{order.shippingAddress}</p>
        </div>

        <div className="text-center">
          <button
            onClick={() => navigate("/")}
            className="bg-amber-800 hover:bg-amber-700 text-white px-8 py-3 rounded-lg font-semibold text-lg transition"
          >
            Continue Shopping
          </button>
        </div>
      </div>
    </div>
  );
}

export default OrderConfirmation;
