import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Bell, CheckCircle, Clock, Loader2, PlusCircle } from 'lucide-react';
import { notificationsApi } from '../services';
import { NotificationResponseDto } from '../types';

const getUiType = (notificationType: string) => {
  if (notificationType.includes('APPROVED')) return 'CONFIRMATION';
  if (notificationType.includes('CREATED') || notificationType.includes('RESERVATION')) return 'RESERVATION';
  if (notificationType.includes('COMMENT')) return 'NEW_ITEM';
  return 'DEFAULT';
};

const Notifications: React.FC = () => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      try {
        const result = await notificationsApi.getAll();
        setNotifications(result);
      } catch (error) {
        console.error('Failed to fetch notifications', error);
        setNotifications([]);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, []);

  const unreadIds = useMemo(
    () => notifications.filter((notification) => !notification.isRead).map((notification) => notification.id),
    [notifications]
  );

  const getIcon = (notificationType: string) => {
    switch (getUiType(notificationType)) {
      case 'CONFIRMATION':
        return <CheckCircle className="text-blue-500" />;
      case 'RESERVATION':
        return <Clock className="text-emerald-500" />;
      case 'NEW_ITEM':
        return <PlusCircle className="text-purple-500" />;
      default:
        return <Clock className="text-gray-500" />;
    }
  };

  const getBg = (notificationType: string) => {
    switch (getUiType(notificationType)) {
      case 'CONFIRMATION':
        return 'bg-blue-100';
      case 'RESERVATION':
        return 'bg-emerald-100';
      case 'NEW_ITEM':
        return 'bg-purple-100';
      default:
        return 'bg-gray-100';
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationsApi.markAsRead(id);
      setNotifications((prev) => prev.map((notification) => (notification.id === id ? { ...notification, isRead: true } : notification)));
    } catch (error) {
      console.error('Failed to mark notification as read', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (unreadIds.length === 0) return;

    try {
      await notificationsApi.markAllAsRead();
      setNotifications((prev) => prev.map((notification) => ({ ...notification, isRead: true })));
    } catch (error) {
      console.error('Failed to mark all notifications as read', error);
    }
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
        <button onClick={handleMarkAllAsRead} className="text-emerald-500 text-sm font-bold">Mark all as read</button>
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
          {notifications.map((notification) => (
            <div
              key={notification.id}
              className={`p-5 flex space-x-4 ${!notification.isRead ? 'bg-emerald-50/50' : 'bg-white'}`}
              onClick={() => !notification.isRead && handleMarkAsRead(notification.id)}
            >
              <div className={`w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 ${getBg(notification.notificationType)}`}>
                {getIcon(notification.notificationType)}
              </div>
              <div className="flex-1">
                <div className="flex justify-between items-start mb-1">
                  <h3 className="font-bold text-gray-900 text-sm">{notification.notificationType.replace(/_/g, ' ')}</h3>
                  <span className="text-[10px] text-gray-400">{new Date(notification.createdAt).toLocaleString()}</span>
                </div>
                <p className="text-sm text-gray-600 leading-snug">{notification.content}</p>
              </div>
              {!notification.isRead && <div className="w-2 h-2 rounded-full bg-emerald-500 mt-2"></div>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Notifications;
