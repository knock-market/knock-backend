import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle, ChevronRight, Edit2, Info, Loader2, Trash2 } from 'lucide-react';
import { itemsApi, reservationsApi } from '../services';
import { ItemStatus, ItemWithUI, ReservationResponseDto, ReservationStatus } from '../types';

type ModalType = 'NONE' | 'RESERVATION' | 'COMPLETE' | 'DELETE';

const MODAL_CONFIG = {
  RESERVATION: {
    title: 'Confirm Reservation?',
    description: 'This will mark the item as reserved for this user.',
    button: 'Confirm',
    buttonClass: 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-200',
    icon: <CheckCircle size={24} strokeWidth={2.5} />,
    iconClass: 'bg-emerald-100 text-emerald-600',
  },
  COMPLETE: {
    title: 'Mark as Completed?',
    description: 'This indicates the transaction is finished.',
    button: 'Complete',
    buttonClass: 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-200',
    icon: <CheckCircle size={24} strokeWidth={2.5} />,
    iconClass: 'bg-emerald-100 text-emerald-600',
  },
  DELETE: {
    title: 'Delete Listing?',
    description: 'Are you sure you want to delete this post? This action cannot be undone.',
    button: 'Delete',
    buttonClass: 'bg-red-500 hover:bg-red-600 shadow-red-200',
    icon: <Trash2 size={24} strokeWidth={2.5} />,
    iconClass: 'bg-red-100 text-red-600',
  },
} as const;

const ManageItemDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [item, setItem] = useState<ItemWithUI | null>(null);
  const [reservations, setReservations] = useState<ReservationResponseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeModal, setActiveModal] = useState<ModalType>('NONE');
  const [selectedReservationId, setSelectedReservationId] = useState<number | null>(null);
  const [toastMessage, setToastMessage] = useState('');

  useEffect(() => {
    if (!toastMessage) return;
    const timer = window.setTimeout(() => setToastMessage(''), 2000);
    return () => window.clearTimeout(timer);
  }, [toastMessage]);

  useEffect(() => {
    const load = async () => {
      if (!id) {
        setItem(null);
        setReservations([]);
        setIsLoading(false);
        return;
      }
      setIsLoading(true);
      try {
        const [itemData, reservationData] = await Promise.all([
          itemsApi.getItem(id),
          reservationsApi.getForItem(id),
        ]);

        setItem({
          ...itemData,
          image: itemData.imageUrls?.[0] || '',
          likesCount: 0,
        });
        setReservations(reservationData);
      } catch (error) {
        console.error('Failed to fetch manage-item detail', error);
        setItem(null);
        setReservations([]);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, [id]);

  const modalContent = useMemo(() => {
    if (activeModal === 'NONE') return null;
    return MODAL_CONFIG[activeModal];
  }, [activeModal]);

  const refreshReservations = async () => {
    if (!id) return;
    try {
      const updated = await reservationsApi.getForItem(id);
      setReservations(updated);
    } catch (error) {
      console.error('Failed to refresh reservations', error);
    }
  };

  const handleConfirmAction = async () => {
    try {
      if (activeModal === 'RESERVATION' && selectedReservationId !== null) {
        await reservationsApi.approve(selectedReservationId);
        setToastMessage('Reservation approved');
        await refreshReservations();
      } else if (activeModal === 'COMPLETE') {
        const approved = reservations.find((reservation) => reservation.status === ReservationStatus.APPROVED);
        if (!approved) {
          alert('No approved reservation found.');
          return;
        }
        await reservationsApi.complete(approved.id);
        setToastMessage('Transaction completed');
        await refreshReservations();
      } else if (activeModal === 'DELETE' && id) {
        await itemsApi.deleteItem(id);
        navigate('/manage-items');
        return;
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to perform action.';
      alert(message);
    } finally {
      setActiveModal('NONE');
      setSelectedReservationId(null);
    }
  };

  const handleEdit = () => {
    alert('Edit feature is not available yet.');
  };

  if (isLoading) {
    return (
      <div className="bg-white min-h-screen flex items-center justify-center max-w-md mx-auto">
        <Loader2 className="w-8 h-8 text-emerald-500 animate-spin" />
      </div>
    );
  }

  if (!item) return <div className="p-8 text-center text-gray-500">Item not found</div>;

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto relative">
      {toastMessage && (
        <div className="fixed top-20 left-1/2 transform -translate-x-1/2 bg-gray-900/90 text-white px-6 py-3 rounded-full text-sm font-medium backdrop-blur-sm z-[60] shadow-xl animate-in fade-in zoom-in duration-200 flex items-center space-x-2 w-max">
          <Info size={16} className="text-emerald-400" />
          <span>{toastMessage}</span>
        </div>
      )}

      {activeModal !== 'NONE' && modalContent && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center px-6">
          <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setActiveModal('NONE')}></div>
          <div className="bg-white w-full max-w-sm rounded-3xl p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex flex-col items-center text-center">
              <div className={`w-12 h-12 rounded-full flex items-center justify-center mb-4 ${modalContent.iconClass}`}>
                {modalContent.icon}
              </div>
              <h2 className="text-xl font-bold text-gray-900 mb-2">{modalContent.title}</h2>
              <p className="text-sm text-gray-500 mb-6 leading-relaxed">{modalContent.description}</p>
              <div className="flex space-x-3 w-full">
                <button
                  onClick={() => setActiveModal('NONE')}
                  className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-xl hover:bg-gray-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleConfirmAction}
                  className={`flex-1 py-3 text-white font-bold rounded-xl shadow-lg transition-colors ${modalContent.buttonClass}`}
                >
                  {modalContent.button}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="px-4 py-4 flex items-center justify-between border-b border-gray-100 sticky top-0 bg-white z-10">
        <div className="flex items-center">
          <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft size={24} />
          </button>
          <h1 className="text-lg font-bold text-gray-900 ml-2">Manage Item</h1>
        </div>
      </div>

      <div className="p-6">
        <div className="flex items-center space-x-4 mb-8">
          <div className="w-20 h-20 rounded-full overflow-hidden border-2 border-gray-100 shadow-sm">
            <img src={item.image} alt={item.title} className="w-full h-full object-cover" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900 leading-tight">{item.title}</h2>
            <div className="flex items-center space-x-2 mt-1">
              <span className="text-lg font-semibold text-gray-500">{item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}</span>
              <span className="text-gray-300">•</span>
              <span className={`text-xs font-bold px-2 py-0.5 rounded-md ${item.status === ItemStatus.ON_SALE ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-200 text-gray-600'}`}>
                {item.type}
              </span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-8">
          <button
            onClick={handleEdit}
            className="w-full flex items-center justify-between p-4 border-b border-gray-50 hover:bg-gray-50 transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-50 text-blue-500 rounded-full">
                <Edit2 size={18} />
              </div>
              <span className="font-semibold text-gray-900 text-sm">Edit Listing</span>
            </div>
            <ChevronRight size={18} className="text-gray-300" />
          </button>
          <button
            onClick={() => setActiveModal('COMPLETE')}
            className="w-full flex items-center justify-between p-4 border-b border-gray-50 hover:bg-gray-50 transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-emerald-50 text-emerald-500 rounded-full">
                <CheckCircle size={18} />
              </div>
              <span className="font-semibold text-gray-900 text-sm">Mark as Completed</span>
            </div>
            <ChevronRight size={18} className="text-gray-300" />
          </button>
          <button
            onClick={() => setActiveModal('DELETE')}
            className="w-full flex items-center justify-between p-4 hover:bg-red-50 group transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-red-50 text-red-500 rounded-full group-hover:bg-red-100">
                <Trash2 size={18} />
              </div>
              <span className="font-semibold text-red-500 text-sm">Delete Post</span>
            </div>
            <ChevronRight size={18} className="text-red-200" />
          </button>
        </div>

        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-gray-900">Reservation List</h3>
            <span className="text-sm text-gray-500">{reservations.length} people</span>
          </div>

          <div className="space-y-3">
            {reservations.length > 0 ? (
              reservations.map((reservation) => (
                <div key={reservation.id} className="bg-white border border-gray-100 rounded-2xl p-4 flex items-center justify-between shadow-sm">
                  <div className="flex items-center space-x-3">
                    <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center font-bold text-emerald-500">
                      {reservation.memberName.charAt(0)}
                    </div>
                    <div>
                      <p className="font-bold text-gray-900 text-sm">{reservation.memberName}</p>
                      <p className="text-xs text-gray-400">{new Date(reservation.createdAt).toLocaleDateString()}</p>
                    </div>
                  </div>

                  {reservation.status === ReservationStatus.APPROVED ? (
                    <button disabled className="px-4 py-2 bg-emerald-100 text-emerald-600 text-xs font-bold rounded-full">
                      Approved
                    </button>
                  ) : reservation.status === ReservationStatus.WAITING ? (
                    <button
                      onClick={() => {
                        setSelectedReservationId(reservation.id);
                        setActiveModal('RESERVATION');
                      }}
                      className="px-5 py-2 bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold rounded-full shadow-md shadow-emerald-200 transition-all active:scale-95"
                    >
                      Confirm
                    </button>
                  ) : (
                    <span className="text-xs text-gray-400 uppercase font-bold">{reservation.status}</span>
                  )}
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-gray-400 text-sm">No reservation requests yet.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ManageItemDetail;
