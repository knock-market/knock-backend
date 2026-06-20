import ImageWithFallback from './ImageWithFallback';
import { ItemStatus, ItemSummaryResponseDto, ItemType } from '../types';

type SellerShelfItemCardProps = {
  item: ItemSummaryResponseDto;
  onClick: () => void;
};

const SellerShelfItemCard = ({ item, onClick }: SellerShelfItemCardProps) => (
  <button
    type="button"
    onClick={onClick}
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
);

export default SellerShelfItemCard;
