import { createContext, useContext, useState, useEffect } from "react";
import { getProfile } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [avatarVersion, setAvatarVersion] = useState(0)

    useEffect(() => {
        const loadUser = async () => {
            const token = sessionStorage.getItem("token");
            if (token) {
                try {
                    const response = await getProfile(token);
                    setUser(response.data);
                } catch (error) {
                    console.error("Failed to load user profile", error);
                    sessionStorage.removeItem("token");
                    setUser(null);
                }
            }
            setLoading(false);
        };
        loadUser();
    }, []);

    const login = (userData, token) => {
        sessionStorage.setItem("token", token);
        setUser(userData);
    };

    const logout = () => {
        sessionStorage.removeItem("token");
        setUser(null);
    }; 

    const refreshAvatar = () => setAvatarVersion(v => v +1);

    return (
        <AuthContext.Provider value={{ user, loading, login, logout, avatarVersion, refreshAvatar}}>
            {!loading && children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}