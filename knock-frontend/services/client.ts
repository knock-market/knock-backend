import axios from 'axios';

const client = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

// Response interceptor for handling common errors or data unwrapping if needed
client.interceptors.response.use(
    (response) => {
        // If backend returns { success: true, data: ... }, we might want to return response.data
        return response.data;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default client;
