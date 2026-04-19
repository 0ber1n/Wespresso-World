import { useState, useEffect } from "react";
import {getMenu } from "../services/api";

function Menu() {
    const [drinks, setDrinks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchMenu = async () => {
            try {
                const response = await getMenu();
                setDrinks(response.data);
            } catch (err) {
                setError("Failed to load menu");
            } finally {
                setLoading(false);
            }
        };

        fetchMenu();
    }, []);

    if (loading) return <p>Loading menu...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h1>Wespresso World Menu</h1>
            {drinks.map((drink) => (
                <div key={drink.id}>
                    <h2>{drink.name}</h2>
                    <p>{drink.description}</p>
                    <p>Price: ${drink.price}</p>
                </div>
            ))}
        </div>
    );  
}   

export default Menu;