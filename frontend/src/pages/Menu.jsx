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


    if (loading) return (<div className="flex justify-center items-center">Loading menu...</div>);
    if (error) return (<div className="flex justify-center items-center"><p>{error}</p></div>);

    return (
        <div className="bg-amber-50 min-h-screen p-8">
            <h1 className="text-4xl font-bold text-amber-900 mb-8 text-center">Wespresso World Menu</h1>
            <h2 className="text-2xl font-bold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">Drinks</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            {drinks.map((drink) => (
                <div key={drink.id} className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition">
                    <h2 className="text-xl font-bold text-amber-900">{drink.name}</h2>
                    <p className="text-gray-600 mt-2">{drink.description}</p>
                    <p className="text-amber-700 font-semibold mt-2">Price: ${drink.price}</p>
                    {user && <button onClick={() => handleaddDrink(drink.id)} className="bg-amber-700 hover:bg-amber-600 text-white py-2 px-4 rounded-lg transition">Add to Cart</button>}
                </div>
            ))}
            </div>
            <h2 className="text-2xl font-bold text-amber-800 mb-4 border-b-2 border-amber-300 pb-2">Coffee Beans</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            {beans.map((bean) => (
                <div key={bean.id} className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition">
                    <h2 className="text-xl font-bold text-amber-900">{bean.name}</h2>
                    <p className="text-gray-600 mt-2">{bean.description}</p>
                    <p className="text-amber-700 font-semibold mt-2">Price: ${bean.price}</p>
                    {user && <button onClick={() => handleaddBean(bean.id)} className="bg-amber-700 hover:bg-amber-600 text-white py-2 px-4 rounded-lg transition">Add to Cart</button>}
                </div>
            ))}
            </div>
        </div>
    );  

}   

export default Menu;