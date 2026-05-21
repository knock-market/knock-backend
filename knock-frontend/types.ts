export type ApiResultType = 'SUCCESS' | 'ERROR';

export interface ApiErrorDetail {
  code: string;
  message: string;
  data: unknown;
}

export interface ApiResponse<T> {
  result: ApiResultType;
  data: T;
  error: ApiErrorDetail | null;
}

export enum ItemType {
  SELL = 'SELL',
  GIVE = 'GIVE',
}

export enum ItemCategory {
  CLOTHING = 'CLOTHING',
  FURNITURE = 'FURNITURE',
  ETC = 'ETC',
  DIGITAL_DEVICE = 'DIGITAL_DEVICE',
  BOOKS = 'BOOKS',
}

export enum ItemStatus {
  ON_SALE = 'ON_SALE',
  RESERVED = 'RESERVED',
  SOLD = 'SOLD',
}

export enum ReservationStatus {
  WAITING = 'WAITING',
  APPROVED = 'APPROVED',
  COMPLETED = 'COMPLETED',
  CANCELED = 'CANCELED',
}

export type InviteDuration =
  | 'FIVE_MINUTES'
  | 'THIRTY_MINUTES'
  | 'ONE_HOUR'
  | 'ONE_DAY'
  | 'PERMANENT';

export interface MemberResponseDto {
  id?: number;
  email: string;
  name: string;
  nickname: string;
  profileImageUrl?: string;
  provider?: string;
  mannerTemperature?: number;
}

export interface ItemSummaryResponseDto {
  id: number;
  title: string;
  price: number;
  type: ItemType;
  category: ItemCategory;
  status: ItemStatus;
  thumbnailUrl?: string;
  writerId?: number;
  writerNickname?: string;
  writerProfileImageUrl?: string;
  likesCount?: number;
  postedAt?: string;
  tradeLocationName?: string;
  tradeLocationAddress?: string;
  tradeLatitude?: number;
  tradeLongitude?: number;
}

export interface SellerShareLinkResponseDto {
  token: string;
  path: string;
  expiresAt?: string;
}

export interface SellerShopResponseDto {
  sellerId: number;
  sellerName: string;
  sellerNickname: string;
  sellerProfileImageUrl?: string;
  items: ItemSummaryResponseDto[];
}

export interface ItemResponseDto {
  id: number;
  title: string;
  description: string;
  price: number;
  type: ItemType;
  category: ItemCategory;
  status: ItemStatus;
  imageUrls: string[];
  writerId?: number;
  writerNickname?: string;
  writerProfileImageUrl?: string;
  tradeLocationName?: string;
  tradeLocationAddress?: string;
  tradeLatitude?: number;
  tradeLongitude?: number;
}

export interface MyBookmarkResponseDto {
  bookmarkId: number;
  itemId: number;
  title: string;
  price: number;
  type: ItemType;
  category: ItemCategory;
  status: ItemStatus;
  thumbnailUrl?: string;
  createdAt?: string;
  itemCreatedAt?: string;
}

export interface BookmarkToggleResponseDto {
  itemId: number;
  toggleOn: boolean;
}

export interface ReservationCreateResponseDto {
  reservationId: number;
}

export interface ReservationResponseDto {
  id: number;
  itemId: number;
  itemTitle: string;
  memberId: number;
  memberName: string;
  status: ReservationStatus;
  createdAt: string;
}

export interface NotificationResponseDto {
  id: number;
  notificationType: string;
  content: string;
  relatedUrl?: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationSettingsResponseDto {
  push: boolean;
  newItems: boolean;
  chat: boolean;
  marketing: boolean;
  sound: boolean;
}

export interface BlockedUserResponseDto {
  id: number;
  name: string;
  blockedAt: string;
}

export interface ImageUploadResultDto {
  originalFilename: string;
  imageUrl: string;
  s3Key: string;
}

export interface LocationSearchResponseDto {
  name: string;
  address: string;
  latitude?: number;
  longitude?: number;
  naverMapX?: number;
  naverMapY?: number;
}

export interface User {
  id: number | string;
  name: string;
  avatar: string;
  nickname?: string;
  role?: string;
  trustScore?: number;
}

export interface ItemWithUI extends ItemSummaryResponseDto {
  image?: string;
  description?: string;
  postedAtLabel?: string;
  isLiked?: boolean;
  seller?: User;
  requesters?: User[];
}
