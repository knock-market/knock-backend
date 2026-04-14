type TradeLocationFieldsProps = {
  locationName: string;
  address: string;
  latitude: string;
  longitude: string;
  onLocationNameChange: (value: string) => void;
  onAddressChange: (value: string) => void;
  onLatitudeChange: (value: string) => void;
  onLongitudeChange: (value: string) => void;
};

const TradeLocationFields = ({
  locationName,
  address,
  latitude,
  longitude,
  onLocationNameChange,
  onAddressChange,
  onLatitudeChange,
  onLongitudeChange,
}: TradeLocationFieldsProps) => {
  return (
    <section className="space-y-4" aria-labelledby="trade-location-title">
      <div>
        <h2 id="trade-location-title" className="text-sm font-semibold text-gray-700 mb-1">Pickup Location</h2>
        <p className="text-xs text-gray-500">Set a public meetup point. Exact details can still be confirmed later.</p>
      </div>
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">Place Name</label>
        <input
          type="text"
          value={locationName}
          onChange={(event) => onLocationNameChange(event.target.value)}
          placeholder="Gangnam Station Exit 2"
          className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
        />
      </div>
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">Address</label>
        <input
          type="text"
          value={address}
          onChange={(event) => onAddressChange(event.target.value)}
          placeholder="396 Gangnam-daero, Gangnam-gu, Seoul"
          className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
        />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">Latitude</label>
          <input
            type="number"
            value={latitude}
            onChange={(event) => onLatitudeChange(event.target.value)}
            placeholder="37.498095"
            className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
          />
        </div>
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">Longitude</label>
          <input
            type="number"
            value={longitude}
            onChange={(event) => onLongitudeChange(event.target.value)}
            placeholder="127.027610"
            className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
          />
        </div>
      </div>
    </section>
  );
};

export default TradeLocationFields;
