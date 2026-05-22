const scriptId = 'naver-map-script';

export type NaverLatLng = {
  lat?: () => number;
  lng?: () => number;
  y?: number;
  x?: number;
};

export type NaverPoint = {
  x: number;
  y: number;
};

export type NaverMapInstance = {
  setCenter?: (position: unknown) => void;
  panTo?: (position: unknown) => void;
  getCenter?: () => NaverLatLng;
  setZoom?: (zoom: number) => void;
  refresh?: () => void;
};

export type NaverMarkerInstance = {
  setPosition?: (position: unknown) => void;
  setMap?: (map: unknown) => void;
};

declare global {
  interface Window {
    naver?: {
      maps: {
        LatLng: new (latitude: number, longitude: number) => unknown;
        Point: new (x: number, y: number) => NaverPoint;
        Map: new (element: HTMLElement, options: object) => NaverMapInstance;
        Marker: new (options: object) => NaverMarkerInstance;
        Event: {
          addListener: (target: unknown, eventName: string, listener: (event: { coord: unknown }) => void) => void;
        };
        TransCoord?: {
          fromTM128ToLatLng: (point: NaverPoint) => NaverLatLng;
        };
      };
    };
  }
}

export const getNaverMapClientId = (): string | undefined =>
  import.meta.env.VITE_NAVER_MAP_CLIENT_ID as string | undefined;

export const loadNaverMap = (clientId: string) => new Promise<void>((resolve, reject) => {
  if (window.naver?.maps) {
    resolve();
    return;
  }

  const existing = document.getElementById(scriptId) as HTMLScriptElement | null;
  if (existing) {
    const status = existing.dataset.status;
    if (status === 'loaded' && window.naver?.maps) {
      resolve();
      return;
    }
    if (status === 'failed') {
      existing.remove();
    } else {
      existing.addEventListener('load', () => resolve(), { once: true });
      existing.addEventListener('error', () => reject(new Error('Naver map script failed')), { once: true });
      return;
    }
  }

  const script = document.createElement('script');
  script.id = scriptId;
  script.dataset.status = 'loading';
  script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(clientId)}&submodules=geocoder`;
  script.async = true;
  script.onload = () => {
    script.dataset.status = 'loaded';
    resolve();
  };
  script.onerror = () => {
    script.dataset.status = 'failed';
    reject(new Error('Naver map script failed'));
  };
  document.head.appendChild(script);
});

export const getLatLngNumbers = (coord: NaverLatLng): { latitude: number; longitude: number } | null => {
  const latitude = typeof coord.lat === 'function' ? coord.lat() : coord.y;
  const longitude = typeof coord.lng === 'function' ? coord.lng() : coord.x;

  if (typeof latitude !== 'number' || typeof longitude !== 'number') {
    return null;
  }

  return { latitude, longitude };
};
