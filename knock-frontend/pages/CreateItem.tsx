import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, Camera, Loader2, X } from 'lucide-react';
import { imagesApi, itemsApi } from '../services';
import TradeLocationFields from '../components/TradeLocationFields';

type CreateItemDraft = {
  transactionType?: 'free' | 'sale';
  price?: string;
  title?: string;
  description?: string;
  imageUrls?: string[];
};

type CreateItemLocationState = {
  draft?: CreateItemDraft;
  location?: {
    locationName?: string;
    address?: string;
    latitude?: string;
    longitude?: string;
  };
};

const POST_SUCCESS_REDIRECT_DELAY_MS = 900;
const POST_SUCCESS_MESSAGE = 'Item posted. Returning to the previous page...';

const CreateItem: React.FC = () => {
  const navigate = useNavigate();
  const routeLocation = useLocation();
  const routeState = (routeLocation.state || {}) as CreateItemLocationState;
  const draft = routeState.draft || {};
  const selectedLocation = routeState.location || {};
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [transactionType, setTransactionType] = useState<'free' | 'sale'>(draft.transactionType || 'free');
  const [price, setPrice] = useState(draft.price || '1000');
  const [title, setTitle] = useState(draft.title || '');
  const [description, setDescription] = useState(draft.description || '');
  const [imageUrls, setImageUrls] = useState<string[]>(draft.imageUrls || []);
  const [locationName, setLocationName] = useState(selectedLocation.locationName || '');
  const [locationAddress, setLocationAddress] = useState(selectedLocation.address || '');
  const [latitude, setLatitude] = useState(selectedLocation.latitude || '');
  const [longitude, setLongitude] = useState(selectedLocation.longitude || '');
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitNotice, setSubmitNotice] = useState('');
  const redirectTimeoutRef = useRef<number | null>(null);

  useEffect(() => () => {
    if (redirectTimeoutRef.current) {
      window.clearTimeout(redirectTimeoutRef.current);
    }
  }, []);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setIsUploading(true);
    try {
      const selectedFiles = Array.from(files as FileList);
      const uploadPromises = selectedFiles.map((file: File) => imagesApi.upload(file));
      const uploaded = await Promise.all(uploadPromises);
      const newUrls = uploaded.map((file) => file.imageUrl);
      setImageUrls((prev) => [...prev, ...newUrls].slice(0, 5));
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to upload image.';
      alert(message);
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };
  const removeImage = (url: string) => {
    setImageUrls((prev) => prev.filter((value) => value !== url));
  };

  const openLocationPicker = () => {
    navigate('/create/location', {
      state: {
        draft: { transactionType, price, title, description, imageUrls },
        location: {
          locationName,
          address: locationAddress,
          latitude,
          longitude,
        },
      },
    });
  };

  const handleSubmit = async () => {
    setSubmitNotice('');

    if (!title.trim() || !description.trim()) {
      alert('Please fill in required fields.');
      return;
    }

    const parsedPrice = Number(price);
    if (transactionType === 'sale') {
      const isInvalidSalePrice = !price.trim() || !Number.isFinite(parsedPrice) || !Number.isInteger(parsedPrice) || parsedPrice <= 0;
      if (isInvalidSalePrice) {
        alert('Please enter a valid sale price greater than 0.');
        return;
      }
    }

    const parsedLatitude = latitude.trim() ? Number(latitude) : undefined;
    const parsedLongitude = longitude.trim() ? Number(longitude) : undefined;
    const hasOneCoordinate = (parsedLatitude === undefined) !== (parsedLongitude === undefined);
    const hasInvalidCoordinates = (parsedLatitude !== undefined && (parsedLatitude < -90 || parsedLatitude > 90))
      || (parsedLongitude !== undefined && (parsedLongitude < -180 || parsedLongitude > 180));
    if (hasOneCoordinate || hasInvalidCoordinates) {
      alert('Please enter a valid latitude and longitude pair.');
      return;
    }
    setIsSubmitting(true);
    try {
      await itemsApi.createItem({
        title: title.trim(),
        description: description.trim(),
        category: 'ETC',
        itemType: transactionType === 'sale' ? 'SELL' : 'GIVE',
        price: transactionType === 'sale' ? parsedPrice : 0,
        imageUrls,
        tradeLocationName: locationName.trim() || undefined,
        tradeLocationAddress: locationAddress.trim() || undefined,
        tradeLatitude: parsedLatitude,
        tradeLongitude: parsedLongitude,
      });
      setSubmitNotice(POST_SUCCESS_MESSAGE);
      redirectTimeoutRef.current = window.setTimeout(() => navigate('/home'), POST_SUCCESS_REDIRECT_DELAY_MS);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to post item.';
      alert(message);
      setIsSubmitting(false);
    }
  };

  const isPostButtonDisabled = isSubmitting || isUploading || Boolean(submitNotice);
  const postButtonLabel = submitNotice ? 'Returning...' : isSubmitting ? 'Posting...' : isUploading ? 'Uploading photos...' : 'Post Item';

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button
          onClick={() => navigate(-1)}
          className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
          aria-label="Go back"
        >
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Register Item</h1>
      </div>

      <div className="p-6 space-y-8">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">Photos ({imageUrls.length}/5)</label>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            multiple
            accept="image/*"
            className="hidden"
          />
          <div className="flex space-x-3 overflow-x-auto no-scrollbar py-2">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading || imageUrls.length >= 5}
              className={`flex-shrink-0 w-20 h-20 rounded-lg border-2 border-dashed flex flex-col items-center justify-center transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${isUploading ? 'bg-gray-50 border-gray-200 text-gray-400' : 'border-emerald-300 text-emerald-700 bg-emerald-50 hover:bg-emerald-100'}`}
              aria-label="Add item photos"
            >
              {isUploading ? <Loader2 size={24} className="animate-spin" /> : <Camera size={24} />}
              <span className="text-[10px] font-bold mt-1">{isUploading ? 'Uploading' : 'Add'}</span>
            </button>

            {imageUrls.map((url, idx) => (
              <div key={idx} className="relative flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden shadow-sm border border-gray-100">
                <img src={url} alt={`Item preview ${idx + 1}`} className="w-full h-full object-cover" loading="lazy" decoding="async" />
                <button
                  type="button"
                  onClick={() => removeImage(url)}
                  className="absolute top-1 right-1 bg-black/60 text-white p-1 rounded-lg hover:bg-black/80 transition-colors focus-visible:ring-2 focus-visible:ring-white"
                  aria-label={`Remove item preview ${idx + 1}`}
                >
                  <X size={12} />
                </button>
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Item Name</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="What are you selling?"
              className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
              placeholder="Describe the item and pickup instructions."
              className="w-full bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
            ></textarea>
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">Transaction Type</label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setTransactionType('free')}
              className={`py-3 rounded-lg text-sm font-bold transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${transactionType === 'free' ? 'bg-emerald-600 text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              Free
            </button>
            <button
              type="button"
              onClick={() => {
                setTransactionType('sale');
                setPrice('1000');
              }}
              className={`py-3 rounded-lg text-sm font-bold transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${transactionType === 'sale' ? 'bg-emerald-600 text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              Sale
            </button>
          </div>

          {transactionType === 'sale' && (
            <div className="mt-4 animate-in fade-in slide-in-from-top-1 duration-200">
              <label className="block text-sm font-semibold text-gray-700 mb-2">Price (₩)</label>
              <input
                type="number"
                min="1"
                step="1"
                inputMode="numeric"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                placeholder="1000"
                className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900 font-medium"
              />
            </div>
          )}
        </div>

        <TradeLocationFields
          locationName={locationName}
          address={locationAddress}
          latitude={latitude}
          longitude={longitude}
          onLocationNameChange={setLocationName}
          onAddressChange={setLocationAddress}
          onOpenPicker={openLocationPicker}
        />

        <div className="pt-4">
          {submitNotice && (
            <p role="status" className="mb-3 rounded-lg border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700">
              {submitNotice}
            </p>
          )}
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isPostButtonDisabled}
            className={`w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-4 rounded-lg shadow-lg shadow-emerald-200 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-700 focus-visible:ring-offset-2 ${isPostButtonDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {postButtonLabel}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateItem;
