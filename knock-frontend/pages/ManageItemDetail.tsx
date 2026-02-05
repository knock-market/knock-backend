import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit2, CheckCircle, Trash2, ChevronRight, Info, Loader2 } from 'lucide-react';
import { MOCK_ITEMS } from '../constants';
import { itemsApi, reservationsApi } from '../services';
import { ItemStatus, ItemWithUI, ReservationResponseDto, ReservationStatus } from '../types';

type ModalType = 'NONE' | 'RESERVATION' | 'COMPLETE' | 'DELETE';

const ManageItemDetail: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [item, setItem] = useState<ItemWithUI | undefined>(MOCK_ITEMS.find((i) => i.id === id));
    const [reservations, setReservations] = useState<ReservationResponseDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // State for Modal and Toast
    const [activeModal, setActiveModal] = useState<ModalType>('NONE');
    const [selectedReservationId, setSelectedReservationId] = useState<string | number | null>(null);
    const [showToast, setShowToast] = useState(false);
    const [toastMessage, setToastMessage] = useState('');

    useEffect(() => {
        if (!id) return;

        fetchData();
    }, [id]);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [itemRes, resRes] = await Promise.all([
                itemsApi.getItem(id!),
                reservationsApi.getForItem(id!)
            ]);

            const itemData = itemRes.data;
            setItem({
                ...itemData,
                image: itemData.imageUrls?.[0] || '',
            });
            setReservations(resRes.data || []);
        } catch (err) {
            console.error("Failed to fetch data:", err);
            // Fallback already handled by initial state for demo
        } finally {
            setIsLoading(false);
        }
    };

    if (isLoading) {
        return (
            <div className="bg-white min-h-screen flex items-center justify-center max-w-md mx-auto">
                <Loader2 className="w-8 h-8 text-emerald-500 animate-spin" />
            </div>
        );
    }

    if (!item) return <div className="p-8 text-center text-gray-500">Item not found</div>;

    // Modal Triggers
    const openReservationModal = (resId: string | number) => {
        setSelectedReservationId(resId);
        setActiveModal('RESERVATION');
    };

    const openCompleteModal = () => {
        setActiveModal('COMPLETE');
    };

    const openDeleteModal = () => {
        setActiveModal('DELETE');
    };

    // Action Execution
    const handleConfirmAction = async () => {
        try {
            if (activeModal === 'RESERVATION' && selectedReservationId) {
                await reservationsApi.approve(Number(selectedReservationId));
                setToastMessage('Reservation approved');
            } else if (activeModal === 'COMPLETE') {
                const approvedRes = reservations.find(r => r.status === ReservationStatus.APPROVED);
                if (approvedRes) {
                    await reservationsApi.complete(Number(approvedRes.id));
                    setToastMessage('Transaction completed');
                }
            } else if (activeModal === 'DELETE') {
                // itemsApi.delete logic...
                setToastMessage('Item deleted (simulated)');
                navigate('/manage-items');
                return;
            }

            setShowToast(true);
            setTimeout(() => setShowToast(false), 2000);
            fetchData(); // Refresh state
        } catch (err) {
            console.error("Action failed:", err);
            alert("Failed to perform action");
        } finally {
            setActiveModal('NONE');
            setSelectedReservationId(null);
        }
    };

    const handleEdit = () => {
        setToastMessage('Edit feature under development');
        setShowToast(true);
        setTimeout(() => setShowToast(false), 2000);
    };

    // Modal Content Config
    const getModalContent = () => {
        switch (activeModal) {
            case 'RESERVATION':
                return {
                    icon: <CheckCircle size={24} strokeWidth={2.5} />,
                    iconBg: 'bg-emerald-100',
                    iconColor: 'text-emerald-600',
                    title: 'Confirm Reservation?',
                    description: 'This will mark the item as reserved for this user. Other requests will be kept on hold.',
                    confirmBtnText: 'Confirm',
                    confirmBtnClass: 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-200'
                };
            case 'COMPLETE':
                return {
                    icon: <CheckCircle size={24} strokeWidth={2.5} />,
                    iconBg: 'bg-emerald-100',
                    iconColor: 'text-emerald-600',
                    title: 'Mark as Completed?',
                    description: 'This indicates the transaction is finished. The item will be moved to your history.',
                    confirmBtnText: 'Complete',
                    confirmBtnClass: 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-200'
                };
            case 'DELETE':
                return {
                    icon: <Trash2 size={24} strokeWidth={2.5} />,
                    iconBg: 'bg-red-100',
                    iconColor: 'text-red-600',
                    title: 'Delete Listing?',
                    description: 'Are you sure you want to delete this post? This action cannot be undone.',
                    confirmBtnText: 'Delete',
                    confirmBtnClass: 'bg-red-500 hover:bg-red-600 shadow-red-200'
                };
            default:
                return null;
        }
    };

    const modalContent = getModalContent();

    return (
        <div className="bg-white min-h-screen pb-24 max-w-md mx-auto relative">
            {/* Toast Notification */}
            {showToast && (
                <div className="fixed top-20 left-1/2 transform -translate-x-1/2 bg-gray-900/90 text-white px-6 py-3 rounded-full text-sm font-medium backdrop-blur-sm z-[60] shadow-xl animate-in fade-in zoom-in duration-200 flex items-center space-x-2 w-max">
                    <Info size={16} className="text-emerald-400" />
                    <span>{toastMessage}</span>
                </div>
            )}

            {/* Unified Confirmation Modal */}
            {activeModal !== 'NONE' && modalContent && (
                <div className="fixed inset-0 z-[70] flex items-center justify-center px-6">
                    <div
                        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
                        onClick={() => setActiveModal('NONE')}
                    ></div>
                    <div className="bg-white w-full max-w-sm rounded-3xl p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
                        <div className="flex flex-col items-center text-center">
                            <div className={`w-12 h-12 rounded-full flex items-center justify-center mb-4 ${modalContent.iconBg} ${modalContent.iconColor}`}>
                                {modalContent.icon}
                            </div>
                            <h2 className="text-xl font-bold text-gray-900 mb-2">{modalContent.title}</h2>
                            <p className="text-sm text-gray-500 mb-6 leading-relaxed">
                                {modalContent.description}
                            </p>
                            <div className="flex space-x-3 w-full">
                                <button
                                    onClick={() => setActiveModal('NONE')}
                                    className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-xl hover:bg-gray-200 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handleConfirmAction}
                                    className={`flex-1 py-3 text-white font-bold rounded-xl shadow-lg transition-colors ${modalContent.confirmBtnClass}`}
                                >
                                    {modalContent.confirmBtnText}
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
                {/* Header Item Info */}
                <div className="flex items-center space-x-4 mb-8">
                    <div className="w-20 h-20 rounded-full overflow-hidden border-2 border-gray-100 shadow-sm">
                        <img src={item.image} alt={item.title} className="w-full h-full object-cover" />
                    </div>
                    <div>
                        <h2 className="text-xl font-bold text-gray-900 leading-tight">{item.title}</h2>
                        <div className="flex items-center space-x-2 mt-1">
                            <span className="text-lg font-semibold text-gray-500">
                                {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                            </span>
                            <span className="text-gray-300">•</span>
                            <span className={`text-xs font-bold px-2 py-0.5 rounded-md ${item.status === 'AVAILABLE' ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-200 text-gray-600'}`}>
                                {item.type === 'SALE' ? 'SALE' : item.type}
                            </span>
                        </div>
                        <div className="mt-1">
                            <span className={`text-[10px] px-2 py-0.5 rounded font-bold uppercase tracking-wide ${item.status === 'AVAILABLE' ? 'bg-green-100 text-green-700' :
                                item.status === 'RESERVED' ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-500'
                                }`}>
                                {item.status}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Action Buttons List */}
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
                        onClick={openCompleteModal}
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
                        onClick={openDeleteModal}
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

                {/* Reservation List */}
                <div>
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="text-lg font-bold text-gray-900">Reservation List</h3>
                        <span className="text-sm text-gray-500">{reservations.length} people</span>
                    </div>

                    <div className="space-y-3">
                        {reservations.length > 0 ? (
                            reservations.map((res, idx) => (
                                <div key={res.id} className="bg-white border border-gray-100 rounded-2xl p-4 flex items-center justify-between shadow-sm">
                                    <div className="flex items-center space-x-3">
                                        <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center font-bold text-emerald-500">
                                            {res.memberName.charAt(0)}
                                        </div>
                                        <div>
                                            <p className="font-bold text-gray-900 text-sm">{res.memberName}</p>
                                            <p className="text-xs text-gray-400">{new Date(res.createdAt).toLocaleDateString()}</p>
                                        </div>
                                    </div>

                                    {res.status === ReservationStatus.APPROVED ? (
                                        <button
                                            disabled
                                            className="px-4 py-2 bg-emerald-100 text-emerald-600 text-xs font-bold rounded-full"
                                        >
                                            Approved
                                        </button>
                                    ) : res.status === ReservationStatus.PENDING ? (
                                        <button
                                            onClick={() => openReservationModal(res.id)}
                                            className="px-5 py-2 bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold rounded-full shadow-md shadow-emerald-200 transition-all active:scale-95"
                                        >
                                            Confirm
                                        </button>
                                    ) : (
                                        <span className="text-xs text-gray-400 uppercase font-bold">{res.status}</span>
                                    )}
                                </div>
                            ))
                        ) : (
                            <div className="text-center py-8 text-gray-400 text-sm">
                                No reservation requests yet.
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ManageItemDetail;