import { GroupWithUI, ItemWithUI, ItemStatus, ItemType, ItemCategory, User, NotificationResponseDto } from './types';

export const CURRENT_USER: User = {
  id: 'u1',
  name: 'Alex Kim',
  avatar: 'https://picsum.photos/id/64/100/100',
  role: 'Incheon National Univ.',
  trustScore: 98,
  badges: ['Punctual', 'Kind', 'Fast Reply', 'Accurate']
};

export const DEFAULT_IMAGE = 'https://picsum.photos/400/300?grayscale';

export const CATEGORY_LABELS: Record<ItemCategory, string> = {
  [ItemCategory.CLOTHING]: 'Clothing',
  [ItemCategory.FURNITURE]: 'Furniture',
  [ItemCategory.BOOKS]: 'Books',
  [ItemCategory.DIGITAL_DEVICE]: 'Digital / Electronics',
  [ItemCategory.ETC]: 'Other'
};


export const MOCK_GROUPS: GroupWithUI[] = [
  {
    id: 'my-group',
    name: 'My Private Circle',
    memberCount: 1,
    activeListings: 0,
    image: 'https://images.unsplash.com/photo-1615873968403-89e068629265?auto=format&fit=crop&q=80&w=400',
    description: 'My personal space for sharing with invited friends.'
  },
  {
    id: 'g1',
    name: 'Incheon Univ. IT Dept',
    memberCount: 842,
    activeListings: 12,
    image: 'https://picsum.photos/id/1/400/200',
    description: 'Textbooks, tech gear, and study snacks within our trusted circle.'
  },
  {
    id: 'g2',
    name: 'Design Studio',
    memberCount: 18,
    activeListings: 3,
    image: 'https://picsum.photos/id/2/400/200',
    description: 'Shared tools and materials for the studio.'
  },
  {
    id: 'g3',
    name: 'Greenwood Apartments',
    memberCount: 85,
    activeListings: 5,
    image: 'https://picsum.photos/id/10/400/200',
    description: 'Neighbors sharing household items.'
  }
];

export const MOCK_ITEMS: ItemWithUI[] = [
  {
    id: 'i1',
    title: 'Vintage Film Camera',
    price: 45000,
    type: ItemType.SELL,
    status: ItemStatus.AVAILABLE,
    image: 'https://picsum.photos/id/250/400/400',
    description: 'Classic film camera, works perfectly. Moving out so selling my collection.',
    seller: CURRENT_USER,
    groupName: 'Incheon Univ. IT Dept',
    postedAt: '2 hours ago',
    likes: 12,
    category: ItemCategory.DIGITAL_DEVICE,
    requesters: [
      {
        id: 'r1',
        name: 'Alex Johnson',
        avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=100',
        role: 'Student',
        trustScore: 88,
        badges: []
      },
      {
        id: 'r2',
        name: 'Sarah Miller',
        avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=100',
        role: 'Design Dept',
        trustScore: 95,
        badges: []
      },
      {
        id: 'r3',
        name: 'Mike Wilson',
        avatar: 'https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&q=80&w=100',
        role: 'Staff',
        trustScore: 92,
        badges: []
      }
    ]
  },
  {
    id: 'i2',
    title: 'Calculus Textbook',
    price: 0,
    type: ItemType.GIVE,
    status: ItemStatus.AVAILABLE,
    image: 'https://picsum.photos/id/24/400/400',
    description: 'Used for one semester. Some highlights but good condition.',
    seller: {
      id: 'u2',
      name: 'Sarah Lee',
      avatar: 'https://picsum.photos/id/65/100/100',
      role: 'Student',
      trustScore: 90,
      badges: ['Kind']
    },
    groupName: 'Incheon Univ. IT Dept',
    postedAt: '5 hours ago',
    likes: 4,
    category: ItemCategory.BOOKS,
    requesters: []
  },
  {
    id: 'i4',
    title: 'Mechanical Keyboard',
    price: 85000,
    type: ItemType.SELL,
    status: ItemStatus.AVAILABLE,
    image: 'https://picsum.photos/id/366/400/400',
    description: 'Blue switches. Clicky.',
    seller: {
      id: 'u3',
      name: 'Mike Chen',
      avatar: 'https://picsum.photos/id/77/100/100',
      role: 'Developer',
      trustScore: 95,
      badges: []
    },
    groupName: 'Incheon Univ. IT Dept',
    postedAt: '1 day ago',
    likes: 22,
    category: ItemCategory.DIGITAL_DEVICE,
    requesters: []
  }
];

export const MOCK_NOTIFICATIONS: NotificationResponseDto[] = [
  {
    id: 'n1',
    type: 'RESERVATION',
    title: 'Someone reserved your item',
    content: "Sarah from 'Design Team' wants to pick up the Chair.",
    message: "Sarah from 'Design Team' wants to pick up the Chair.",
    time: '2m ago',
    createdAt: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
    isRead: false
  },
  {
    id: 'n2',
    type: 'CONFIRMATION',
    title: 'Reservation confirmed!',
    content: "Your request for the Camera was accepted.",
    message: "Your request for the Camera was accepted.",
    time: '1h ago',
    createdAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    isRead: true
  },
  {
    id: 'n3',
    type: 'NEW_ITEM',
    title: "New in 'Campus Hub'",
    content: "Leo just posted: MacBook Pro Stand - Free",
    message: "Leo just posted: MacBook Pro Stand - Free",
    time: '5h ago',
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    isRead: true
  }
];