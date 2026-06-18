import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AlertCircle, ArrowLeft, CheckCircle, Clock, Flag, Heart, MapPin, Share } from 'lucide-react';
import { blocksApi, bookmarksApi, itemsApi, reportsApi, reservationsApi } from '../services';
import { ItemDetailSkeleton } from '../components/Skeletons';
import ImageWithFallback from '../components/ImageWithFallback';
import NaverMap from '../components/NaverMap';
import { ItemResponseDto, ItemStatus, ReportReason } from '../types';

const reportReasonOptions: { value: ReportReason; label: string }[] = [
  { value: 'PROHIBITED_ITEM', label: 'Prohibited item' },
  { value: 'SUSPECTED_FRAUD', label: 'Suspected fraud' },
  { value: 'OFF_PLATFORM_PAYMENT', label: 'Off-platform payment' },
  { value: 'PERSONAL_INFO_OR_CODE_REQUEST', label: 'Personal info or code request' },
  { value: 'HARASSMENT_OR_THREAT', label: 'Harassment or threat' },
  { value: 'COUNTERFEIT_OR_STOLEN_SUSPECTED', label: 'Counterfeit or stolen item' },
  { value: 'OTHER', label: 'Other' },
];

const getErrorStatus = (error: unknown): number | undefined => {
  if (typeof error !== 'object' || error === null || !('status' in error)) {
    return undefined;
  }

  const status = Number((error as { status?: number }).status);
  return Number.isFinite(status) ? status : undefined;
};

const isAuthError = (error: unknown): boolean => {
  const status = getErrorStatus(error);
  return status === 401 || status === 403;
};

const ItemDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const itemPublicId = id?.trim() || '';
  const hasValidItemPublicId = itemPublicId.length > 0;

  const [item, setItem] = useState<ItemResponseDto | null>(null);
  const [hasRequested, setHasRequested] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [showCopyToast, setShowCopyToast] = useState(false);
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportReason, setReportReason] = useState<ReportReason>('PROHIBITED_ITEM');
  const [reportDescription, setReportDescription] = useState('');
  const [reportNotice, setReportNotice] = useState('');
  const [blockNotice, setBlockNotice] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isReportSubmitting, setIsReportSubmitting] = useState(false);
  const [isBlockSubmitting, setIsBlockSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      if (!hasValidItemPublicId) {
        if (cancelled) return;
        setItem(null);
        setHasRequested(false);
        setIsLiked(false);
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setHasRequested(false);
      setIsLiked(false);
      try {
        const [itemResult, bookmarkResult] = await Promise.allSettled([
          itemsApi.getItem(itemPublicId),
          bookmarksApi.getMyBookmarks(),
        ]);

        if (itemResult.status === 'rejected') {
          throw itemResult.reason;
        }

        if (cancelled) return;
        const data = itemResult.value;
        setItem(data);

        if (bookmarkResult.status === 'fulfilled') {
          const liked = bookmarkResult.value.some((bookmark) => bookmark.itemId === data.id);
          setIsLiked(liked);
        } else if (!isAuthError(bookmarkResult.reason)) {
          console.error('Failed to fetch bookmarks', bookmarkResult.reason);
        }
      } catch (error) {
        if (cancelled) return;
        console.error('Failed to fetch item', error);
        setItem(null);
        setHasRequested(false);
        setIsLiked(false);
      } finally {
        if (cancelled) return;
        setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [itemPublicId, hasValidItemPublicId]);

  if (isLoading) {
    return <ItemDetailSkeleton />;
  }

  if (!item) return <div className="p-8 text-center text-gray-500">Item not found</div>;

  const isUnavailable = item.status !== ItemStatus.ON_SALE;
  const reserveButtonLabel = hasRequested
    ? 'Requested'
    : item.status === ItemStatus.RESERVED
      ? 'Reserved'
      : item.status === ItemStatus.SOLD
        ? 'Sold'
        : 'Reserve Now';

  const handleReserveClick = () => {
    if (hasRequested || isUnavailable) return;
    setShowReserveModal(true);
  };

  const handleReportClick = () => {
    setReportNotice('');
    setShowReportModal(true);
  };

  const confirmReservation = async () => {
    if (!item) return;

    setIsSubmitting(true);
    try {
      await reservationsApi.create(item.id);
      setHasRequested(true);
      alert('Reservation request sent to the seller!');
    } catch (error) {
      if (isAuthError(error)) {
        navigate(`/login?next=${encodeURIComponent(`/item/${itemPublicId}`)}`);
        return;
      }
      const message = error instanceof Error ? error.message : 'Failed to send reservation request.';
      alert(message);
    } finally {
      setIsSubmitting(false);
      setShowReserveModal(false);
    }
  };

  const toggleLike = async () => {
    if (!item) return;

    try {
      const result = await bookmarksApi.toggle(item.id);
      setIsLiked(result.toggleOn);
    } catch (error) {
      if (isAuthError(error)) {
        navigate(`/login?next=${encodeURIComponent(`/item/${itemPublicId}`)}`);
        return;
      }
      const message = error instanceof Error ? error.message : 'Failed to update bookmark.';
      alert(message);
    }
  };

  const submitReport = async () => {
    if (!item) return;

    setIsReportSubmitting(true);
    setReportNotice('');
    try {
      await reportsApi.create({
        targetType: 'ITEM',
        targetId: item.id,
        reason: reportReason,
        description: reportDescription.trim() || undefined,
      });
      setReportNotice('Report received. The seller will not see your identity from this report.');
      setReportDescription('');
    } catch (error) {
      if (isAuthError(error)) {
        navigate(`/login?next=${encodeURIComponent(`/item/${itemPublicId}`)}`);
        return;
      }
      const message = error instanceof Error ? error.message : 'Failed to submit report.';
      setReportNotice(message);
    } finally {
      setIsReportSubmitting(false);
    }
  };

  const blockSeller = async () => {
    if (!item?.writerId) return;

    setIsBlockSubmitting(true);
    setBlockNotice('');
    try {
      await blocksApi.block(item.writerId);
      setBlockNotice('Seller blocked. Public pages stay visible, but new reservations, bookmarks, reviews, and notifications are restricted.');
    } catch (error) {
      if (isAuthError(error)) {
        navigate(`/login?next=${encodeURIComponent(`/item/${itemPublicId}`)}`);
        return;
      }
      const message = error instanceof Error ? error.message : 'Failed to block seller.';
      setBlockNotice(message);
    } finally {
      setIsBlockSubmitting(false);
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: item.title,
          text: `Check out ${item.title} from this Knock Market shelf`,
          url: window.location.href,
        });
      } catch (error) {
        const isShareCancelled =
          typeof error === 'object' &&
          error !== null &&
          'name' in error &&
          (error as { name?: unknown }).name === 'AbortError';

        if (isShareCancelled) {
          console.warn('Share was cancelled or unavailable', error);
        } else {
          console.error('Failed to share item', error);
        }
      }
      return;
    }

    try {
      await navigator.clipboard.writeText(window.location.href);
      setShowCopyToast(true);
      setTimeout(() => setShowCopyToast(false), 2000);
    } catch (error) {
      console.error('Failed to copy link:', error);
    }
  };

  const handleBack = () => {
    if (window.history.state && window.history.state.idx > 0) {
      navigate(-1);
      return;
    }
    navigate('/home');
  };

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto relative">
      {showCopyToast && (
        <div className="fixed top-20 left-1/2 transform -translate-x-1/2 bg-gray-900/90 text-white px-6 py-3 rounded-full text-sm font-medium backdrop-blur-sm z-[60] shadow-xl animate-in fade-in zoom-in duration-200 flex items-center space-x-2">
          <CheckCircle size={16} className="text-emerald-400" />
          <span>Link copied to clipboard</span>
        </div>
      )}

      {showReserveModal && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center px-6">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setShowReserveModal(false)}
          ></div>
          <div className="bg-white w-full max-w-sm rounded-lg p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex flex-col items-center text-center">
              <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center mb-4 text-emerald-700">
                <AlertCircle size={24} strokeWidth={2.5} />
              </div>
              <h2 className="text-xl font-bold text-gray-900 mb-2">Request Reservation?</h2>
              <p className="text-sm text-gray-500 mb-6 leading-relaxed">
                This will notify the seller that you are interested.
              </p>
              <div className="flex space-x-3 w-full">
                <button
                  onClick={() => setShowReserveModal(false)}
                  className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-lg hover:bg-gray-200 transition-colors focus-visible:ring-2 focus-visible:ring-gray-500"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmReservation}
                  disabled={isSubmitting}
                  className="flex-1 py-3 bg-emerald-600 text-white font-bold rounded-lg hover:bg-emerald-700 shadow-lg shadow-emerald-200 transition-colors disabled:opacity-60 focus-visible:ring-2 focus-visible:ring-emerald-700"
                >
                  {isSubmitting ? 'Submitting...' : 'Confirm'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {showReportModal && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center px-6">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setShowReportModal(false)}
          ></div>
          <div className="bg-white w-full max-w-sm rounded-lg p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex items-start space-x-3 mb-5">
              <div className="w-11 h-11 bg-red-50 rounded-lg flex items-center justify-center text-red-600">
                <Flag size={22} strokeWidth={2.5} />
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-900">Report this item</h2>
                <p className="mt-1 text-xs text-gray-500 leading-relaxed">
                  Reports are private and help Knock review prohibited items, fraud, and unsafe behavior.
                </p>
              </div>
            </div>

            <label className="block text-sm font-bold text-gray-900 mb-2" htmlFor="report-reason">
              Reason
            </label>
            <select
              id="report-reason"
              value={reportReason}
              onChange={(event) => setReportReason(event.target.value as ReportReason)}
              className="w-full rounded-lg border border-gray-200 px-3 py-3 text-sm focus:border-red-500 focus:ring-2 focus:ring-red-100 outline-none"
            >
              {reportReasonOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>

            <label className="block text-sm font-bold text-gray-900 mt-4 mb-2" htmlFor="report-description">
              Details <span className="font-normal text-gray-400">(optional)</span>
            </label>
            <textarea
              id="report-description"
              value={reportDescription}
              onChange={(event) => setReportDescription(event.target.value)}
              maxLength={1000}
              rows={4}
              className="w-full resize-none rounded-lg border border-gray-200 px-3 py-3 text-sm focus:border-red-500 focus:ring-2 focus:ring-red-100 outline-none"
              placeholder="Add helpful context without sharing passwords, codes, or payment details."
            />

            {reportNotice && (
              <p className="mt-3 rounded-lg bg-gray-50 px-3 py-2 text-xs font-semibold text-gray-700">
                {reportNotice}
              </p>
            )}
            {blockNotice && (
              <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-xs font-semibold text-red-700">
                {blockNotice}
              </p>
            )}

            <div className="mt-5 space-y-3">
              {item.writerId && (
                <button
                  onClick={blockSeller}
                  disabled={isBlockSubmitting}
                  className="w-full py-3 border border-red-200 text-red-700 font-bold rounded-lg hover:bg-red-50 transition-colors disabled:opacity-60 focus-visible:ring-2 focus-visible:ring-red-700"
                >
                  {isBlockSubmitting ? 'Blocking...' : 'Block seller'}
                </button>
              )}
              <div className="flex space-x-3">
              <button
                onClick={() => setShowReportModal(false)}
                className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-lg hover:bg-gray-200 transition-colors focus-visible:ring-2 focus-visible:ring-gray-500"
              >
                Close
              </button>
              <button
                onClick={submitReport}
                disabled={isReportSubmitting}
                className="flex-1 py-3 bg-red-600 text-white font-bold rounded-lg hover:bg-red-700 transition-colors disabled:opacity-60 focus-visible:ring-2 focus-visible:ring-red-700"
              >
                {isReportSubmitting ? 'Submitting...' : 'Submit'}
              </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="relative h-80 bg-gray-100">
        <ImageWithFallback src={item.imageUrls?.[0]} alt={item.title} className="w-full h-full object-cover" />
        <div className="absolute top-0 left-0 right-0 p-4 flex justify-between items-start bg-gradient-to-b from-black/30 to-transparent z-20">
          <button
            onClick={handleBack}
            className="p-2 bg-white/80 backdrop-blur-md text-gray-900 rounded-lg hover:bg-white transition-colors focus-visible:ring-2 focus-visible:ring-white"
            aria-label="Go back"
          >
            <ArrowLeft size={24} />
          </button>
          <button
            onClick={handleShare}
            className="p-2 bg-white/80 backdrop-blur-md text-gray-900 rounded-lg hover:bg-white transition-colors focus-visible:ring-2 focus-visible:ring-white"
            aria-label="Share item"
          >
            <Share size={24} />
          </button>
        </div>
      </div>

      <div className="px-6 py-6 rounded-t-lg -mt-6 bg-white relative z-10">
        <div className="flex justify-between items-start mb-2">
          <h1 className="text-2xl font-bold text-gray-900 leading-tight w-3/4">{item.title}</h1>
          <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-3 py-1 rounded-lg">{item.type}</span>
        </div>

        <div className="flex items-center space-x-2 mb-6">
          <span className="text-sm text-gray-500">Like New</span>
          <span className="text-gray-300">•</span>
          <span className="text-sm text-gray-500">{item.writerNickname || 'Seller shelf'}</span>
        </div>

        <p className="text-gray-600 leading-relaxed mb-8">{item.description}</p>

        <div className="bg-gray-50 p-4 rounded-lg mb-8 border border-gray-100">
          <div className="flex items-start space-x-3">
            <MapPin size={20} className="text-emerald-700 mt-0.5" />
            <div className="flex-1 min-w-0">
              <NaverMap
                latitude={item.tradeLatitude}
                longitude={item.tradeLongitude}
                name={item.tradeLocationName}
                address={item.tradeLocationAddress}
              />
            </div>
          </div>
        </div>

        <div className="border-t border-gray-100 pt-6">
          <button
            type="button"
            onClick={() => item.writerId && navigate(`/seller/${item.writerId}`)}
            className="w-full flex items-center justify-between p-2 -mx-2 rounded-lg hover:bg-gray-50 transition-colors text-left focus-visible:ring-2 focus-visible:ring-emerald-600"
          >
            <div className="flex items-center space-x-3">
              <ImageWithFallback
                src={item.writerProfileImageUrl}
                alt="Seller"
                className="w-12 h-12 rounded-lg border border-gray-100 object-cover"
              />
              <div>
                <p className="font-bold text-gray-900">{item.writerNickname || `Seller #${item.writerId}`}</p>
                <p className="text-xs text-gray-500">Seller</p>
              </div>
            </div>
          </button>
          <button
            type="button"
            onClick={handleReportClick}
            className="mt-4 inline-flex items-center space-x-2 text-sm font-semibold text-red-600 hover:text-red-700 focus-visible:ring-2 focus-visible:ring-red-500 rounded-md"
          >
            <Flag size={16} />
            <span>Report item or block seller</span>
          </button>
        </div>
      </div>

      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-4 pb-8 flex items-center space-x-4 max-w-md mx-auto z-50">
        <button
          onClick={toggleLike}
          className={`p-3 rounded-lg border transition-colors focus-visible:ring-2 focus-visible:ring-red-500 ${isLiked ? 'bg-red-50 border-red-100 text-red-600' : 'border-gray-200 text-gray-500 hover:bg-gray-50'}`}
          aria-label={isLiked ? 'Remove bookmark' : 'Add bookmark'}
        >
          <Heart size={24} fill={isLiked ? 'currentColor' : 'none'} />
        </button>
        <button
          onClick={handleReserveClick}
          disabled={hasRequested || isUnavailable}
          className={`flex-1 py-4 rounded-lg font-bold text-white flex items-center justify-center space-x-2 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-700 focus-visible:ring-offset-2 ${hasRequested || isUnavailable
            ? 'bg-gray-400 cursor-not-allowed'
            : 'bg-emerald-600 hover:bg-emerald-700 shadow-lg shadow-emerald-200'}`}
        >
          {hasRequested || isUnavailable ? (
            <span>{reserveButtonLabel}</span>
          ) : (
            <>
              <Clock size={20} />
              <span>{reserveButtonLabel}</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default ItemDetail;
