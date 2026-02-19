import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bookmark, Loader2 } from 'lucide-react';
import { bookmarksApi } from '../services';
import { ItemType, ItemWithUI, MyBookmarkResponseDto } from '../types';
import ImageWithFallback from '../components/ImageWithFallback';

const Bookmarks: React.FC = () => {
  const navigate = useNavigate();
  const [savedItems, setSavedItems] = useState<ItemWithUI[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      try {
        const bookmarks: MyBookmarkResponseDto[] = await bookmarksApi.getMyBookmarks();
        const items: ItemWithUI[] = bookmarks.map((bookmark) => {
          const itemCreatedAt = bookmark.itemCreatedAt ?? bookmark.createdAt;
          return {
            id: bookmark.itemId,
            title: bookmark.title,
            price: bookmark.price,
            thumbnailUrl: bookmark.thumbnailUrl,
            image: bookmark.thumbnailUrl,
            type: bookmark.type,
            status: bookmark.status,
            category: bookmark.category,
            postedAtLabel: itemCreatedAt ? new Date(itemCreatedAt).toLocaleDateString() : '',
          };
        });
        setSavedItems(items);
      } catch (error) {
        console.error('Failed to fetch bookmarks', error);
        setSavedItems([]);
      } finally {
        setIsLoading(false);
      }
    };

    load();
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
        <h1 className="text-xl font-bold text-gray-900">Saved Items</h1>
      </div>

      <div className="p-4 space-y-4">
        {savedItems.length > 0 ? (
          savedItems.map((item) => (
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
                  <Bookmark size={14} className="text-emerald-600 fill-current" />
                </div>
                {item.type === ItemType.GIVE && (
                  <span className="absolute bottom-0 left-0 right-0 bg-emerald-500/90 text-white text-[10px] font-bold px-2 py-0.5 text-center">FREE</span>
                )}
              </div>

              <div className="flex-1 flex flex-col justify-center py-1">
                <div className="mb-1">
                  <span className="text-[10px] text-gray-400 font-medium mb-1 block">Bookmarked</span>
                  <h3 className="font-bold text-gray-900 text-sm line-clamp-2 leading-snug">{item.title}</h3>
                </div>
                <div className="mt-auto">
                  <span className={`font-bold text-sm ${item.type === ItemType.GIVE ? 'text-emerald-600' : 'text-gray-900'}`}>
                    {item.type === ItemType.GIVE ? 'Free' : `₩${item.price.toLocaleString()}`}
                  </span>
                  {item.postedAtLabel && <p className="text-[10px] text-gray-400 mt-1">{item.postedAtLabel}</p>}
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="flex flex-col items-center justify-center py-32 text-center">
            <div className="w-20 h-20 bg-gray-50 rounded-3xl flex items-center justify-center text-gray-300 mb-6 group-hover:scale-110 transition-transform">
              <Bookmark size={40} strokeWidth={1.5} className="text-gray-200" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">No saved items yet</h3>
            <p className="text-sm text-gray-500 max-w-[200px] mx-auto leading-relaxed">
              Items you bookmark will appear here so you can find them easily later.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Bookmarks;
