import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, ChevronRight, Loader2, Package } from 'lucide-react';
import { itemsApi } from '../services';
import { ItemStatus, ItemSummaryResponseDto, ItemWithUI } from '../types';
import ImageWithFallback from '../components/ImageWithFallback';

const ManageItems: React.FC = () => {
  const navigate = useNavigate();
  const [myItems, setMyItems] = useState<ItemWithUI[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      try {
        const items: ItemSummaryResponseDto[] = await itemsApi.getMySelling();
        const mappedItems: ItemWithUI[] = items.map((item) => ({
          ...item,
          image: item.thumbnailUrl,
        }));
        setMyItems(mappedItems);
      } catch (error) {
        console.error('Failed to fetch my listings', error);
        setMyItems([]);
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
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button onClick={() => navigate('/profile')} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">My Listings</h1>
      </div>

      <div className="p-4 space-y-4">
        {myItems.length > 0 ? (
          myItems.map((item) => (
            <Link
              key={item.id}
              to={`/manage-item/${item.id}`}
              className="bg-white border border-gray-100 rounded-lg p-4 shadow-sm flex space-x-4 hover:shadow-md transition-shadow active:scale-[0.99] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-600 focus-visible:ring-offset-2"
            >
              <div className="w-20 h-20 bg-gray-100 rounded-xl overflow-hidden flex-shrink-0 relative">
                <ImageWithFallback src={item.image || item.thumbnailUrl} alt={item.title} className="w-full h-full object-cover" />
                {item.status !== ItemStatus.ON_SALE && (
                  <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                    <span className="text-white text-[10px] font-bold border border-white px-2 py-0.5 rounded">
                      {item.status}
                    </span>
                  </div>
                )}
              </div>

              <div className="flex-1 flex flex-col justify-center">
                <h3 className="font-bold text-gray-900 text-sm line-clamp-1">{item.title}</h3>
                <p className="text-sm font-medium text-emerald-600 mt-1">
                  {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                </p>
                <div className="flex items-center justify-between mt-2">
                  <div className="text-xs text-gray-400">{item.likesCount ?? 0} Likes</div>
                  <ChevronRight size={16} className="text-gray-300" />
                </div>
              </div>
            </Link>
          ))
        ) : (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <div className="w-20 h-20 bg-gray-50 rounded-3xl flex items-center justify-center text-gray-400 mb-6">
              <Package size={40} strokeWidth={1.5} />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">No listings yet</h3>
            <p className="text-sm text-gray-500 max-w-[200px] mx-auto leading-relaxed">
              Post your first item, then share your shelf link.
            </p>
            <button
              onClick={() => navigate('/create')}
              className="mt-8 px-8 py-3 bg-emerald-500 text-white rounded-2xl font-bold shadow-lg shadow-emerald-200 transition-all active:scale-[0.98]"
            >
              Start Selling
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ManageItems;
