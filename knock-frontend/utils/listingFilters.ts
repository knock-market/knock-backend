import { ItemListQueryParams, ItemListSort, ItemStatus } from '../types';

export type ListingFilters = {
  keyword: string;
  location: string;
  status: ItemStatus;
  sort: ItemListSort;
  page: number;
  size: number;
};

const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 20;
const MAX_SIZE = 50;

export const defaultListingFilters: ListingFilters = {
  keyword: '',
  location: '',
  status: ItemStatus.ON_SALE,
  sort: 'LATEST',
  page: DEFAULT_PAGE,
  size: DEFAULT_SIZE,
};

export const listingSortOptions: { value: ItemListSort; label: string }[] = [
  { value: 'LATEST', label: 'Newest' },
  { value: 'POPULAR', label: 'Most interested' },
  { value: 'PRICE_ASC', label: 'Lowest price' },
  { value: 'PRICE_DESC', label: 'Highest price' },
];

const statusValues = new Set(Object.values(ItemStatus));
const sortValues = new Set(listingSortOptions.map((option) => option.value));

export const readListingFilters = (params: URLSearchParams): ListingFilters => ({
  keyword: params.get('keyword')?.trim() || '',
  location: params.get('location')?.trim() || '',
  status: normalizeStatus(params.get('status')),
  sort: normalizeSort(params.get('sort')),
  page: normalizePage(params.get('page')),
  size: normalizeSize(params.get('size')),
});

export const toListingQueryParams = (filters: ListingFilters): ItemListQueryParams => {
  const query: ItemListQueryParams = {
    status: filters.status,
    sort: filters.sort,
    page: filters.page,
    size: filters.size,
  };
  if (filters.keyword.trim()) {
    query.keyword = filters.keyword.trim();
  }
  if (filters.location.trim()) {
    query.location = filters.location.trim();
  }
  return query;
};

export const toSearchParams = (filters: ListingFilters): URLSearchParams => {
  const params = new URLSearchParams();
  const query = toListingQueryParams(filters);
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value));
    }
  });
  return params;
};

export const hasActiveListingFilters = (filters: ListingFilters): boolean => (
  Boolean(filters.keyword.trim()) ||
  Boolean(filters.location.trim()) ||
  filters.status !== defaultListingFilters.status ||
  filters.sort !== defaultListingFilters.sort
);

const normalizeStatus = (value: string | null): ItemStatus => {
  if (value && statusValues.has(value as ItemStatus)) {
    return value as ItemStatus;
  }
  return defaultListingFilters.status;
};

const normalizeSort = (value: string | null): ItemListSort => {
  if (value && sortValues.has(value as ItemListSort)) {
    return value as ItemListSort;
  }
  return defaultListingFilters.sort;
};

const normalizePage = (value: string | null): number => {
  const page = Number(value);
  return Number.isInteger(page) && page >= 0 ? page : DEFAULT_PAGE;
};

const normalizeSize = (value: string | null): number => {
  const size = Number(value);
  return Number.isInteger(size) && size >= 1 && size <= MAX_SIZE ? size : DEFAULT_SIZE;
};
