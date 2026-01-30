import axios from 'axios';
import { env } from '../config/env';

export const backendClient = axios.create({
    baseURL: env.BACKEND_URL,
    timeout: 5000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptor para logs ou auth futura
backendClient.interceptors.response.use(
    (response) => response,
    (error) => {
        // Aqui normalizaremos erros do Backend Core para erros do BFF no futuro
        console.error(`Backend call failed: ${error.message}`);
        return Promise.reject(error);
    }
);
