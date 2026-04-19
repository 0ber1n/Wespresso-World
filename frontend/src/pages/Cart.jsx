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
            const response = await addDrinkToCart(cartId, {drinkId, quantity: 1});
            setCart(response.data);
        } catch (err) {
            setError("Failed to add drink to cart");
        }
    };

    const handleAddBeansToCart = async (beansId) => {
        try {
            const response = await addBeanToCart(cartId, {beanId, quantity: 1} );
            setCart(response.data);
        } catch (err) {
            setError("Failed to add beans to cart");
        }
    };

    if (loading) return <p>Loading cart...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h1>{cart.customerName}'s Cart</h1>
            <h2>Items in Cart:</h2>
            {cart.items.length === 0 ? (
                <p>Your cart is empty</p>
            ) : (
                cart.items.map((item) => (
                    <div key={item.id}>
                        <p>{item.itemName}</p>
                        <p>Quantity: {item.quantity}</p>
                        <p>Price: ${item.price}</p>
                    </div>
                ))
            )}
            <h3>Total: ${cart.totalPrice}</h3>
            <h2>Add More Drinks:</h2>
            {menu.map((drink) => (
                <div key={drink.id}>
                    <h3>{drink.name}</h3>
                    <p>{drink.description}</p>
                    <p>Price: ${drink.price}</p>
                    <button onClick={() => handleAddToCart(drink.id)}>Add to Cart</button>
                </div>
            ))}
            <h2>Add More Beans:</h2>
            {beans.map((bean) => (
                <div key={bean.id}>
                    <h3>{bean.name}</h3>
                    <p>{bean.description}</p>
                    <p>Price: ${bean.price}</p>
                    <button onClick={() => handleAddBeansToCart(bean.id)}>Add to Cart</button>
                </div>
            ))}
        </div>      
    );
}

export default Cart;