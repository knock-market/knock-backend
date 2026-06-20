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

export enum ItemStatus {
  ON_SALE = 'ON_SALE',
  RESERVED = 'RESERVED',
  SOLD = 'SOLD',
}

export type ItemListSort =
  | 'LATEST'
  | 'POPULAR'
  | 'PRICE_ASC'
  | 'PRICE_DESC';

export interface ItemListQueryParams {
  keyword?: string;
  location?: string;
  status?: ItemStatus;
  sort?: ItemListSort;
  page?: number;
  size?: number;
}

export enum ReservationStatus {
  WAITING = 'WAITING',
  APPROVED = 'APPROVED',
  COMPLETED = 'COMPLETED',
  CANCELED = 'CANCELED',
}

export type InviteDuration =
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
}

export interface ItemSummaryResponseDto {
  id: number;
  publicId: string;
  title: string;
  price: number;
  type: ItemType;
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

export interface MySellingItemSummaryResponseDto extends ItemSummaryResponseDto {
  viewCount?: number;
}

export interface SellerShareLinkResponseDto {
  token: string;
  path: string;
  expiresAt?: string;
  active: boolean;
  clickCount: number;
  useCount: number;
}

export interface SellerShareLinkSummaryResponseDto {
  token: string;
  path: string;
  expiresAt?: string;
  active: boolean;
  clickCount: number;
  useCount: number;
  createdAt?: string;
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
  publicId: string;
  title: string;
  description: string;
  price: number;
  type: ItemType;
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


export type ItemPolicyWarningSeverity = 'NONE' | 'WARNING';

export interface ItemPolicyWarningRequestDto {
  title: string;
  description: string;
  itemType?: 'SELL' | 'GIVE';
}

export interface ItemPolicyWarningResponseDto {
  policyVersion: string;
  warningCategories: string[];
  policyUrl: string;
  severity: ItemPolicyWarningSeverity;
  message: string;
}

export interface MyBookmarkResponseDto {
  bookmarkId: number;
  itemId: number;
  itemPublicId: string;
  title: string;
  price: number;
  type: ItemType;
  status: ItemStatus;
  thumbnailUrl?: string;
  createdAt?: string;
  itemCreatedAt?: string;
}

export interface BookmarkToggleResponseDto {
  itemId: number;
  toggleOn: boolean;
}


export type ReportTargetType = 'MEMBER' | 'ITEM' | 'RESERVATION' | 'REVIEW';

export type ReportReason =
  | 'PROHIBITED_ITEM'
  | 'SUSPECTED_FRAUD'
  | 'OFF_PLATFORM_PAYMENT'
  | 'PERSONAL_INFO_OR_CODE_REQUEST'
  | 'HARASSMENT_OR_THREAT'
  | 'NO_SHOW'
  | 'COUNTERFEIT_OR_STOLEN_SUSPECTED'
  | 'OTHER';

export type ReportStatus = 'RECEIVED' | 'REVIEWING' | 'RESOLVED' | 'DISMISSED';


export interface BlockResponseDto {
  memberId: number;
  nickname: string;
  profileImageUrl?: string;
  blockedAt?: string;
}

export interface ReportCreateRequestDto {
  targetType: ReportTargetType;
  targetId: number;
  reason: ReportReason;
  description?: string;
}

export interface ReportResponseDto {
  reportId: number;
  status: ReportStatus;
  createdAt?: string;
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
  viewCount?: number;
  seller?: User;
  requesters?: User[];
}
