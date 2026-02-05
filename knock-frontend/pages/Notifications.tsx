import React, { useState, useEffect } from 'react';
import { ArrowLeft, CheckCircle, Clock, PlusCircle, Loader2, Bell } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { notificationsApi } from '../services';
import { MOCK_NOTIFICATIONS } from '../constants';
import { NotificationResponseDto } from '../types';

const Notifications: React.FC = () => {
    const navigate = useNavigate();
    const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        setIsLoading(true);
        notificationsApi.getAll()
            .then((res: any) => {
                setNotifications(res.data || []);
            })
            .catch(() => {
                // Fallback to mock data on error
                setNotifications(MOCK_NOTIFICATIONS);
            })
            .finally(() => setIsLoading(false));
    }, []);

    const getIcon = (type: string) => {
        switch (type) {
            case 'CONFIRMATION': return <CheckCircle className="text-blue-500" />;
            case 'RESERVATION': return <Clock className="text-emerald-500" />;
            case 'NEW_ITEM': return <PlusCircle className="text-purple-500" />;
            default: return <Clock className="text-gray-500" />;
        }
    };

    const getBg = (type: string) => {
        switch (type) {
            case 'CONFIRMATION': return 'bg-blue-100';
            case 'RESERVATION': return 'bg-emerald-100';
            case 'NEW_ITEM': return 'bg-purple-100';
            default: return 'bg-gray-100';
        }
    };

    const handleMarkAsRead = (id: string | number) => {
        notificationsApi.markAsRead(id)
            .then(() => {
                setNotifications(prev =>
                    prev.map(n => n.id === id ? { ...n, isRead: true } : n)
                );
            })
            .catch(console.error);
    };

    if (isLoading) {
        return (
            <div className="bg-white min-h-screen flex items-center justify-center max-w-md mx-auto">
                <Loader2 className="w-8 h-8 text-emerald-500 animate-spin" />
            </div>
        );
    }

    return (
        <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
            <div className="px-4 py-4 flex items-center justify-between border-b border-gray-100 sticky top-0 bg-white z-10">
                <div className="flex items-center">
                    <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600">
                        <ArrowLeft size={24} />
                    </button>
                    <h1 className="text-lg font-bold text-gray-900 ml-2">Notifications</h1>
                </div>
                <button className="text-emerald-500 text-sm font-bold">Mark all as read</button>
            </div>

            {notifications.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-32 text-center">
                    <div className="w-20 h-20 bg-gray-50 rounded-3xl flex items-center justify-center text-gray-300 mb-6">
                        <Bell size={40} strokeWidth={1.5} />
                    </div>
                    <h3 className="text-lg font-bold text-gray-900 mb-2">All caught up!</h3>
                    <p className="text-sm text-gray-500 max-w-[200px] mx-auto leading-relaxed">
                        We'll let you know when there are new requests or community updates.
                    </p>
                </div>
            ) : (
                <div className="divide-y divide-gray-100">
                    {notifications.map((notif) => (
                        <div
                            key={notif.id}
                            className={`p-5 flex space-x-4 ${!notif.isRead ? 'bg-emerald-50/50' : 'bg-white'}`}
                            onClick={() => !notif.isRead && handleMarkAsRead(notif.id)}
                        >
                            <div className={`w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 ${getBg(notif.type)}`}>
                                {getIcon(notif.type)}
                            </div>
                            <div className="flex-1">
                                <div className="flex justify-between items-start mb-1">
                                    <h3 className="font-bold text-gray-900 text-sm">{notif.title || notif.type}</h3>
                                    <span className="text-[10px] text-gray-400">{notif.time || notif.createdAt}</span>
                                </div>
                                <p className="text-sm text-gray-600 leading-snug">{notif.message || notif.content}</p>

                                {notif.type === 'RESERVATION' && (
                                    <div className="mt-3 flex space-x-2">
                                        <button className="bg-emerald-500 text-white text-xs font-bold px-4 py-2 rounded-lg hover:bg-emerald-600">Coordinate</button>
                                        <button className="bg-gray-100 text-gray-600 text-xs font-bold px-4 py-2 rounded-lg hover:bg-gray-200">Decline</button>
                                    </div>
                                )}
                            </div>
                            {!notif.isRead && (
                                <div className="w-2 h-2 rounded-full bg-emerald-500 mt-2"></div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Notifications;