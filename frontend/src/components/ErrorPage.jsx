import { useNavigate } from 'react-router-dom';

function ErrorPage({ message }) {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-cream-100 px-8">
      <p className="text-terra-600 text-xs tracking-widest uppercase font-medium mb-4">Something went wrong</p>
      <h1 className="text-4xl font-bold text-brown-900 mb-3" style={{ fontFamily: 'var(--font-family-serif)' }}>
        Oops.
      </h1>
      <p className="text-brown-500 mb-8 text-center max-w-sm">
        {message || 'An error occurred while processing your request.'}
      </p>
      <button
        onClick={() => navigate(-1)}
        className="bg-forest-800 hover:bg-forest-700 text-cream-50 px-6 py-2.5 rounded-xl text-sm font-medium transition-colors shadow-warm"
      >
        Go back
      </button>
    </div>
  );
}

export default ErrorPage;
