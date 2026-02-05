import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, Package, Loader2 } from 'lucide-react';
import { bookmarksApi } from '../services';
import { MOCK_ITEMS } from '../constants';
import { ItemType, MyBookmarkResponseDto, ItemWithUI } from '../types';
import ImageWithFallback from '../components/ImageWithFallback';

const Likes: React.FC = () => {
  const navigate = useNavigate();
  const [likedItems, setLikedItems] = useState<ItemWithUI[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setIsLoading(true);
    bookmarksApi.getMyBookmarks()
      .then((res: any) => {
        const bookmarks: MyBookmarkResponseDto[] = res.data || [];
        // Map API response to ItemWithUI format
        const items: ItemWithUI[] = bookmarks.map(b => ({
          id: b.id,
          title: b.title,
          price: b.price,
          thumbnailUrl: b.thumbnailUrl,
          image: b.thumbnailUrl,
          type: b.price === 0 ? ItemType.FREE : ItemType.SALE,
          status: 'AVAILABLE' as any,
          category: '',
          postedAt: b.createdAt ? new Date(b.createdAt).toLocaleDateString() : ''
        }));
        setLikedItems(items);
      })
      .catch(() => {
        // Fallback to mock data
        setLikedItems([MOCK_ITEMS[0], MOCK_ITEMS[2]]);
      })
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) {
    return (
      <div className="bg-white min-h-screen flex items-center justify-center max-w-md mx-auto">
        <Loader2 className="w-8 h-8 text-emerald-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
      <div className="px-6 py-5 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <h1 className="text-xl font-bold text-gray-900">Liked Items</h1>
      </div>

      <div className="p-4 space-y-4">
        {likedItems.length > 0 ? (
          likedItems.map((item) => (
            <div
              key={item.id}
              onClick={() => navigate(`/item/${item.id}`)}
              className="bg-white border border-gray-100 rounded-2xl p-3 flex space-x-4 cursor-pointer hover:shadow-md transition-all active:scale-[0.99]"
            >
              <div className="relative w-24 h-24 bg-gray-100 rounded-xl overflow-hidden flex-shrink-0">
                <ImageWithFallback
                  src={item.image || item.thumbnailUrl}
                  alt={item.title}
                  className="w-full h-full object-cover"
                />
                <div className="absolute top-1 right-1 bg-white/80 backdrop-blur-sm p-1.5 rounded-full">
                  <Heart size={14} className="text-red-500 fill-current" />
                </div>
                {item.type === ItemType.FREE && (
                  <span className="absolute bottom-0 left-0 right-0 bg-emerald-500/90 text-white text-[10px] font-bold px-2 py-0.5 text-center">FREE</span>
                )}
              </div>

              <div className="flex-1 flex flex-col justify-center py-1">
                <div className="mb-1">
                  <span className="text-[10px] text-gray-400 font-medium mb-1 block">{item.groupName || 'Bookmarked'}</span>
                  <h3 className="font-bold text-gray-900 text-sm line-clamp-2 leading-snug">{item.title}</h3>
                </div>
                <div className="mt-auto">
                  <span className={`font-bold text-sm ${item.type === ItemType.FREE ? 'text-emerald-600' : 'text-gray-900'}`}>
                    {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                  </span>
                  {item.postedAt && <p className="text-[10px] text-gray-400 mt-1">{item.postedAt}</p>}
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="flex flex-col items-center justify-center py-32 text-center">
            <div className="w-20 h-20 bg-gray-50 rounded-3xl flex items-center justify-center text-gray-300 mb-6 group-hover:scale-110 transition-transform">
              <Heart size={40} strokeWidth={1.5} className="text-gray-200" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">No liked items yet</h3>
            <p className="text-sm text-gray-500 max-w-[200px] mx-auto leading-relaxed">
              Items you heart will appear here so you can find them easily later.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Likes;