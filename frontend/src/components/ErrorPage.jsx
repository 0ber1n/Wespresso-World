import { useNavigate } from "react-router-dom";
import Navbar from "./Navbar";

function ErrorPage() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-amber-50">
      <h1 className="text-4xl font-bold text-amber-900 mb-4">Oops! Something went wrong.</h1>
      <p className="text-gray-600 mb-8">We're sorry, but an error occurred while processing your request.</p>
      <button
        onClick={() => navigate(-1)}
        className="bg-amber-700 hover:bg-amber-600 text-white py-2 px-4 rounded-lg transition"
      >
        Go Back
      </button>
    </div>
  );
}

export default ErrorPage;