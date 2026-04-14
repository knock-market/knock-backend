import { useEffect, useRef, useState } from 'react';

const scriptId = 'naver-map-script';

const loadNaverMap = (clientId: string) => new Promise<void>((resolve, reject) => {
  if (window.naver?.maps) {
    resolve();
    return;
  }
  const existing = document.getElementById(scriptId) as HTMLScriptElement | null;
  if (existing) {
    existing.addEventListener('load', () => resolve(), { once: true });
    existing.addEventListener('error', () => reject(new Error('Naver map script failed')), { once: true });
    return;
  }
  const script = document.createElement('script');
  script.id = scriptId;
  script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(clientId)}&submodules=geocoder`;
  script.async = true;
  script.onload = () => resolve();
  script.onerror = () => reject(new Error('Naver map script failed'));
  document.head.appendChild(script);
});

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
  const mapRef = useRef<HTMLDivElement>(null);
  const [isMapReady, setIsMapReady] = useState(false);
  const clientId = import.meta.env.VITE_NAVER_MAP_CLIENT_ID as string | undefined;

  useEffect(() => {
    if (!clientId || !mapRef.current) {
      return;
    }
    let mounted = true;
    loadNaverMap(clientId).then(() => {
      if (!mounted || !mapRef.current || !window.naver?.maps) return;
      const initialLat = Number(latitude) || 37.5665;
      const initialLng = Number(longitude) || 126.978;
      const center = new window.naver.maps.LatLng(initialLat, initialLng);
      const map = new window.naver.maps.Map(mapRef.current, { center, zoom: 15 });
      new window.naver.maps.Marker({ position: center, map });
      window.naver.maps.Event.addListener(map, 'click', (event) => {
        const coord = event.coord as { y?: number; x?: number; lat?: () => number; lng?: () => number };
        const nextLat = typeof coord.lat === 'function' ? coord.lat() : coord.y;
        const nextLng = typeof coord.lng === 'function' ? coord.lng() : coord.x;
        if (typeof nextLat === 'number' && typeof nextLng === 'number') {
          onLatitudeChange(nextLat.toFixed(6));
          onLongitudeChange(nextLng.toFixed(6));
        }
      });
      setIsMapReady(true);
    }).catch(() => setIsMapReady(false));
    return () => {
      mounted = false;
    };
  }, [clientId, latitude, longitude, onLatitudeChange, onLongitudeChange]);

  const searchNaverLocation = () => {
    if (!address.trim() || !window.naver?.maps.Service) return;
    window.naver.maps.Service.geocode({ query: address.trim() }, (status, response) => {
      const result = response as { v2?: { addresses?: Array<{ x: string; y: string; roadAddress?: string }> } };
      const first = result.v2?.addresses?.[0];
      if (status === window.naver?.maps.Service?.Status.OK && first) {
        onLatitudeChange(Number(first.y).toFixed(6));
        onLongitudeChange(Number(first.x).toFixed(6));
        onAddressChange(first.roadAddress || address);
      }
    });
  };

  return (
    <section className="space-y-4" aria-labelledby="trade-location-title">
      <div>
        <h2 id="trade-location-title" className="text-sm font-semibold text-gray-700 mb-1">Pickup Location</h2>
        <p className="text-xs text-gray-500">Search in Naver Map, then tap the meetup point on the map.</p>
      </div>
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">Naver Map Search</label>
        <input
          type="text"
          value={address}
          onChange={(event) => onAddressChange(event.target.value)}
          placeholder="Search a station or public place"
          className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
        />
        <button
          type="button"
          onClick={searchNaverLocation}
          className="mt-2 w-full rounded-lg bg-gray-900 px-4 py-3 text-sm font-bold text-white hover:bg-blue-700 transition-colors focus-visible:ring-2 focus-visible:ring-blue-700"
        >
          Search with Naver Map
        </button>
      </div>
      {clientId ? (
        <div ref={mapRef} className="h-56 w-full rounded-lg bg-gray-100 border border-gray-200" />
      ) : (
        <div className="rounded-lg border border-gray-200 bg-gray-50 p-4 text-sm text-gray-600">
          Add <span className="font-semibold">VITE_NAVER_MAP_CLIENT_ID</span> to enable direct map picking.
        </div>
      )}
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-2">Selected Place Label</label>
        <input
          type="text"
          value={locationName}
          onChange={(event) => onLocationNameChange(event.target.value)}
          placeholder={isMapReady ? 'e.g. Main gate bench' : 'Pickup point label'}
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
