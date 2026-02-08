// ============== Enums (Match Backend) ==============
export enum ItemType {
  SELL = 'SELL',
  GIVE = 'GIVE'
}

export enum ItemCategory {
  CLOTHING = 'CLOTHING',
  FURNITURE = 'FURNITURE',
  ETC = 'ETC',
  DIGITAL_DEVICE = 'DIGITAL_DEVICE',
  BOOKS = 'BOOKS'
}

export enum ItemStatus {
  AVAILABLE = 'AVAILABLE',
  RESERVED = 'RESERVED',
  COMPLETED = 'COMPLETED'
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// ============== API Response DTOs (Match Backend) ==============

/** GET /api/v1/members/my */
export interface MemberResponseDto {
  id: string | number;
  email: string;
  name: string;
  nickname: string;
  profileImageUrl?: string;
}

/** GET /api/v1/groups/my, GET /api/v1/groups/{id} */
export interface GroupResponseDto {
  id: string | number;
  name: string;
  description?: string;
  // NOTE: memberCount and profileImageUrl are NOT in current backend DTO
  // Frontend will handle missing values gracefully
}

/** GET /api/v1/groups/{groupId}/items */
export interface ItemSummaryResponseDto {
  id: string | number;
  title: string;
  price: number;
  type: ItemType;
  category: ItemCategory;
  status: ItemStatus;
  thumbnailUrl?: string;
  writerId?: string | number;
}

/** GET /api/v1/items/{id} */
export interface ItemResponseDto {
  id: string | number;
  title: string;
  description: string;
  price: number;
  type: ItemType;
  category: ItemCategory;
  status: ItemStatus;
  imageUrls: string[];
  writerId?: string | number;
  // NOTE: writerNickname, writerProfileImageUrl are NOT in current backend
}

/** GET /api/v1/items/my-bookmarks */
export interface MyBookmarkResponseDto {
  id: string | number;
  title: string;
  price: number;
  thumbnailUrl?: string;
  createdAt: string;
}

/** GET /api/v1/reservations/my */
export interface ReservationResponseDto {
  id: string | number;
  itemId: string | number;
  itemTitle: string;
  memberId: string | number;
  memberName: string;
  status: ReservationStatus;
  createdAt: string;
}

/** GET /api/v1/notifications */
export interface NotificationResponseDto {
  id: string | number;
  type: string;
  content: string;
  relatedUrl?: string;
  isRead: boolean;
  createdAt: string;
  // Frontend compat fields
  title?: string;
  message?: string;
  time?: string;
}

// ============== Frontend Extended Types ==============
// These extend API types with UI-specific properties

export interface User {
  id: string | number;
  name: string;
  avatar: string;
  nickname?: string;
  role?: string;
  trustScore?: number;
  badges?: string[];
}

export interface GroupWithUI extends GroupResponseDto {
  memberCount?: number;
  activeListings?: number;
  image?: string; // For UI compatibility, maps to profileImageUrl
}

export interface ItemWithUI extends ItemSummaryResponseDto {
  image?: string; // Alias for thumbnailUrl
  description?: string; // For item detail
  postedAt?: string;
  likes?: number;
  isLiked?: boolean;
  groupName?: string;
  seller?: User;
  requesters?: User[];
}