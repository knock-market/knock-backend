import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Store } from 'lucide-react';
import ImageWithFallback from '../components/ImageWithFallback';
import { authApi, itemsApi } from '../services';
import { ItemStatus, ItemSummaryResponseDto, ItemType, MemberResponseDto } from '../types';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<ItemSummaryResponseDto[]>([]);
  const [me, setMe] = useState<MemberResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      const [marketItemsResult, memberResult] = await Promise.allSettled([
        itemsApi.getMarketplaceItems(),
        authApi.getOptionalMe(),
      ]);

      if (marketItemsResult.status === 'fulfilled') {
        setItems(marketItemsResult.value);
      } else {
        console.error('Failed to fetch marketplace items', marketItemsResult.reason);
        setItems([]);
      }

      setMe(memberResult.status === 'fulfilled' ? memberResult.value : null);
      setIsLoading(false);
    };

    load();
  }, []);

  if (isLoading) {
    return <div className="min-h-screen bg-gray-50 flex items-center justify-center text-gray-500">Loading market...</div>;
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto relative">
      <div className="fixed bottom-24 left-0 right-0 max-w-md mx-auto z-40 px-6 flex justify-end pointer-events-none">
        <button
          onClick={() => navigate('/create')}
          className="w-14 h-14 bg-zinc-900 hover:bg-blue-700 text-white rounded-lg shadow-lg shadow-gray-200 transition-colors flex items-center justify-center pointer-events-auto focus-visible:ring-2 focus-visible:ring-blue-700 focus-visible:ring-offset-2"
          aria-label="List an item"
        >
          <Plus size={28} strokeWidth={2.5} />
        </button>
      </div>

      <header className="bg-white px-6 pt-12 pb-6 sticky top-0 z-10 shadow-sm border-b border-gray-100">
        <p className="text-xs font-bold uppercase tracking-wide text-blue-700">Personal Shelves</p>
        <h1 className="mt-2 text-2xl font-bold text-gray-900">Browse seller shelves</h1>
        <p className="text-gray-600 text-sm mt-2">
          Open a seller page from a shared link, then reserve a pickup.
        </p>
        {me?.id && (
          <button
            type="button"
            onClick={() => navigate(`/seller/${me.id}`)}
            className="mt-5 inline-flex items-center gap-2 rounded-lg bg-gray-900 px-4 py-2 text-sm font-bold text-white hover:bg-blue-700 transition-colors focus-visible:ring-2 focus-visible:ring-blue-700"
          >
            <Store size={16} />
            My seller page
          </button>
        )}
      </header>

      <main className="p-4 grid grid-cols-2 gap-4">
        {items.length > 0 ? (
          items.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => navigate(`/item/${item.publicId}`)}
              className="text-left bg-white rounded-lg overflow-hidden shadow-sm border border-gray-200 hover:border-blue-400 hover:shadow-md transition-colors focus-visible:ring-2 focus-visible:ring-blue-700"
            >
              <div className="relative aspect-square bg-gray-100 overflow-hidden">
                <ImageWithFallback src={item.thumbnailUrl} alt={item.title} className="w-full h-full object-cover" />
                {item.status !== ItemStatus.ON_SALE && (
                  <span className="absolute top-2 left-2 bg-gray-900 text-white text-[10px] font-bold px-2 py-1 rounded-md">
                    {item.status}
                  </span>
                )}
              </div>
              <div className="p-3">
                <p className="text-[10px] font-bold uppercase tracking-wide text-blue-700 truncate">
                  {item.writerNickname || `Seller #${item.writerId}`}
                </p>
                <h2 className="mt-1 font-semibold text-sm text-gray-900 truncate">{item.title}</h2>
                <p className={`mt-1 text-sm font-bold ${item.type === ItemType.GIVE ? 'text-emerald-700' : 'text-gray-900'}`}>
                  {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                </p>
                <p className="mt-2 text-[10px] text-gray-500 truncate">
                  {item.tradeLocationName || item.tradeLocationAddress || 'Pickup point pending'}
                </p>
              </div>
            </button>
          ))
        ) : (
          <div className="col-span-2 flex flex-col items-center justify-center py-24 text-center">
            <div className="w-20 h-20 bg-white rounded-lg flex items-center justify-center text-gray-400 mb-6 border border-gray-100">
              <Store size={40} strokeWidth={1.5} />
            </div>
            <h2 className="text-lg font-bold text-gray-900 mb-2">No shelves yet</h2>
            <p className="text-sm text-gray-500 max-w-[220px] mx-auto leading-relaxed">
              List your first item and share your seller page.
            </p>
          </div>
        )}
      </main>
    </div>
  );
};

export default Home;
