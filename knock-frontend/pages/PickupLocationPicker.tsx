import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, Check, Crosshair, Loader2, MapPin, Search } from 'lucide-react';
import { locationsApi } from '../services';
import { LocationSearchResponseDto } from '../types';
import {
  getLatLngNumbers,
  getNaverMapClientId,
  loadNaverMap,
  NaverMapInstance,
  NaverMarkerInstance,
} from '../utils/naverMap';

type PickupLocationState = {
  draft?: Record<string, unknown>;
  location?: {
    locationName?: string;
    address?: string;
    latitude?: string;
    longitude?: string;
  };
};

const SEOUL_CITY_HALL = { latitude: 37.5665, longitude: 126.978 };
const MAP_LOAD_FAIL_MESSAGE = 'Naver Map could not be loaded. Check the Client ID, allowed web URL, and Dynamic Map setting in Naver Cloud.';
const SEARCH_FAIL_MESSAGE = 'No matching pickup location found. Try a nearby station, cafe, or street address.';
const SEARCH_READY_MESSAGE = 'Select the pickup location that best matches your search.';
const SEARCH_ERROR_MESSAGE = 'Place search is unavailable. Add NAVER_SEARCH_CLIENT_ID and NAVER_SEARCH_CLIENT_SECRET to the backend environment.';
const MAP_TILE_FAIL_MESSAGE = 'Map tiles are not visible. Check the frontend Client ID, allowed web URL, and Dynamic Map setting.';

const formatCoordinate = (value: number) => value.toFixed(6);

const PickupLocationPicker = () => {
  const navigate = useNavigate();
  const routeLocation = useLocation();
  const state = (routeLocation.state || {}) as PickupLocationState;
  const initialLocation = state.location || {};
  const clientId = getNaverMapClientId();

  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<NaverMapInstance | null>(null);
  const markerRef = useRef<NaverMarkerInstance | null>(null);

  const [query, setQuery] = useState(initialLocation.address || initialLocation.locationName || '');
  const [locationName, setLocationName] = useState(initialLocation.locationName || '');
  const [address, setAddress] = useState(initialLocation.address || '');
  const [latitude, setLatitude] = useState(initialLocation.latitude || '');
  const [longitude, setLongitude] = useState(initialLocation.longitude || '');
  const [isMapReady, setIsMapReady] = useState(false);
  const [mapLoadFailed, setMapLoadFailed] = useState(false);
  const [mapTilesVisible, setMapTilesVisible] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [searchResults, setSearchResults] = useState<LocationSearchResponseDto[]>([]);

  const setSelectedPoint = (nextLatitude: number, nextLongitude: number) => {
    setLatitude(formatCoordinate(nextLatitude));
    setLongitude(formatCoordinate(nextLongitude));
  };

  const moveMap = (nextLatitude: number, nextLongitude: number) => {
    if (!window.naver?.maps) {
      setSelectedPoint(nextLatitude, nextLongitude);
      return;
    }
    const position = new window.naver.maps.LatLng(nextLatitude, nextLongitude);
    mapInstanceRef.current?.setCenter?.(position);
    markerRef.current?.setPosition?.(position);
    setSelectedPoint(nextLatitude, nextLongitude);
  };

  const getSearchResultPoint = (result: LocationSearchResponseDto) => {
    if (Number.isFinite(result.latitude) && Number.isFinite(result.longitude)) {
      return { latitude: result.latitude as number, longitude: result.longitude as number };
    }

    if (!window.naver?.maps.TransCoord || !Number.isFinite(result.naverMapX) || !Number.isFinite(result.naverMapY)) {
      return null;
    }

    const point = new window.naver.maps.Point(result.naverMapX as number, result.naverMapY as number);
    return getLatLngNumbers(window.naver.maps.TransCoord.fromTM128ToLatLng(point));
  };

  const selectSearchResult = (result: LocationSearchResponseDto) => {
    setLocationName(result.name);
    setAddress(result.address);
    setQuery(result.name || result.address);
    setStatusMessage('');
    const point = getSearchResultPoint(result);
    if (!point) {
      setStatusMessage(MAP_TILE_FAIL_MESSAGE);
      return;
    }
    moveMap(point.latitude, point.longitude);
  };

  const searchLocation = async () => {
    if (!query.trim()) {
      setStatusMessage('Enter a station, address, or public place first.');
      return;
    }

    setIsSearching(true);
    setStatusMessage('');
    setSearchResults([]);

    try {
      const results = await locationsApi.search(query.trim());
      setSearchResults(results);
      if (results.length === 0) {
        setStatusMessage(SEARCH_FAIL_MESSAGE);
        return;
      }
      setStatusMessage(SEARCH_READY_MESSAGE);
    } catch {
      setStatusMessage(SEARCH_ERROR_MESSAGE);
      setAddress(query.trim());
      setLocationName((current) => current || query.trim());
    } finally {
      setIsSearching(false);
    }
  };

  const useCurrentLocation = () => {
    if (!navigator.geolocation) {
      setStatusMessage('Current location is not available in this browser.');
      return;
    }

    setStatusMessage('Checking your current location...');
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude: nextLatitude, longitude: nextLongitude } = position.coords;
        setLocationName('My current location');
        setAddress('Current location');
        setQuery('Current location');
        setStatusMessage('');
        moveMap(nextLatitude, nextLongitude);
      },
      () => setStatusMessage('Could not read your current location. Check browser permission.'),
      { enableHighAccuracy: true, timeout: 8000 }
    );
  };

  const confirmLocation = () => {
    navigate('/create', {
      replace: true,
      state: {
        draft: state.draft,
        location: { locationName, address, latitude, longitude },
      },
    });
  };

  useEffect(() => {
    if (!clientId || !mapRef.current) {
      setIsMapReady(false);
      setMapLoadFailed(false);
      return;
    }

    let mounted = true;
    loadNaverMap(clientId).then(() => {
      if (!mounted || !mapRef.current || !window.naver?.maps) return;
      setMapLoadFailed(false);
      setMapTilesVisible(true);
      const initialLatitude = Number(initialLocation.latitude) || SEOUL_CITY_HALL.latitude;
      const initialLongitude = Number(initialLocation.longitude) || SEOUL_CITY_HALL.longitude;
      const center = new window.naver.maps.LatLng(initialLatitude, initialLongitude);
      const map = new window.naver.maps.Map(mapRef.current, { center, zoom: 16 });
      const marker = new window.naver.maps.Marker({ position: center, map });
      mapInstanceRef.current = map;
      markerRef.current = marker;
      setSelectedPoint(initialLatitude, initialLongitude);

      window.naver.maps.Event.addListener(map, 'click', (event) => {
        const point = getLatLngNumbers(event.coord as never);
        if (!point) return;
        moveMap(point.latitude, point.longitude);
      });
      window.naver.maps.Event.addListener(map, 'idle', () => {
        const centerPoint = map.getCenter?.();
        if (!centerPoint) return;
        const point = getLatLngNumbers(centerPoint);
        if (!point) return;
        marker.setPosition?.(new window.naver!.maps.LatLng(point.latitude, point.longitude));
        setSelectedPoint(point.latitude, point.longitude);
      });

      setIsMapReady(true);
      window.setTimeout(() => map.refresh?.(), 0);
      window.setTimeout(() => {
        if (!mapRef.current) return;
        const visibleTile = Array.from(mapRef.current.querySelectorAll('img')).some((image) => {
          const tile = image as HTMLImageElement;
          return tile.complete && tile.naturalWidth > 0 && tile.naturalHeight > 0;
        });
        setMapTilesVisible(visibleTile);
        if (!visibleTile) {
          setStatusMessage(MAP_TILE_FAIL_MESSAGE);
        }
      }, 2500);
    }).catch(() => {
      if (mounted) {
        setIsMapReady(false);
        setMapLoadFailed(true);
        setStatusMessage(MAP_LOAD_FAIL_MESSAGE);
      }
    });

    return () => {
      mounted = false;
      markerRef.current?.setMap?.(null);
    };
  }, [clientId, initialLocation.latitude, initialLocation.longitude]);

  useEffect(() => {
    if (!isMapReady || initialLocation.latitude || !query.trim()) return;
    searchLocation();
  }, [isMapReady]);

  const canConfirm = latitude.trim() && longitude.trim();

  return (
    <div className="bg-white min-h-screen max-w-md mx-auto">
      <header className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-20">
        <button
          type="button"
          onClick={() => navigate('/create', { replace: true, state })}
          className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
          aria-label="Go back"
        >
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Pick Pickup Location</h1>
      </header>

      <main className="p-4 space-y-4 pb-8">
        <div className="rounded-lg border border-gray-200 bg-gray-50 p-3">
          <label className="block text-sm font-semibold text-gray-700 mb-2">Search</label>
          <form
            className="flex gap-2"
            onSubmit={(event) => {
              event.preventDefault();
              searchLocation();
            }}
          >
            <input
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Station, cafe, or street address"
              className="min-w-0 flex-1 bg-white border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
            />
            <button
              type="submit"
              disabled={isSearching}
              className="w-12 rounded-lg bg-gray-900 text-white flex items-center justify-center hover:bg-emerald-700 disabled:opacity-50 focus-visible:ring-2 focus-visible:ring-emerald-700"
              aria-label="Search pickup location"
            >
              {isSearching ? <Loader2 size={18} className="animate-spin" /> : <Search size={18} />}
            </button>
          </form>
          <button
            type="button"
            onClick={useCurrentLocation}
            disabled={mapLoadFailed}
            className="mt-3 inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-bold text-gray-700 hover:bg-gray-100 disabled:opacity-50 focus-visible:ring-2 focus-visible:ring-emerald-600"
          >
            <Crosshair size={16} />
            Use My Current Location
          </button>
          {statusMessage && <p className="mt-3 text-xs font-medium text-amber-700">{statusMessage}</p>}
          {searchResults.length > 0 && (
            <div className="mt-3 space-y-2">
              {searchResults.map((result, index) => (
                <button
                  type="button"
                  key={`${result.name}-${result.address}-${index}`}
                  onClick={() => selectSearchResult(result)}
                  className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-left hover:border-emerald-500 hover:bg-emerald-50 focus-visible:ring-2 focus-visible:ring-emerald-600"
                >
                  <span className="block text-sm font-bold text-gray-900">{result.name}</span>
                  <span className="mt-1 block text-xs text-gray-500">{result.address}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {clientId ? (
          <div className="relative h-[420px] rounded-lg overflow-hidden border border-gray-200 bg-gray-100">
            <div ref={mapRef} className="h-full w-full" />
            <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
              <div className="-mt-8 flex flex-col items-center">
                <MapPin size={40} className="text-emerald-700 drop-shadow-md" fill="white" />
                <div className="h-3 w-3 rounded-full bg-emerald-700/30" />
              </div>
            </div>
            {!isMapReady && (
              <div className="absolute inset-0 flex items-center justify-center bg-gray-50 text-sm text-gray-500">
                {mapLoadFailed ? 'Map could not be loaded.' : 'Loading map...'}
              </div>
            )}
            {isMapReady && !mapTilesVisible && (
              <div className="absolute inset-x-4 top-4 rounded-lg border border-amber-200 bg-white/95 px-3 py-2 text-xs font-medium text-amber-700 shadow-sm">
                {MAP_TILE_FAIL_MESSAGE}
              </div>
            )}
          </div>
        ) : (
          <div className="rounded-lg border border-gray-200 bg-gray-50 p-5 text-sm text-gray-600">
            Map is not available yet. Add the Naver Maps Client ID to the frontend environment file and restart Vite.
          </div>
        )}

        <div className="space-y-3 rounded-lg border border-gray-200 bg-white p-4">
          <label className="block text-sm font-semibold text-gray-700">Selected Place Label</label>
          <input
            type="text"
            value={locationName}
            onChange={(event) => setLocationName(event.target.value)}
            placeholder="e.g. Exit 2 bench"
            className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
          />
          <label className="block text-sm font-semibold text-gray-700">Address</label>
          <input
            type="text"
            value={address}
            onChange={(event) => setAddress(event.target.value)}
            placeholder="Pickup address"
            className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
          />
          <p className="text-xs text-gray-500">{latitude || '-'}, {longitude || '-'}</p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">Latitude</label>
              <input
                type="number"
                value={latitude}
                onChange={(event) => setLatitude(event.target.value)}
                placeholder="37.566500"
                className="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">Longitude</label>
              <input
                type="number"
                value={longitude}
                onChange={(event) => setLongitude(event.target.value)}
                placeholder="126.978000"
                className="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
              />
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={confirmLocation}
          disabled={!canConfirm}
          className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-emerald-600 px-4 py-4 text-sm font-bold text-white shadow-lg shadow-emerald-100 hover:bg-emerald-700 disabled:opacity-50 focus-visible:ring-2 focus-visible:ring-emerald-700"
        >
          <Check size={18} />
          Use This Pickup Location
        </button>
      </main>
    </div>
  );
};

export default PickupLocationPicker;
