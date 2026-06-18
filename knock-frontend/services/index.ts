import client from './client';
import {
    BlockResponseDto,
    BookmarkToggleResponseDto,
    ImageUploadResultDto,
    InviteDuration,
    ItemPolicyWarningRequestDto,
    ItemPolicyWarningResponseDto,
    ItemResponseDto,
    ItemSummaryResponseDto,
    LocationSearchResponseDto,
    MemberResponseDto,
    MyBookmarkResponseDto,
    NotificationResponseDto,
    NotificationSettingsResponseDto,
    ReservationCreateResponseDto,
    ReservationResponseDto,
    ReportCreateRequestDto,
    ReportResponseDto,
    SellerShareLinkResponseDto,
    SellerShareLinkSummaryResponseDto,
    SellerShopResponseDto,
} from '../types';

const get = <T>(url: string, config?: object) => client.get<T, T>(url, config);
const post = <T, D = unknown>(url: string, data?: D, config?: object) =>
    client.post<T, T, D>(url, data, config);
const put = <T, D = unknown>(url: string, data?: D, config?: object) =>
    client.put<T, T, D>(url, data, config);
const patch = <T, D = unknown>(url: string, data?: D, config?: object) =>
    client.patch<T, T, D>(url, data, config);
const del = <T>(url: string, config?: object) => client.delete<T, T>(url, config);

type ItemCreatePayload = {
    title: string;
    description: string;
    price: number;
    itemType: 'SELL' | 'GIVE';
    imageUrls: string[];
    tradeLocationName?: string;
    tradeLocationAddress?: string;
    tradeLatitude?: number;
    tradeLongitude?: number;
};

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

// ============== Item API ==============
export const itemsApi = {
    getMarketplaceItems: () => get<ItemSummaryResponseDto[]>('/items'),
    getItem: (itemId: number | string) => get<ItemResponseDto>(`/items/${itemId}`),
    getItemForManagement: (itemId: number | string) => get<ItemResponseDto>(`/items/manage/${itemId}`),
    createItem: (data: ItemCreatePayload) => post<{ id: number; publicId: string }, ItemCreatePayload>('/items', data),
    getMySelling: () => get<ItemSummaryResponseDto[]>('/items/my-selling'),
    getSellerItems: (memberId: number | string) => get<ItemSummaryResponseDto[]>(`/members/${memberId}/items`),
    deleteItem: (itemId: number | string) => del<void>(`/items/${itemId}`),
};

// ============== Item Policy API ==============
export const itemPolicyApi = {
    getWarnings: (data: ItemPolicyWarningRequestDto) =>
        post<ItemPolicyWarningResponseDto, ItemPolicyWarningRequestDto>('/item-policy/warnings', data),
};

// ============== Location API ==============
export const locationsApi = {
    search: (query: string) =>
        get<LocationSearchResponseDto[]>('/locations/search', { params: { query } }),
};

// ============== Seller Share API ==============
export const sellerShareApi = {
    create: (duration: InviteDuration = 'ONE_DAY') =>
        post<SellerShareLinkResponseDto, { duration: InviteDuration }>('/seller-shares', { duration }),
    getMyLinks: () => get<SellerShareLinkSummaryResponseDto[]>('/seller-shares/my'),
    deactivate: (token: string) => del<void>(`/seller-shares/${token}`),
    getShop: (token: string) => get<SellerShopResponseDto>(`/seller-shares/${token}`),
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



// ============== Block API ==============
export const blocksApi = {
    block: (memberId: number | string) => post<BlockResponseDto>(`/blocks/${memberId}`),
    unblock: (memberId: number | string) => del<void>(`/blocks/${memberId}`),
    getMyBlocks: () => get<BlockResponseDto[]>('/blocks/my'),
};

// ============== Report API ==============
export const reportsApi = {
    create: (data: ReportCreateRequestDto) => post<ReportResponseDto, ReportCreateRequestDto>('/reports', data),
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
