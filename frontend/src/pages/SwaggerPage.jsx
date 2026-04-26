import SwaggerUI from "swagger-ui-react";
import "swagger-ui-react/swagger-ui.css";

function SwaggerPage() {
    const token = sessionStorage.getItem("token");

    if (!token) {
        return (
            <div className="flex flex-col items-center justify-center min-h-screen bg-amber-50">
                <h1 className="text-4xl font-bold text-amber-900 mb-4">Unauthorized</h1>
                <p className="text-gray-600 mb-8">You must be logged in to view the API documentation.</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-amber-50 p-8">
            <h1 className="text-4xl font-bold text-amber-900 mb-8 text-center">API Documentation</h1>
            <SwaggerUI 
                url="/api/v1/api-docs" 
                requestInterceptor={(request) => {
                    if (token) {
                        request.headers["Authorization"] = `Bearer ${token}`;
                    }   
                    return request;
                }}/>
         </div>
    );
}

export default SwaggerPage;