import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptor: inject active Okta subject for tenant isolation
apiClient.interceptors.request.use((config) => {
    const activeSubject = localStorage.getItem('okta_subject') || 'okta_happy_paws_admin';
    config.headers['X-Dev-Okta-Subject'] = activeSubject;
    return config;
});

export default apiClient;