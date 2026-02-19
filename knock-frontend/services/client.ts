import axios, { AxiosError } from 'axios';
import { ApiResponse } from '../types';

export interface ClientError extends Error {
    code?: string;
    status?: number;
    details?: unknown;
}

const createClientError = (
    message: string,
    status?: number,
    code?: string,
    details?: unknown
): ClientError => {
    const error = new Error(message) as ClientError;
    error.status = status;
    error.code = code;
    error.details = details;
    return error;
};

const client = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

client.interceptors.response.use(
    (response) => {
        const payload = response.data as ApiResponse<unknown>;
        if (!payload || typeof payload !== 'object' || !('result' in payload)) {
            return response.data;
        }

        if (payload.result === 'ERROR') {
            const errorMessage = payload.error?.message || 'Request failed.';
            return Promise.reject(
                createClientError(
                    errorMessage,
                    response.status,
                    payload.error?.code,
                    payload.error?.data
                )
            );
        }

        return payload.data;
    },
    (error) => {
        const axiosError = error as AxiosError<ApiResponse<unknown>>;
        const status = axiosError.response?.status;
        const payload = axiosError.response?.data;
        const payloadMessage =
            payload && typeof payload === 'object' && 'error' in payload
                ? payload.error?.message
                : undefined;
        const payloadCode =
            payload && typeof payload === 'object' && 'error' in payload
                ? payload.error?.code
                : undefined;
        const payloadDetails =
            payload && typeof payload === 'object' && 'error' in payload
                ? payload.error?.data
                : undefined;

        return Promise.reject(
            createClientError(
                payloadMessage || axiosError.message || 'Network request failed.',
                status,
                payloadCode,
                payloadDetails
            )
        );
    }
);

export default client;
