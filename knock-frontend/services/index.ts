import client from './client';

// ============== Auth API ==============
export const authApi = {
    login: (provider: string = 'KAKAO') => {
        window.location.href = `http://localhost:8080/oauth2/authorization/${provider.toLowerCase()}`;
    },
    emailLogin: (data: { email: string; password: string }) =>
        client.post('/auth/login', data),
    signup: (data: { email: string; name: string; password: string; nickname: string; profileImageUrl?: string }) =>
        client.post('/members', data),
    logout: () => client.post('/auth/logout'),
    getMe: () => client.get('/members/my'),
};

// ============== Group API ==============
export const groupsApi = {
    getMyGroups: () => client.get('/groups/my'),
    getGroup: (groupId: number | string) => client.get(`/groups/${groupId}`),
    createGroup: (data: { name: string; description?: string }) =>
        client.post('/groups', data),
    createInviteCode: (groupId: number, duration: number = 24) =>
        client.post(`/groups/${groupId}/invite-codes`, { duration }),
    joinGroup: (inviteCode: string) =>
        client.post('/groups/join', { inviteCode }),
    leaveGroup: (groupId: number) =>
        client.post(`/groups/${groupId}/leave`),
};

// ============== Item API ==============
export const itemsApi = {
    getItems: (groupId: number | string) => client.get(`/groups/${groupId}/items`),
    getItem: (itemId: number | string) => client.get(`/items/${itemId}`),
    createItem: (data: {
        groupId: number;
        title: string;
        description: string;
        price: number;
        itemType: 'SELL' | 'GIVE';
        category: string;
        imageUrls: string[];
    }) => client.post('/items', data),
    getMySelling: () => client.get('/items/my-selling'),
};

// ============== Bookmark API ==============
export const bookmarksApi = {
    toggle: (itemId: number | string) =>
        client.post(`/items/${itemId}/bookmarks`),
    getMyBookmarks: () => client.get('/items/my-bookmarks'),
};

// ============== Reservation API ==============
export const reservationsApi = {
    create: (itemId: number) => client.post('/reservations', { itemId }),
    getForItem: (itemId: number | string) =>
        client.get(`/items/${itemId}/reservations`),
    getMyReservations: () => client.get('/reservations/my'),
    approve: (id: number) => client.patch(`/reservations/${id}/approve`),
    complete: (id: number) => client.patch(`/reservations/${id}/complete`),
    cancel: (id: number) => client.patch(`/reservations/${id}/cancel`),
};

// ============== Notification API ==============
export const notificationsApi = {
    getAll: () => client.get('/notifications'),
    markAsRead: (id: number | string) =>
        client.patch(`/notifications/${id}/read`),
};

// ============== Image API ==============
export const imagesApi = {
    upload: (file: File, directory: string = 'items') => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('directory', directory);
        return client.post('/images/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    },
    delete: (imageUrl: string) =>
        client.delete('/images', { params: { imageUrl } }),
};
