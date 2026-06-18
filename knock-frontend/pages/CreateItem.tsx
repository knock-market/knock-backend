import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AlertTriangle, ArrowLeft, Camera, Loader2, ShieldCheck, X } from 'lucide-react';
import { imagesApi, itemPolicyApi, itemsApi } from '../services';
import TradeLocationFields from '../components/TradeLocationFields';
import type { ItemPolicyWarningResponseDto } from '../types';

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
const POLICY_PREFLIGHT_FALLBACK_MESSAGE =
  'We could not verify the latest item policy right now. Please avoid prohibited or unsafe items and continue only if your listing follows marketplace policy.';

type ItemCreatePayload = Parameters<typeof itemsApi.createItem>[0];

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
  const [formError, setFormError] = useState('');
  const [policyWarning, setPolicyWarning] = useState<ItemPolicyWarningResponseDto | null>(null);
  const [policyPreflightFallback, setPolicyPreflightFallback] = useState('');
  const [policyAcknowledged, setPolicyAcknowledged] = useState(false);
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

  const resetPolicyAcknowledgement = () => {
    if (policyAcknowledged || policyWarning || policyPreflightFallback) {
      setPolicyAcknowledged(false);
      setPolicyWarning(null);
      setPolicyPreflightFallback('');
    }
  };

  const submitItem = async (payload: ItemCreatePayload) => {
    await itemsApi.createItem(payload);
    setSubmitNotice(POST_SUCCESS_MESSAGE);
    redirectTimeoutRef.current = window.setTimeout(() => navigate('/home'), POST_SUCCESS_REDIRECT_DELAY_MS);
  };

  const handleSubmit = async (skipPolicyPreflight = false) => {
    setSubmitNotice('');
    setFormError('');
    setPolicyWarning(null);
    setPolicyPreflightFallback('');

    if (!title.trim() || !description.trim()) {
      setFormError('Please fill in item name and description.');
      return;
    }

    const parsedPrice = Number(price);
    if (transactionType === 'sale') {
      const isInvalidSalePrice = !price.trim() || !Number.isFinite(parsedPrice) || !Number.isInteger(parsedPrice) || parsedPrice <= 0;
      if (isInvalidSalePrice) {
        setFormError('Please enter a valid sale price greater than 0.');
        return;
      }
    }

    const parsedLatitude = latitude.trim() ? Number(latitude) : undefined;
    const parsedLongitude = longitude.trim() ? Number(longitude) : undefined;
    const hasNonNumericCoordinate = (parsedLatitude !== undefined && !Number.isFinite(parsedLatitude))
      || (parsedLongitude !== undefined && !Number.isFinite(parsedLongitude));
    if (hasNonNumericCoordinate) {
      setFormError('Please enter a valid latitude and longitude pair.');
      return;
    }
    const hasSelectedPickupLocation = locationName.trim() && locationAddress.trim()
      && parsedLatitude !== undefined && parsedLongitude !== undefined;
    if (!hasSelectedPickupLocation) {
      setFormError('Please pick an exact pickup location on the map before posting.');
      return;
    }
    const hasOneCoordinate = (parsedLatitude === undefined) !== (parsedLongitude === undefined);
    const hasInvalidCoordinates = (parsedLatitude !== undefined && (parsedLatitude < -90 || parsedLatitude > 90))
      || (parsedLongitude !== undefined && (parsedLongitude < -180 || parsedLongitude > 180));
    if (hasOneCoordinate || hasInvalidCoordinates) {
      setFormError('Please enter a valid latitude and longitude pair.');
      return;
    }

    const payload: ItemCreatePayload = {
      title: title.trim(),
      description: description.trim(),
      itemType: transactionType === 'sale' ? 'SELL' : 'GIVE',
      price: transactionType === 'sale' ? parsedPrice : 0,
      imageUrls,
      tradeLocationName: locationName.trim() || undefined,
      tradeLocationAddress: locationAddress.trim() || undefined,
      tradeLatitude: parsedLatitude,
      tradeLongitude: parsedLongitude,
    };

    setIsSubmitting(true);
    try {
      if (!skipPolicyPreflight && !policyAcknowledged) {
        try {
          const warning = await itemPolicyApi.getWarnings({
            title: payload.title,
            description: payload.description,
            itemType: payload.itemType,
          });

          const hasWarning = warning.severity === 'WARNING' || warning.warningCategories.length > 0;
          if (hasWarning) {
            setPolicyWarning(warning);
            setIsSubmitting(false);
            return;
          }
        } catch {
          setPolicyPreflightFallback(POLICY_PREFLIGHT_FALLBACK_MESSAGE);
          setIsSubmitting(false);
          return;
        }
      }

      await submitItem(payload);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to post item.';
      setFormError(message);
      setIsSubmitting(false);
    }
  };

  const isPostButtonDisabled = isSubmitting || isUploading || Boolean(submitNotice);
  const postButtonLabel = submitNotice ? 'Returning...' : isSubmitting ? 'Posting...' : isUploading ? 'Uploading photos...' : 'Post Item';
  const policyDialogOpen = Boolean(policyWarning || policyPreflightFallback);
  const policyDialogTitle = policyWarning ? 'Check item policy before posting' : 'Policy check unavailable';
  const policyDialogMessage = policyWarning?.message || policyPreflightFallback;
  const policyDialogCategories = policyWarning?.warningCategories || [];
  const policyDialogPolicyUrl = policyWarning?.policyUrl;
  const policyDialogPolicyVersion = policyWarning?.policyVersion;
  const continueAfterPolicyDialog = () => {
    setPolicyAcknowledged(true);
    setPolicyWarning(null);
    setPolicyPreflightFallback('');
    void handleSubmit(true);
  };

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
              onChange={(e) => {
                setTitle(e.target.value);
                resetPolicyAcknowledgement();
              }}
              placeholder="What are you selling?"
              className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Description</label>
            <textarea
              value={description}
              onChange={(e) => {
                setDescription(e.target.value);
                resetPolicyAcknowledgement();
              }}
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
              onClick={() => {
                setTransactionType('free');
                resetPolicyAcknowledgement();
              }}
              className={`py-3 rounded-lg text-sm font-bold transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${transactionType === 'free' ? 'bg-emerald-600 text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              Free
            </button>
            <button
              type="button"
              onClick={() => {
                setTransactionType('sale');
                setPrice('1000');
                resetPolicyAcknowledgement();
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
          {formError && (
            <p role="alert" className="mb-3 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
              {formError}
            </p>
          )}
          {submitNotice && (
            <p role="status" className="mb-3 rounded-lg border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700">
              {submitNotice}
            </p>
          )}
          <button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={isPostButtonDisabled}
            className={`w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-4 rounded-lg shadow-lg shadow-emerald-200 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-700 focus-visible:ring-offset-2 ${isPostButtonDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {postButtonLabel}
          </button>
        </div>
      </div>

      {policyDialogOpen && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="item-policy-warning-title"
          className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 px-4 py-6 sm:items-center"
        >
          <div className="w-full max-w-md rounded-2xl bg-white p-5 shadow-2xl">
            <div className="flex items-start gap-3">
              <div className="rounded-full bg-amber-100 p-2 text-amber-700">
                {policyWarning ? <AlertTriangle size={22} /> : <ShieldCheck size={22} />}
              </div>
              <div className="flex-1">
                <h2 id="item-policy-warning-title" className="text-base font-bold text-gray-900">
                  {policyDialogTitle}
                </h2>
                <p className="mt-2 text-sm leading-6 text-gray-700">{policyDialogMessage}</p>
                {policyDialogCategories.length > 0 && (
                  <div className="mt-3">
                    <p className="text-xs font-bold uppercase tracking-wide text-gray-500">Warning categories</p>
                    <ul className="mt-2 flex flex-wrap gap-2">
                      {policyDialogCategories.map((category) => (
                        <li key={category} className="rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-800">
                          {category}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
                {policyDialogPolicyVersion && (
                  <p className="mt-3 text-xs text-gray-500">Checked against policy version {policyDialogPolicyVersion}.</p>
                )}
                {policyDialogPolicyUrl && (
                  <a
                    href={policyDialogPolicyUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="mt-3 inline-flex text-sm font-bold text-emerald-700 underline decoration-emerald-200 underline-offset-4"
                  >
                    Read marketplace item policy
                  </a>
                )}
              </div>
            </div>
            <div className="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-2">
              <button
                type="button"
                onClick={() => {
                  setPolicyWarning(null);
                  setPolicyPreflightFallback('');
                  setIsSubmitting(false);
                }}
                className="rounded-lg border border-gray-200 px-4 py-3 text-sm font-bold text-gray-700 hover:bg-gray-50 focus-visible:ring-2 focus-visible:ring-emerald-600"
              >
                Review Listing
              </button>
              <button
                type="button"
                onClick={continueAfterPolicyDialog}
                className="rounded-lg bg-emerald-600 px-4 py-3 text-sm font-bold text-white shadow-lg shadow-emerald-100 hover:bg-emerald-700 focus-visible:ring-2 focus-visible:ring-emerald-700 focus-visible:ring-offset-2"
              >
                Continue and Post
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CreateItem;
