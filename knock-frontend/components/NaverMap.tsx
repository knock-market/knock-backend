import { useEffect, useMemo, useRef, useState } from 'react';
import { getNaverMapClientId, loadNaverMap } from '../utils/naverMap';

type NaverMapProps = {
  latitude?: number;
  longitude?: number;
  name?: string;
  address?: string;
};

const NaverMap = ({ latitude, longitude, name, address }: NaverMapProps) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const [mapUnavailable, setMapUnavailable] = useState(false);
  const clientId = getNaverMapClientId();
  const hasCoordinates = Number.isFinite(latitude) && Number.isFinite(longitude);
  const searchQuery = useMemo(() => encodeURIComponent(address || name || 'pickup location'), [address, name]);
  const searchUrl = `https://map.naver.com/p/search/${searchQuery}`;

  useEffect(() => {
    if (!clientId || !hasCoordinates || !mapRef.current || latitude === undefined || longitude === undefined) {
      setMapUnavailable(true);
      return;
    }

    let mounted = true;
    loadNaverMap(clientId)
      .then(() => {
        if (!mounted || !mapRef.current || !window.naver?.maps) return;
        const position = new window.naver.maps.LatLng(latitude, longitude);
        const map = new window.naver.maps.Map(mapRef.current, {
          center: position,
          zoom: 15,
        });
        new window.naver.maps.Marker({ position, map });
        setMapUnavailable(false);
      })
      .catch(() => setMapUnavailable(true));

    return () => {
      mounted = false;
    };
  }, [clientId, hasCoordinates, latitude, longitude]);

  return (
    <div className="space-y-3">
      {hasCoordinates && !mapUnavailable && <div ref={mapRef} className="h-44 w-full rounded-lg bg-gray-100" />}
      <div>
        <p className="text-sm font-semibold text-gray-900">{name || 'Pickup by arrangement'}</p>
        {address && <p className="text-xs text-gray-500 mt-1">{address}</p>}
      </div>
      <a
        href={searchUrl}
        target="_blank"
        rel="noreferrer"
        className="inline-flex text-sm font-semibold text-emerald-700 hover:text-emerald-800 focus-visible:ring-2 focus-visible:ring-emerald-600 rounded-lg"
      >
        Open in Naver Map
      </a>
    </div>
  );
};

export default NaverMap;
