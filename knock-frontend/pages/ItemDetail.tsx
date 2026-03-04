import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AlertCircle, ArrowLeft, CheckCircle, Clock, Heart, MapPin, Share } from 'lucide-react';
import { bookmarksApi, itemsApi, reservationsApi } from '../services';
import { ItemDetailSkeleton } from '../components/Skeletons';
import ImageWithFallback from '../components/ImageWithFallback';
import { ItemCategory, ItemResponseDto, ItemStatus } from '../types';
import { CATEGORY_LABELS } from '../constants';

const ItemDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const itemId = id ? Number(id) : NaN;
  const hasValidItemId = Number.isInteger(itemId) && itemId > 0;

  const [item, setItem] = useState<ItemResponseDto | null>(null);
  const [hasRequested, setHasRequested] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [showCopyToast, setShowCopyToast] = useState(false);
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!hasValidItemId) {
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
          itemsApi.getItem(itemId),
          bookmarksApi.getMyBookmarks(),
        ]);

        if (itemResult.status === 'rejected') {
          throw itemResult.reason;
        }

        const data = itemResult.value;
        setItem(data);

        if (bookmarkResult.status === 'fulfilled') {
          const liked = bookmarkResult.value.some((bookmark) => bookmark.itemId === data.id);
          setIsLiked(liked);
        } else {
          console.error('Failed to fetch bookmarks', bookmarkResult.reason);
        }
      } catch (error) {
        console.error('Failed to fetch item', error);
        setItem(null);
        setHasRequested(false);
        setIsLiked(false);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, [itemId, hasValidItemId]);

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

  const confirmReservation = async () => {
    if (!hasValidItemId) return;

    setIsSubmitting(true);
    try {
      await reservationsApi.create(itemId);
      setHasRequested(true);
      alert('Reservation request sent to the seller!');
    } catch (error) {
      const status = typeof error === 'object' && error !== null && 'status' in error
        ? Number((error as { status?: number }).status)
        : undefined;
      if (status === 401) {
        navigate(`/login?next=${encodeURIComponent(`/item/${itemId}`)}`);
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
    if (!hasValidItemId) return;

    try {
      const result = await bookmarksApi.toggle(itemId);
      setIsLiked(result.toggleOn);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to update bookmark.';
      alert(message);
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: item.title,
          text: `Check out ${item.title} on Knock Market`,
          url: window.location.href,
        });
      } catch (error) {
        console.log('Error sharing:', error);
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
          <div className="bg-white w-full max-w-sm rounded-3xl p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex flex-col items-center text-center">
              <div className="w-12 h-12 bg-emerald-100 rounded-full flex items-center justify-center mb-4 text-emerald-600">
                <AlertCircle size={24} strokeWidth={2.5} />
              </div>
              <h2 className="text-xl font-bold text-gray-900 mb-2">Request Reservation?</h2>
              <p className="text-sm text-gray-500 mb-6 leading-relaxed">
                This will notify the seller that you are interested.
              </p>
              <div className="flex space-x-3 w-full">
                <button
                  onClick={() => setShowReserveModal(false)}
                  className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-xl hover:bg-gray-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmReservation}
                  disabled={isSubmitting}
                  className="flex-1 py-3 bg-emerald-500 text-white font-bold rounded-xl hover:bg-emerald-600 shadow-lg shadow-emerald-200 transition-colors disabled:opacity-60"
                >
                  {isSubmitting ? 'Submitting...' : 'Confirm'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="relative h-80 bg-gray-100">
        <ImageWithFallback src={item.imageUrls?.[0]} alt={item.title} className="w-full h-full object-cover" />
        <div className="absolute top-0 left-0 right-0 p-4 flex justify-between items-start bg-gradient-to-b from-black/30 to-transparent z-20">
          <button onClick={handleBack} className="p-2 bg-white/20 backdrop-blur-md text-white rounded-full hover:bg-white/30 transition-colors">
            <ArrowLeft size={24} />
          </button>
          <button
            onClick={handleShare}
            className="p-2 bg-white/20 backdrop-blur-md text-white rounded-full hover:bg-white/30 transition-colors"
            aria-label="Share item"
          >
            <Share size={24} />
          </button>
        </div>
      </div>

      <div className="px-6 py-6 rounded-t-3xl -mt-6 bg-white relative z-10">
        <div className="flex justify-between items-start mb-2">
          <h1 className="text-2xl font-bold text-gray-900 leading-tight w-3/4">{item.title}</h1>
          <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-3 py-1 rounded-full">{item.type}</span>
        </div>

        <div className="flex items-center space-x-2 mb-6">
          <span className="text-sm text-gray-500">Like New</span>
          <span className="text-gray-300">•</span>
          <span className="text-sm text-gray-500">{CATEGORY_LABELS[item.category as ItemCategory] || item.category}</span>
        </div>

        <p className="text-gray-600 leading-relaxed mb-8">{item.description}</p>

        <div className="bg-gray-50 p-4 rounded-xl flex items-start space-x-3 mb-8">
          <MapPin size={20} className="text-gray-400 mt-0.5" />
          <div>
            <p className="text-sm font-semibold text-gray-900">Pickup by arrangement</p>
            <p className="text-xs text-gray-500 mt-1">Coordinate details after reservation approval.</p>
          </div>
        </div>

        <div className="border-t border-gray-100 pt-6">
          <div className="flex items-center justify-between p-2 -mx-2 rounded-xl">
            <div className="flex items-center space-x-3">
              <ImageWithFallback
                src={item.writerProfileImageUrl}
                alt="Seller"
                className="w-12 h-12 rounded-full border border-gray-100"
              />
              <div>
                <p className="font-bold text-gray-900">{item.writerNickname || `Seller #${item.writerId}`}</p>
                <p className="text-xs text-gray-500">Member</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-4 pb-8 flex items-center space-x-4 max-w-md mx-auto z-50">
        <button
          onClick={toggleLike}
          className={`p-3 rounded-full border transition-colors ${isLiked ? 'bg-red-50 border-red-100 text-red-500' : 'border-gray-200 text-gray-400 hover:bg-gray-50'}`}
        >
          <Heart size={24} fill={isLiked ? 'currentColor' : 'none'} />
        </button>
        <button
          onClick={handleReserveClick}
          disabled={hasRequested || isUnavailable}
          className={`flex-1 py-4 rounded-xl font-bold text-white flex items-center justify-center space-x-2 transition-all active:scale-[0.98] ${hasRequested || isUnavailable
            ? 'bg-gray-400 cursor-not-allowed'
            : 'bg-emerald-500 hover:bg-emerald-600 shadow-lg shadow-emerald-200'}`}
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
