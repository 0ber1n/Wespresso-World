import { useState, useEffect } from "react";
import api, { getMenu, getBeans, createMenu, createBean, deleteMenu, deleteBean, updateMenu, updateBean } from "../services/api";

function Admin() {
    const [drinks, setDrinks] = useState([]);
    const [beans, setBeans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState("drinks");

    const [newDrink, setNewDrink] = useState({ name: "", description: "", price: "" });
    const [newBean, setNewBean] = useState({ name: "", description: "", origin: "", roastLevel: "", isRaw: false, price: "" });
    const [flag, setFlag] = useState(null);

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

    useEffect(() => {
        api.get('/admin/flag')
            .then(res => {
                if (res.data?.flag) {
                    setFlag(res.data.flag);
                }
            })
            .catch(() => {
                // 403 for normal users — expected, do nothing
            });
    }, []);

    const handleAddDrink = async (e) => {
        e.preventDefault();
        try {
            const response = await createMenu(newDrink);
            setDrinks([...drinks, response.data]);
            setNewDrink({ name: "", description: "", price: "" });
        } catch (err) {
            setError("Failed to add drink");
        }
    };

    const handleDeleteDrink = async (id) => {
        try {
            await deleteMenu(id);
            setDrinks(drinks.filter((drink) => drink.id !== id));
        } catch (err) {
            setError("Failed to delete drink");
        }
    };

    const handleAddBean = async (e) => {
        e.preventDefault();
        try {
            const response = await createBean(newBean);
            setBeans([...beans, response.data]);
            setNewBean({ name: "", description: "", origin: "", roastLevel: "", isRaw: false, price: "" });
        } catch (err) {
            setError("Failed to add bean");
        }
    };
    
   const handleDeleteBean = async (id) => {
    try {
      await deleteBean(id);
      setBeans(beans.filter((b) => b.id !== id));
    } catch (err) {
      setError("Failed to delete bean");
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-screen bg-amber-50">
      <p className="text-amber-900 text-xl animate-pulse">Loading...</p>
    </div>
  );

  return (
    <div className="bg-amber-50 min-h-screen p-8">
      <h1 className="text-4xl font-bold text-amber-900 mb-8 text-center">
        ⚙️ Admin Panel
      </h1>

      {error && (
        <p className="bg-red-100 text-red-600 p-3 rounded-lg mb-4 text-center">{error}</p>
      )}

      {flag && (
                    <div className="bg-green-100 border border-green-400 text-green-800 rounded-xl p-6 mb-8 text-center shadow-md">
                        <p className="text-lg font-bold mb-1">You've captured the flag!</p>
                        <p className="font-mono text-xl tracking-wider">{flag}</p>
                    </div>
                )}

      {/* Tabs */}
      <div className="flex gap-4 mb-8">
        <button
          onClick={() => setActiveTab("drinks")}
          className={`px-6 py-2 rounded-lg font-semibold transition ${activeTab === "drinks" ? "bg-amber-800 text-white" : "bg-white text-amber-800 hover:bg-amber-100"}`}
        >
          Drinks
        </button>
        <button
          onClick={() => setActiveTab("beans")}
          className={`px-6 py-2 rounded-lg font-semibold transition ${activeTab === "beans" ? "bg-amber-800 text-white" : "bg-white text-amber-800 hover:bg-amber-100"}`}
        >
          Beans
        </button>
      </div>

      {/* Drinks Tab */}
      {activeTab === "drinks" && (
        <div>
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h2 className="text-2xl font-semibold text-amber-800 mb-4">Add New Drink</h2>
            <form onSubmit={handleAddDrink} className="space-y-4">
              <input
                type="text"
                placeholder="Name"
                value={newDrink.name}
                onChange={(e) => setNewDrink({ ...newDrink, name: e.target.value })}
                required
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <input
                type="text"
                placeholder="Description"
                value={newDrink.description}
                onChange={(e) => setNewDrink({ ...newDrink, description: e.target.value })}
                required
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <input
                type="number"
                placeholder="Price"
                value={newDrink.price}
                onChange={(e) => setNewDrink({ ...newDrink, price: e.target.value })}
                required
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <button
                type="submit"
                className="w-full bg-amber-800 hover:bg-amber-700 text-white py-2 rounded-lg font-semibold transition"
              >
                Add Drink
              </button>
            </form>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <h2 className="text-2xl font-semibold text-amber-800 mb-4">Manage Drinks</h2>
            <div className="space-y-3">
              {drinks.map((drink) => (
                <div key={drink.id} className="flex justify-between items-center border-b border-amber-100 pb-3">
                  <div>
                    <p className="font-semibold text-amber-900">{drink.name}</p>
                    <p className="text-gray-500 text-sm">{drink.description}</p>
                    <p className="text-amber-700 text-sm">${drink.price}</p>
                  </div>
                  <button
                    onClick={() => handleDeleteDrink(drink.id)}
                    className="bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded-lg transition"
                  >
                    Delete
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Beans Tab */}
      {activeTab === "beans" && (
        <div>
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h2 className="text-2xl font-semibold text-amber-800 mb-4">Add New Bean</h2>
            <form onSubmit={handleAddBean} className="space-y-4">
              <input
                type="text"
                placeholder="Name"
                value={newBean.name}
                onChange={(e) => setNewBean({ ...newBean, name: e.target.value })}
                required
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <input
                type="text"
                placeholder="Description"
                value={newBean.description}
                onChange={(e) => setNewBean({ ...newBean, description: e.target.value })}
                required
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <input
                type="text"
                placeholder="Origin"
                value={newBean.origin}
                onChange={(e) => setNewBean({ ...newBean, origin: e.target.value })}
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <input
                type="text"
                placeholder="Roast Level"
                value={newBean.roastLevel}
                onChange={(e) => setNewBean({ ...newBean, roastLevel: e.target.value })}
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <label className="flex items-center gap-2 text-amber-900">
                <input
                  type="checkbox"
                  checked={newBean.isRaw}
                  onChange={(e) => setNewBean({ ...newBean, isRaw: e.target.checked })}
                />
                Raw Bean
              </label>
              <input
                type="number"
                placeholder="Price"
                value={newBean.price}
                onChange={(e) => setNewBean({ ...newBean, price: e.target.value })}
                required
                className="w-full border border-amber-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              <button
                type="submit"
                className="w-full bg-amber-800 hover:bg-amber-700 text-white py-2 rounded-lg font-semibold transition"
              >
                Add Bean
              </button>
            </form>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <h2 className="text-2xl font-semibold text-amber-800 mb-4">Manage Beans</h2>
            <div className="space-y-3">
              {beans.map((bean) => (
                <div key={bean.id} className="flex justify-between items-center border-b border-amber-100 pb-3">
                  <div>
                    <p className="font-semibold text-amber-900">{bean.name}</p>
                    <p className="text-gray-500 text-sm">{bean.description}</p>
                    <p className="text-amber-700 text-sm">${bean.price}</p>
                  </div>
                  <button
                    onClick={() => handleDeleteBean(bean.id)}
                    className="bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded-lg transition"
                  >
                    Delete
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Admin;