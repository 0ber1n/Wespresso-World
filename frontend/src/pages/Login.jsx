import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login, createCart } from "../services/api";
import { useAuth } from "../context/AuthContext";

function Login() {
    const [credentials, setCredentials] = useState({ username: "", password: "" });
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const { login: authLogin } = useAuth();

    const handleChange = (e) => {
        setCredentials({ ...credentials, [e.target.name]: e.target.value });
    }; 

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await login(credentials);
            sessionStorage.setItem("token", response.data.token);
            const profileResponse = await getProfile();
            const cartResponse = await createCart({customerName: credentials.username});
            sessionStorage.setItem("cartId", cartResponse.data.id);
            authLogin(response.data, response.data.token);
            navigate("/");
        } catch (err) {
            setError("Invalid username or password");
        }
    };

    return (
        <div>
            <h1>Login to Wespress World</h1>
            {error && <p style={{ color: "red" }}>{error}</p>}
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Username:</label>
                    <input 
                        type="text"
                        name="username"
                        value={credentials.username}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Password:</label>
                    <input 
                        type="password"
                        name="password"
                        value={credentials.password}
                        onChange={handleChange}
                        required
                    />
                </div>
                <button type="submit">Login</button>
            </form>
        </div>
    );
}

export default Login;