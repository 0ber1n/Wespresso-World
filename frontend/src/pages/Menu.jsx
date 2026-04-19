import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {getMenu, getBeans, addBeanToCart, addDrinkToCart } from "../services/api";
import { useAuth } from "../context/AuthContext";

function Menu() {
    const [drinks, setDrinks] = useState([]);
    const [beans, setBeans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { user } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [drinksResponse, beansResponse] = await Promise.all([getMenu(), getBeans()]);
                setDrinks(drinksResponse.data);
                setBeans(beansResponse.data);
            } catch (err) {
                setError("Failed to load menu");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const handleaddDrink = async (drinkId) => {
        try {
            const cartId = sessionStorage.getItem("cartId");
            await addDrinkToCart(cartId, { drinkId, quantity: 1 });
            alert("Drink added to cart!");
        } catch (err) {
            setError("Failed to add drink to cart");
        }
    };

    const handleaddBean = async (beanId) => {
        try {
            const cartId = sessionStorage.getItem("cartId");
            await addBeanToCart(cartId, { beanId, quantity: 1 });
            alert("Bean added to cart!");
        } catch (err) {
            setError("Failed to add bean to cart");
        }
    };


    if (loading) return <p>Loading menu...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h1>Wespresso World Menu</h1>
            <h2>Drinks</h2>
            {drinks.map((drink) => (
                <div key={drink.id}>
                    <h2>{drink.name}</h2>
                    <p>{drink.description}</p>
                    <p>Price: ${drink.price}</p>
                    {user && <button onClick={() => handleaddDrink(drink.id)}>Add to Cart</button>}
                </div>
            ))}
            <h2>Coffee Beans</h2>
            {beans.map((bean) => (
                <div key={bean.id}>
                    <h2>{bean.name}</h2>
                    <p>{bean.description}</p>
                    <p>Price: ${bean.price}</p>
                    {user && <button onClick={() => handleaddBean(bean.id)}>Add to Cart</button>}
                </div>
            ))}
        </div>
    );  
}   

export default Menu;