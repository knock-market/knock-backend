import { MapPin } from 'lucide-react';

type TradeLocationFieldsProps = {
  locationName: string;
  address: string;
  latitude: string;
  longitude: string;
  onLocationNameChange: (value: string) => void;
  onAddressChange: (value: string) => void;
  onOpenPicker: () => void;
};

const TradeLocationFields = ({
  locationName,
  address,
  latitude,
  longitude,
  onLocationNameChange,
  onAddressChange,
  onOpenPicker,
}: TradeLocationFieldsProps) => {
  const hasCoordinates = latitude.trim() && longitude.trim();

  return (
    <section className="space-y-4" aria-labelledby="trade-location-title">
      <div>
        <h2 id="trade-location-title" className="text-sm font-semibold text-gray-700 mb-1">Pickup Location</h2>
        <p className="text-xs text-gray-500">
          Add a rough place first, then choose the exact meetup point on the map.
        </p>
      </div>

      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">Rough Location</label>
        <input
          type="text"
          value={address}
          onChange={(event) => onAddressChange(event.target.value)}
          placeholder="Station, cafe, or street name"
          className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
        />
      </div>

      <button
        type="button"
        onClick={onOpenPicker}
        className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-gray-900 px-4 py-3 text-sm font-bold text-white hover:bg-emerald-700 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-700"
      >
        <MapPin size={18} />
        Pick Pickup Location
      </button>

      <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
        <p className="text-xs font-bold uppercase tracking-wide text-gray-400">Selected Place</p>
        <input
          type="text"
          value={locationName}
          onChange={(event) => onLocationNameChange(event.target.value)}
          placeholder="e.g. Main gate bench"
          className="mt-3 w-full bg-white border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
        />
        {hasCoordinates ? (
          <div className="mt-3 text-xs text-gray-500">
            <p className="font-medium text-gray-700">{address || 'Address not set'}</p>
            <p className="mt-1">{latitude}, {longitude}</p>
          </div>
        ) : (
          <p className="mt-3 text-xs text-gray-500">No exact pickup point selected yet.</p>
        )}
      </div>
    </section>
  );
};

export default TradeLocationFields;
