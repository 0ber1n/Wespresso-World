import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getCart, getMenu, getBeans, addBeanToCart, addDrinkToCart } from "../services/api";

function Cart() {
  const { cartId } = useParams();
  const [cart, setCart] = useState(null);
  const [menu, setMenu] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [beans, setBeans] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cartResponse, menuResponse, beansResponse] = await Promise.all([
          getCart(cartId),
          getMenu(),
          getBeans(),
        ]);
        setCart(cartResponse.data);
        setMenu(menuResponse.data);
        setBeans(beansResponse.data);
      } catch (err) {
        setError("Failed to load cart or menu");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [cartId]);

  const handleAddToCart = async (drinkId) => {
    try {
      const response = await addDrinkToCart(cartId, { drinkId, quantity: 1 });
      setCart(response.data);
    } catch (err) {
      setError("Failed to add drink to cart");
    }
  };

  const handleAddBeansToCart = async (beansId) => {
    try {
      const response = await addBeanToCart(cartId, { beanId: beansId, quantity: 1 });
      setCart(response.data);
    } catch (err) {
      setError("Failed to add beans to cart");
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-amber-50">
      <p className="text-amber-900 text-xl animate-pulse">Loading cart...</p>
    </div>
  );

  if (error) return (
    <div className="flex justify-center items-center h-screen bg-amber-50">
      <p className="text-red-500 text-xl">{error}</p>
    </div>
  );

  return (
    <div className="bg-amber-50 min-h-screen p-8">
      <h1 className="text-4xl font-bold text-amber-900 mb-8 text-center">
        🛒 {cart.customerName}'s Cart
      </h1>

      <div className="bg-white rounded-xl shadow-md p-6 mb-8">
        <h2 className="text-2xl font-semibold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">
          Items in Cart:
        </h2>
        {cart.items.length === 0 ? (
          <p className="text-gray-500 text-center py-4">Your cart is empty</p>
        ) : (
          cart.items.map((item) => (
            <div key={item.id} className="flex justify-between items-center border-b border-amber-100 pb-3 mb-3">
              <div>
                <p className="font-semibold text-amber-900">{item.itemName}</p>
                <p className="text-gray-500 text-sm">Quantity: {item.quantity}</p>
              </div>
              <p className="text-amber-700 font-semibold">${item.price}</p>
            </div>
          ))
        )}
        <div className="mt-4 text-right">
          <h3 className="text-xl font-bold text-amber-900">Total: ${cart.totalPrice}</h3>
        </div>
      </div>

      <h2 className="text-2xl font-semibold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">
        Add More Drinks:
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {menu.map((drink) => (
          <div key={drink.id} className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition">
            <h3 className="text-xl font-bold text-amber-900">{drink.name}</h3>
            <p className="text-gray-600 mt-2">{drink.description}</p>
            <p className="text-amber-700 font-semibold mt-2">Price: ${drink.price}</p>
            <button
              onClick={() => handleAddToCart(drink.id)}
              className="mt-4 w-full bg-amber-800 hover:bg-amber-700 text-white py-2 rounded-lg transition"
            >
              Add to Cart
            </button>
          </div>
        ))}
      </div>

      <h2 className="text-2xl font-semibold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">
        Add More Beans:
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {beans.map((bean) => (
          <div key={bean.id} className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition">
            <h3 className="text-xl font-bold text-amber-900">{bean.name}</h3>
            <p className="text-gray-600 mt-2">{bean.description}</p>
            <p className="text-amber-700 font-semibold mt-2">Price: ${bean.price}</p>
            <button
              onClick={() => handleAddBeansToCart(bean.id)}
              className="mt-4 w-full bg-amber-800 hover:bg-amber-700 text-white py-2 rounded-lg transition"
            >
              Add to Cart
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Cart;