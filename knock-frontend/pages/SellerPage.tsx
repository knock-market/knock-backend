import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Package, Plus } from 'lucide-react';
import ImageWithFallback from '../components/ImageWithFallback';
import { authApi, itemsApi } from '../services';
import { ItemStatus, ItemSummaryResponseDto, ItemType, MemberResponseDto } from '../types';

const SellerPage = () => {
  const { memberId } = useParams<{ memberId: string }>();
  const navigate = useNavigate();
  const sellerId = memberId ? Number(memberId) : NaN;
  const hasValidSellerId = Number.isInteger(sellerId) && sellerId > 0;

  const [items, setItems] = useState<ItemSummaryResponseDto[]>([]);
  const [me, setMe] = useState<MemberResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      if (!hasValidSellerId) {
        setItems([]);
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      const [itemResult, meResult] = await Promise.allSettled([
        itemsApi.getSellerItems(sellerId),
        authApi.getMe(),
      ]);

      setItems(itemResult.status === 'fulfilled' ? itemResult.value : []);
      setMe(meResult.status === 'fulfilled' ? meResult.value : null);
      setIsLoading(false);
    };

    load();
  }, [hasValidSellerId, sellerId]);

  const seller = items.find((item) => item.writerId === sellerId);
  const isOwnPage = me?.id === sellerId;
  const sellerName = seller?.writerNickname || (isOwnPage ? me?.nickname : undefined) || `Seller #${sellerId}`;
  const subtitle = isOwnPage
    ? 'Your public selling shelf'
    : 'Items from this seller only';
  const targetPath = useMemo(
    () => (item: ItemSummaryResponseDto) => isOwnPage ? `/manage-item/${item.id}` : `/item/${item.id}`,
    [isOwnPage]
  );

  if (isLoading) {
    return <div className="min-h-screen bg-gray-50 flex items-center justify-center text-gray-500">Loading shelf...</div>;
  }

  if (!hasValidSellerId) {
    return <div className="p-8 text-center text-gray-500">Seller not found</div>;
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto">
      <div className={`px-4 py-5 sticky top-0 z-20 ${isOwnPage ? 'bg-emerald-700 text-white' : 'bg-white text-gray-900 border-b border-gray-100'}`}>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className={`p-2 -ml-2 rounded-lg transition-colors focus-visible:ring-2 ${isOwnPage ? 'hover:bg-white/10 focus-visible:ring-white' : 'hover:bg-gray-100 focus-visible:ring-emerald-600'}`}
          aria-label="Go back"
        >
          <ArrowLeft size={24} />
        </button>
        <div className="mt-6 flex items-center justify-between gap-4">
          <div>
            <p className={`text-xs font-semibold uppercase tracking-wide ${isOwnPage ? 'text-emerald-100' : 'text-emerald-700'}`}>
              {isOwnPage ? 'My shop' : 'Seller shop'}
            </p>
            <h1 className="mt-1 text-2xl font-bold leading-tight">{sellerName}</h1>
            <p className={`mt-2 text-sm ${isOwnPage ? 'text-emerald-50' : 'text-gray-500'}`}>{subtitle}</p>
          </div>
          {isOwnPage && (
            <button
              type="button"
              onClick={() => navigate('/create')}
              className="shrink-0 inline-flex items-center gap-2 rounded-lg bg-white px-3 py-2 text-sm font-bold text-emerald-800 shadow-sm focus-visible:ring-2 focus-visible:ring-white"
            >
              <Plus size={16} />
              Add
            </button>
          )}
        </div>
      </div>

      <div className="p-4 grid grid-cols-2 gap-4">
        {items.length > 0 ? (
          items.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => navigate(targetPath(item))}
              className="text-left bg-white rounded-lg overflow-hidden shadow-sm border border-gray-200 hover:border-emerald-300 hover:shadow-md transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
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
                <h2 className="font-semibold text-sm text-gray-900 truncate">{item.title}</h2>
                <p className={`mt-1 text-sm font-bold ${item.type === ItemType.GIVE ? 'text-emerald-700' : 'text-gray-900'}`}>
                  {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                </p>
                <p className="mt-2 text-[10px] text-gray-500 truncate">
                  {item.tradeLocationName || item.tradeLocationAddress || 'Pickup by arrangement'}
                </p>
              </div>
            </button>
          ))
        ) : (
          <div className="col-span-2 flex flex-col items-center justify-center py-24 text-center">
            <div className="w-20 h-20 bg-white rounded-lg flex items-center justify-center text-gray-400 mb-6 border border-gray-100">
              <Package size={40} strokeWidth={1.5} />
            </div>
            <h2 className="text-lg font-bold text-gray-900 mb-2">No items yet</h2>
            <p className="text-sm text-gray-500 max-w-[220px] mx-auto leading-relaxed">
              {isOwnPage ? 'Post your first item to open your shelf.' : 'This seller has no public items right now.'}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default SellerPage;
