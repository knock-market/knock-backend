import client from './client';
import {
    BlockedUserResponseDto,
    BookmarkToggleResponseDto,
    GroupCreateResponseDto,
    GroupResponseDto,
    ImageUploadResultDto,
    InviteDuration,
    ItemResponseDto,
    ItemSummaryResponseDto,
    MemberResponseDto,
    MyBookmarkResponseDto,
    NotificationResponseDto,
    NotificationSettingsResponseDto,
    ReservationCreateResponseDto,
    ReservationResponseDto,
} from '../types';

const get = <T>(url: string, config?: object) => client.get<T, T>(url, config);
const post = <T, D = unknown>(url: string, data?: D, config?: object) =>
    client.post<T, T, D>(url, data, config);
const put = <T, D = unknown>(url: string, data?: D, config?: object) =>
    client.put<T, T, D>(url, data, config);
const patch = <T, D = unknown>(url: string, data?: D, config?: object) =>
    client.patch<T, T, D>(url, data, config);
const del = <T>(url: string, config?: object) => client.delete<T, T>(url, config);

// ============== Auth API ==============
export const authApi = {
    emailLogin: (data: { email: string; password: string }) =>
        post<void, { email: string; password: string }>('/auth/login', data),
    signup: (data: { email: string; name: string; password: string; nickname: string; profileImageUrl?: string }) =>
        post<void, { email: string; name: string; password: string; nickname: string; profileImageUrl?: string }>(
            '/members',
            data
        ),
    logout: () => post<void>('/auth/logout'),
    getMe: () => get<MemberResponseDto>('/members/my'),
    updateProfile: (data: { nickname?: string; profileImageUrl?: string }) =>
        put<void, { nickname?: string; profileImageUrl?: string }>('/members/my', data),
};

// ============== Group API ==============
export const groupsApi = {
    getMyGroups: () => get<GroupResponseDto[]>('/groups/my'),
    getGroup: (groupId: number | string) => get<GroupResponseDto>(`/groups/${groupId}`),
    createGroup: (data: { name: string; description?: string; imageUrl?: string }) =>
        post<GroupCreateResponseDto, { name: string; description?: string; imageUrl?: string }>(
            '/groups',
            data
        ),
    createInviteCode: (groupId: number, duration: InviteDuration = 'ONE_DAY') =>
        post<{ inviteCode: string; expiresAt?: string }, { duration: InviteDuration }>(
            `/groups/${groupId}/invite-codes`,
            { duration }
        ),
    joinGroup: (inviteCode: string) =>
        post<{ id: number }, { inviteCode: string }>('/groups/join', { inviteCode }),
    leaveGroup: (groupId: number) =>
        post<void>(`/groups/${groupId}/leave`),
};

// ============== Item API ==============
export const itemsApi = {
    getItems: (groupId: number | string) => get<ItemSummaryResponseDto[]>(`/groups/${groupId}/items`),
    getItem: (itemId: number | string) => get<ItemResponseDto>(`/items/${itemId}`),
    createItem: (data: {
        groupId: number;
        title: string;
        description: string;
        price: number;
        itemType: 'SELL' | 'GIVE';
        category: string;
        imageUrls: string[];
    }) => post<{ id: number }, {
        groupId: number;
        title: string;
        description: string;
        price: number;
        itemType: 'SELL' | 'GIVE';
        category: string;
        imageUrls: string[];
    }>('/items', data),
    getMySelling: () => get<ItemSummaryResponseDto[]>('/items/my-selling'),
    deleteItem: (itemId: number | string) => del<void>(`/items/${itemId}`),
};

// ============== Bookmark API ==============
export const bookmarksApi = {
    toggle: (itemId: number | string) =>
        post<BookmarkToggleResponseDto>(`/items/${itemId}/bookmarks`),
    getMyBookmarks: () => get<MyBookmarkResponseDto[]>('/items/my-bookmarks'),
};

// ============== Reservation API ==============
export const reservationsApi = {
    create: (itemId: number) => post<ReservationCreateResponseDto, { itemId: number }>('/reservations', { itemId }),
    getForItem: (itemId: number | string) =>
        get<ReservationResponseDto[]>(`/items/${itemId}/reservations`),
    getMyReservations: () => get<ReservationResponseDto[]>('/reservations/my'),
    approve: (id: number) => patch<void>(`/reservations/${id}/approve`),
    complete: (id: number) => patch<void>(`/reservations/${id}/complete`),
    cancel: (id: number) => patch<void>(`/reservations/${id}/cancel`),
};

// ============== Notification API ==============
export const notificationsApi = {
    getAll: () => get<NotificationResponseDto[]>('/notifications'),
    markAsRead: (id: number | string) =>
        patch<void>(`/notifications/${id}/read`),
    markAllAsRead: () =>
        patch<void>('/notifications/read-all'),
};

// ============== Member Settings API ==============
export const memberSettingsApi = {
    getNotificationSettings: () =>
        get<NotificationSettingsResponseDto>('/members/my/settings/notifications'),
    updateNotificationSettings: (data: NotificationSettingsResponseDto) =>
        put<void, NotificationSettingsResponseDto>('/members/my/settings/notifications', data),
};

// ============== Member Block API ==============
export const memberBlockApi = {
    getBlockedUsers: () => get<BlockedUserResponseDto[]>('/members/my/blocked'),
    blockUser: (memberId: number | string) => post<void>(`/members/${memberId}/block`),
    unblockUser: (memberId: number | string) => del<void>(`/members/${memberId}/block`),
};

// ============== Image API ==============
export const imagesApi = {
    upload: (file: File, directory: string = 'items') => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('directory', directory);
        return post<ImageUploadResultDto, FormData>('/images/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    },
    delete: (imageUrl: string) =>
        del<void>('/images', { params: { imageUrl } }),
};
