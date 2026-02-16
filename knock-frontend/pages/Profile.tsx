import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle, MessageCircle, Package, Settings, UserX } from 'lucide-react';
import { authApi, itemsApi, reservationsApi } from '../services';
import { ItemStatus, MemberResponseDto, ReservationStatus, User } from '../types';
import { ProfileSkeleton } from '../components/Skeletons';
import ImageWithFallback from '../components/ImageWithFallback';
import { DEFAULT_AVATAR } from '../constants';

interface ProfileStats {
  shared: number;
  received: number;
  active: number;
}

const Profile: React.FC = () => {
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<User>({
    id: 'unknown',
    name: 'User',
    avatar: DEFAULT_AVATAR,
    role: 'Member',
  });
  const [mannerTemperature, setMannerTemperature] = useState(36.5);
  const [stats, setStats] = useState<ProfileStats>({ shared: 0, received: 0, active: 0 });
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      try {
        const [me, sellingItems, reservations] = await Promise.all([
          authApi.getMe(),
          itemsApi.getMySelling(),
          reservationsApi.getMyReservations(),
        ]);

        const memberData = me as MemberResponseDto;
        setUser({
          id: memberData.id ?? memberData.email,
          name: memberData.name || memberData.nickname,
          avatar: memberData.profileImageUrl || DEFAULT_AVATAR,
          nickname: memberData.nickname,
          role: 'Member',
        });
        setMannerTemperature(memberData.mannerTemperature ?? 36.5);

        const shared = sellingItems.length;
        const active = sellingItems.filter((item) => item.status === ItemStatus.ON_SALE).length;
        const received = reservations.filter((reservation) => reservation.status === ReservationStatus.COMPLETED).length;
        setStats({ shared, received, active });
      } catch (error) {
        console.error('Failed to fetch profile data', error);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, []);

  if (isLoading) {
    return <ProfileSkeleton />;
  }

  const handleLogout = async () => {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);
    try {
      await authApi.logout();
      navigate('/', { replace: true });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to log out.';
      alert(message);
    } finally {
      setIsLoggingOut(false);
    }
  };

  const radius = 70;
  const circumference = 2 * Math.PI * radius;
  const progress = Math.min(Math.max((mannerTemperature / 50) * 100, 0), 100) / 100;
  const dashOffset = circumference * (1 - progress);

  return (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto relative">
      <div className="absolute top-0 left-0 right-0 p-4 z-10">
        <button
          onClick={() => navigate(-1)}
          className="p-2 text-gray-600 bg-white/50 backdrop-blur-sm rounded-full hover:bg-white transition-colors"
        >
          <ArrowLeft size={24} />
        </button>
      </div>

      <div className="bg-white pt-12 pb-8 rounded-b-[2.5rem] shadow-sm text-center relative px-6">
        <div className="relative inline-block mb-3 mt-4">
          <div className="p-1 rounded-full bg-gradient-to-tr from-emerald-400 to-amber-300">
            <ImageWithFallback src={user.avatar} alt="Profile" className="w-24 h-24 rounded-full border-4 border-white object-cover shadow-sm" />
          </div>
          <div className="absolute bottom-1 right-1 bg-emerald-500 text-white p-1 rounded-full border-2 border-white">
            <CheckCircle size={12} strokeWidth={3} />
          </div>
        </div>

        <h1 className="text-xl font-bold text-gray-900 leading-tight">{user.name}</h1>
        <p className="text-sm text-gray-400 mt-1">{user.role || 'Member'}</p>

        <div className="mt-8 flex flex-col items-center justify-center">
          <div className="relative w-40 h-40">
            <svg className="w-full h-full -rotate-90" viewBox="0 0 160 160">
              <circle cx="80" cy="80" r="70" stroke="#f3f4f6" strokeWidth="10" fill="none" />
              <circle
                cx="80"
                cy="80"
                r="70"
                stroke="#10b981"
                strokeWidth="10"
                fill="none"
                strokeDasharray={circumference}
                strokeDashoffset={dashOffset}
                strokeLinecap="round"
              />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
              <span className="text-3xl font-bold text-gray-900 leading-none mb-1">{mannerTemperature.toFixed(1)}°</span>
              <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Manner<br />Temperature</p>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6 space-y-6">
        <div className="flex justify-between space-x-3">
          <div className="bg-white flex-1 p-4 rounded-2xl text-center shadow-sm border border-gray-100">
            <p className="text-[10px] text-gray-400 uppercase font-bold mb-2 tracking-wider">Shared</p>
            <p className="text-xl font-bold text-gray-900">{stats.shared}</p>
          </div>
          <div className="bg-white flex-1 p-4 rounded-2xl text-center shadow-sm border border-gray-100">
            <p className="text-[10px] text-gray-400 uppercase font-bold mb-2 tracking-wider">Received</p>
            <p className="text-xl font-bold text-gray-900">{stats.received}</p>
          </div>
          <div className="bg-white flex-1 p-4 rounded-2xl text-center shadow-sm border border-gray-100">
            <p className="text-[10px] text-emerald-500 uppercase font-bold mb-2 tracking-wider">Active</p>
            <p className="text-xl font-bold text-emerald-500">{stats.active}</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm overflow-hidden border border-gray-100">
          {[
            { icon: Package, label: 'My Listings', path: '/manage-items' },
            { icon: Settings, label: 'Edit Profile', path: '/edit-profile' },
            { icon: MessageCircle, label: 'Notification Settings', path: '/settings/notifications' },
            { icon: UserX, label: 'Blocked Users', path: '/settings/blocked' },
          ].map((menu, idx) => {
            const Icon = menu.icon;
            return (
              <button
                key={idx}
                onClick={() => navigate(menu.path)}
                className="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-50 border-b border-gray-100 last:border-0 transition-colors group"
              >
                <div className="flex items-center space-x-4">
                  <div className="text-gray-400 bg-gray-50 p-2 rounded-lg group-hover:bg-white group-hover:text-emerald-500 transition-colors">
                    <Icon size={20} />
                  </div>
                  <span className="font-medium text-gray-700 text-sm">{menu.label}</span>
                </div>
                <div className="text-gray-300">›</div>
              </button>
            );
          })}
        </div>

        <div className="pt-2">
          <button
            onClick={handleLogout}
            disabled={isLoggingOut}
            className="w-full py-4 text-center text-gray-400 text-sm font-medium hover:text-red-500 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {isLoggingOut ? 'Logging out...' : 'Log Out'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile;
