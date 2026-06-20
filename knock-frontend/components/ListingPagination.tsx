import { ListingFilters, toSearchParams } from '../utils/listingFilters';

type ListingPaginationProps = {
  filters: ListingFilters;
  itemCount: number;
  onChange: (params: URLSearchParams) => void;
};

const ListingPagination = ({ filters, itemCount, onChange }: ListingPaginationProps) => {
  const canGoPrevious = filters.page > 0;
  const canGoNext = itemCount >= filters.size;

  const moveToPage = (page: number) => {
    onChange(toSearchParams({ ...filters, page }));
  };

  return (
    <div className="px-4 pb-6">
      <div className="flex items-center justify-between rounded-lg bg-white px-4 py-3 text-sm shadow-sm border border-gray-100">
        <span className="font-semibold text-gray-600">Page {filters.page + 1}</span>
        <div className="flex gap-2">
          <button
            type="button"
            disabled={!canGoPrevious}
            onClick={() => moveToPage(filters.page - 1)}
            className="rounded-md border border-gray-200 px-3 py-2 font-bold text-gray-700 disabled:opacity-40"
          >
            Prev
          </button>
          <button
            type="button"
            disabled={!canGoNext}
            onClick={() => moveToPage(filters.page + 1)}
            className="rounded-md bg-gray-900 px-3 py-2 font-bold text-white disabled:bg-gray-300"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default ListingPagination;
