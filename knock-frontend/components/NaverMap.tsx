import { useEffect, useMemo, useRef, useState } from 'react';

type NaverMapProps = {
  latitude?: number;
  longitude?: number;
  name?: string;
  address?: string;
};

declare global {
  interface Window {
    naver?: {
      maps: {
        LatLng: new (latitude: number, longitude: number) => unknown;
        Map: new (element: HTMLElement, options: object) => unknown;
        Marker: new (options: object) => unknown;
      };
    };
  }
}

const scriptId = 'naver-map-script';

const loadNaverMap = (clientId: string) => {
  const existing = document.getElementById(scriptId) as HTMLScriptElement | null;
  if (existing) {
    return new Promise<void>((resolve, reject) => {
      if (window.naver?.maps) {
        resolve();
        return;
      }
      existing.addEventListener('load', () => resolve(), { once: true });
      existing.addEventListener('error', () => reject(new Error('Naver map script failed')), { once: true });
    });
  }

  return new Promise<void>((resolve, reject) => {
    const script = document.createElement('script');
    script.id = scriptId;
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(clientId)}`;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Naver map script failed'));
    document.head.appendChild(script);
  });
};

const NaverMap = ({ latitude, longitude, name, address }: NaverMapProps) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const [mapUnavailable, setMapUnavailable] = useState(false);
  const clientId = import.meta.env.VITE_NAVER_MAP_CLIENT_ID as string | undefined;
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
