import { ItemSummaryResponseDto } from '../types';

type SellerFilterBarProps = {
  items: ItemSummaryResponseDto[];
  activeSellerId: number | null;
  onChange: (sellerId: number | null) => void;
};

const SellerFilterBar = ({ items, activeSellerId, onChange }: SellerFilterBarProps) => {
  const sellers = Array.from(
    new Map(
      items
        .filter((item) => item.writerId)
        .map((item) => [
          item.writerId,
          {
            id: item.writerId as number,
            name: item.writerNickname || `Seller #${item.writerId}`,
          },
        ])
    ).values()
  );

  if (sellers.length === 0) {
    return null;
  }

  return (
    <div className="flex space-x-2 overflow-x-auto no-scrollbar pb-1" aria-label="Seller filter">
      <button
        type="button"
        onClick={() => onChange(null)}
        className={`px-4 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${activeSellerId === null
          ? 'bg-gray-900 text-white'
          : 'bg-white text-gray-600 border border-gray-200 hover:border-emerald-300'
          }`}
      >
        All sellers
      </button>
      {sellers.map((seller) => (
        <button
          key={seller.id}
          type="button"
          onClick={() => onChange(seller.id)}
          className={`px-4 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${activeSellerId === seller.id
            ? 'bg-gray-900 text-white'
            : 'bg-white text-gray-600 border border-gray-200 hover:border-emerald-300'
            }`}
        >
          {seller.name}
        </button>
      ))}
    </div>
  );
};

export default SellerFilterBar;
