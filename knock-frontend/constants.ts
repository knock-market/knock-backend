import { ItemCategory } from './types';

export const DEFAULT_IMAGE = 'https://picsum.photos/400/300?grayscale';
export const DEFAULT_AVATAR = 'https://ui-avatars.com/api/?name=Knock+User&background=e2e8f0&color=94a3b8';

export const CATEGORY_LABELS: Record<ItemCategory, string> = {
  [ItemCategory.CLOTHING]: 'Clothing',
  [ItemCategory.FURNITURE]: 'Furniture',
  [ItemCategory.BOOKS]: 'Books',
  [ItemCategory.DIGITAL_DEVICE]: 'Digital / Electronics',
  [ItemCategory.ETC]: 'Other'
};
