import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { itemsApi, authApi, reservationsApi, bookmarksApi } from '../services';
import { ItemDetailSkeleton } from '../components/Skeletons';
import { CURRENT_USER, CATEGORY_LABELS } from '../constants';
import { ItemStatus, ItemResponseDto, ItemCategory } from '../types';
import { AlertCircle, ArrowLeft, CheckCircle, Clock, Heart, MapPin, Share, Loader2 } from 'lucide-react';
import ImageWithFallback from '../components/ImageWithFallback';

const ItemDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [item, setItem] = useState<ItemResponseDto | null>(null);
  const [isReserved, setIsReserved] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [showCopyToast, setShowCopyToast] = useState(false);
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    setIsLoading(true);
    itemsApi.getItem(id)
      .then((response: any) => {
        const data = response.data;
        setItem(data);
        setIsReserved(data.status === ItemStatus.RESERVED);
      })
      .catch((err) => {
        console.error("Failed to fetch item", err);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [id]);

  if (isLoading) {
    return <ItemDetailSkeleton />;
  }

  if (!item) return <div className="p-8 text-center text-gray-500">Item not found</div>;

  const handleReserveClick = () => {
    if (isReserved) return;
    setShowReserveModal(true);
  };

  const confirmReservation = async () => {
    setIsSubmitting(true);
    try {
      await reservationsApi.create(Number(id));
      setIsReserved(true);
      alert("Reservation request sent to the seller!");
    } catch (err) {
      console.error("Reservation failed:", err);
      alert("Failed to send reservation request.");
    } finally {
      setIsSubmitting(false);
      setShowReserveModal(false);
    }
  };

  const toggleLike = async () => {
    try {
      // bookmarksApi.toggle(Number(id)); // Assuming toggle exists
      setIsLiked(!isLiked);
    } catch (err) {
      console.error("Like toggle failed:", err);
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
    } else {
      // Fallback for browsers that don't support Web Share API
      try {
        await navigator.clipboard.writeText(window.location.href);
        setShowCopyToast(true);
        setTimeout(() => setShowCopyToast(false), 2000);
      } catch (err) {
        console.error('Failed to copy link:', err);
      }
    }
  };

  const handleBack = () => {
    if (window.history.state && window.history.state.idx > 0) {
      navigate(-1);
    } else {
      navigate('/home');
    }
  };

  const handleProfileClick = () => {
    navigate('/profile');
  };

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto relative">
      {/* Toast Notification */}
      {showCopyToast && (
        <div className="fixed top-20 left-1/2 transform -translate-x-1/2 bg-gray-900/90 text-white px-6 py-3 rounded-full text-sm font-medium backdrop-blur-sm z-[60] shadow-xl animate-in fade-in zoom-in duration-200 flex items-center space-x-2">
          <CheckCircle size={16} className="text-emerald-400" />
          <span>Link copied to clipboard</span>
        </div>
      )}

      {/* Reservation Confirmation Modal */}
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
                This will notify the seller that you are interested. They will review and confirm your request.
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
                  className="flex-1 py-3 bg-emerald-500 text-white font-bold rounded-xl hover:bg-emerald-600 shadow-lg shadow-emerald-200 transition-colors"
                >
                  Confirm
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Sticky Header Image */}
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
        {/* Dots for slideshow placeholder */}
        <div className="absolute bottom-4 left-0 right-0 flex justify-center space-x-2 z-10">
          <div className="w-2 h-2 bg-white rounded-full"></div>
          <div className="w-2 h-2 bg-white/50 rounded-full"></div>
          <div className="w-2 h-2 bg-white/50 rounded-full"></div>
        </div>
      </div>

      <div className="px-6 py-6 rounded-t-3xl -mt-6 bg-white relative z-10">
        {/* Title & Badge */}
        <div className="flex justify-between items-start mb-2">
          <h1 className="text-2xl font-bold text-gray-900 leading-tight w-3/4">{item.title}</h1>
          <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-3 py-1 rounded-full">
            {item.type}
          </span>
        </div>

        <div className="flex items-center space-x-2 mb-6">
          <span className="text-sm text-gray-500">Like New</span>
          <span className="text-gray-300">•</span>
          <span className="text-sm text-gray-500">{CATEGORY_LABELS[item.category as ItemCategory] || item.category}</span>
        </div>

        {/* Description */}
        <p className="text-gray-600 leading-relaxed mb-8">
          {item.description}
        </p>

        {/* Location Info Mock */}
        <div className="bg-gray-50 p-4 rounded-xl flex items-start space-x-3 mb-8">
          <MapPin size={20} className="text-gray-400 mt-0.5" />
          <div>
            <p className="text-sm font-semibold text-gray-900">North Campus Library</p>
            <p className="text-xs text-gray-500 mt-1">Pick up available until 5 PM today.</p>
          </div>
        </div>

        {/* Seller Profile */}
        <div className="border-t border-gray-100 pt-6">
          <div
            onClick={handleProfileClick}
            className="flex items-center justify-between cursor-pointer hover:bg-gray-50 p-2 -mx-2 rounded-xl transition-colors"
          >
            <div className="flex items-center space-x-3">
              <ImageWithFallback src={item.writerProfileImageUrl || 'https://ui-avatars.com/api/?name=?&background=e2e8f0&color=94a3b8'} alt="Seller" className="w-12 h-12 rounded-full border border-gray-100" />
              <div>
                <p className="font-bold text-gray-900">{item.writerNickname || `Seller #${item.writerId}`}</p>
                <p className="text-xs text-gray-500">Member</p>
              </div>
            </div>
            <div className="flex flex-col items-end">
              <span className="text-[10px] text-gray-400 mt-1">Recently active</span>
            </div>
          </div>
        </div>

        {/* Q&A Section Placeholder */}
        <div className="mt-8 pt-6 border-t border-gray-100">
          <div className="flex items-center bg-gray-50 rounded-full px-4 py-2">
            <input
              type="text"
              placeholder="Ask a question..."
              className="bg-transparent flex-1 text-sm text-gray-900 focus:outline-none placeholder-gray-500"
            />
            <button className="text-emerald-600 text-sm font-bold">Post</button>
          </div>
        </div>
      </div>

      {/* Sticky Bottom Action */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-4 pb-8 flex items-center space-x-4 max-w-md mx-auto z-50">
        <button
          onClick={() => setIsLiked(!isLiked)}
          className={`p-3 rounded-full border transition-colors ${isLiked ? 'bg-red-50 border-red-100 text-red-500' : 'border-gray-200 text-gray-400 hover:bg-gray-50'
            }`}
        >
          <Heart size={24} fill={isLiked ? "currentColor" : "none"} />
        </button>
        <button
          onClick={handleReserveClick}
          disabled={isReserved && item.status !== ItemStatus.AVAILABLE}
          className={`flex-1 py-4 rounded-xl font-bold text-white flex items-center justify-center space-x-2 transition-all active:scale-[0.98] ${isReserved
            ? 'bg-gray-400 cursor-not-allowed'
            : 'bg-emerald-500 hover:bg-emerald-600 shadow-lg shadow-emerald-200'
            }`}
        >
          {isReserved ? (
            <span>Requested</span>
          ) : (
            <>
              <Clock size={20} />
              <span>Reserve Now</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default ItemDetail;