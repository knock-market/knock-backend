import { FormEvent, useEffect, useState } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import { ItemStatus } from '../types';
import {
  defaultListingFilters,
  listingSortOptions,
  ListingFilters,
} from '../utils/listingFilters';

type ListingFilterBarProps = {
  filters: ListingFilters;
  onApply: (filters: ListingFilters) => void;
};

const ListingFilterBar = ({ filters, onApply }: ListingFilterBarProps) => {
  const [draft, setDraft] = useState(filters);

  useEffect(() => {
    setDraft(filters);
  }, [filters]);

  const submit = (event: FormEvent) => {
    event.preventDefault();
    onApply(draft);
  };

  return (
    <form onSubmit={submit} className="space-y-3 rounded-xl border border-gray-100 bg-white p-3 shadow-sm">
      <div className="flex items-center gap-2 rounded-lg bg-gray-50 px-3 py-2">
        <Search size={16} className="text-gray-400" />
        <input
          value={draft.keyword}
          onChange={(event) => setDraft({ ...draft, keyword: event.target.value })}
          maxLength={100}
          placeholder="Search title or description"
          className="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-gray-400"
        />
      </div>

      <div className="flex items-center gap-2 rounded-lg bg-gray-50 px-3 py-2">
        <SlidersHorizontal size={16} className="text-gray-400" />
        <input
          value={draft.location}
          onChange={(event) => setDraft({ ...draft, location: event.target.value })}
          maxLength={100}
          placeholder="Pickup location"
          className="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-gray-400"
        />
      </div>

      <div className="grid grid-cols-2 gap-2">
        <select
          value={draft.status}
          onChange={(event) => setDraft({ ...draft, status: event.target.value as ItemStatus })}
          className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs font-semibold text-gray-700 outline-none focus:ring-2 focus:ring-emerald-500"
          aria-label="Item status"
        >
          {Object.values(ItemStatus).map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </select>
        <select
          value={draft.sort}
          onChange={(event) => setDraft({ ...draft, sort: event.target.value as ListingFilters['sort'] })}
          className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs font-semibold text-gray-700 outline-none focus:ring-2 focus:ring-emerald-500"
          aria-label="Sort items"
        >
          {listingSortOptions.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </div>

      <div className="flex items-center gap-2">
        <button
          type="submit"
          className="flex-1 rounded-lg bg-gray-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-emerald-700 focus-visible:ring-2 focus-visible:ring-emerald-600"
        >
          Apply filters
        </button>
        <button
          type="button"
          onClick={() => onApply(defaultListingFilters)}
          className="inline-flex items-center justify-center rounded-lg border border-gray-200 px-3 py-2 text-sm font-bold text-gray-600 transition-colors hover:bg-gray-50 focus-visible:ring-2 focus-visible:ring-gray-400"
          aria-label="Reset filters"
        >
          <X size={16} />
        </button>
      </div>
    </form>
  );
};

export default ListingFilterBar;
