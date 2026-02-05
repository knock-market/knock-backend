import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { CURRENT_USER } from '../constants';
import { authApi } from '../services';
import { Settings, Clock, Heart, MessageCircle, CheckCircle, ArrowLeft, Package, UserX, Info } from 'lucide-react';
import { ProfileSkeleton } from '../components/Skeletons';
import ImageWithFallback from '../components/ImageWithFallback';
import { MemberResponseDto, User } from '../types';

const Profile: React.FC = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(true);
    const [showToast, setShowToast] = useState(false);
    const [user, setUser] = useState<User>(CURRENT_USER);

    useEffect(() => {
        setIsLoading(true);
        authApi.getMe()
            .then((res: any) => {
                const data: MemberResponseDto = res.data;
                setUser({
                    id: data.id,
                    name: data.name || data.nickname,
                    avatar: data.profileImageUrl || CURRENT_USER.avatar,
                    nickname: data.nickname,
                    role: 'Member', // Not provided by API
                    trustScore: CURRENT_USER.trustScore, // Not provided by API yet
                    badges: CURRENT_USER.badges // Not provided by API yet
                });
            })
            .catch(() => {
                // Fallback to mock user
                setUser(CURRENT_USER);
            })
            .finally(() => setIsLoading(false));
    }, []);

    const handleSeeAll = () => {
        setShowToast(true);
        setTimeout(() => setShowToast(false), 2000);
    };

    if (isLoading) {
        return <ProfileSkeleton />;
    }

    // Calculate circumference
    const radius = 70;
    const circumference = 2 * Math.PI * radius;
    const progress = (user.trustScore || 98) / 100;
    const dashOffset = circumference * (1 - progress);

    return (
        <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto relative">
            {/* Toast Notification */}
            {showToast && (
                <div className="fixed top-20 left-1/2 transform -translate-x-1/2 bg-gray-900/90 text-white px-6 py-3 rounded-full text-sm font-medium backdrop-blur-sm z-[60] shadow-xl animate-in fade-in zoom-in duration-200 flex items-center space-x-2 w-max">
                    <Info size={16} className="text-emerald-400" />
                    <span>Feature under development</span>
                </div>
            )}

            {/* Header with Back Button */}
            <div className="absolute top-0 left-0 right-0 p-4 z-10">
                <button
                    onClick={() => navigate(-1)}
                    className="p-2 text-gray-600 bg-white/50 backdrop-blur-sm rounded-full hover:bg-white transition-colors"
                >
                    <ArrowLeft size={24} />
                </button>
            </div>

            {/* Profile Header Card */}
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

                {/* Trust Score Circle */}
                <div className="mt-8 flex flex-col items-center justify-center">
                    <div className="relative w-40 h-40">
                        {/* Background Circle */}
                        <svg className="w-full h-full -rotate-90" viewBox="0 0 160 160">
                            <circle cx="80" cy="80" r="70" stroke="#f3f4f6" strokeWidth="10" fill="none" />
                            {/* Progress Circle - Green */}
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
                            <span className="text-4xl font-bold text-gray-900 leading-none mb-1">{CURRENT_USER.trustScore}</span>
                            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Manners<br />Score</p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="p-6 space-y-6">
                {/* Stats Grid */}
                <div className="flex justify-between space-x-3">
                    <div className="bg-white flex-1 p-4 rounded-2xl text-center shadow-sm border border-gray-100">
                        <p className="text-[10px] text-gray-400 uppercase font-bold mb-2 tracking-wider">Shared</p>
                        <p className="text-xl font-bold text-gray-900">12</p>
                    </div>
                    <div className="bg-white flex-1 p-4 rounded-2xl text-center shadow-sm border border-gray-100">
                        <p className="text-[10px] text-gray-400 uppercase font-bold mb-2 tracking-wider">Received</p>
                        <p className="text-xl font-bold text-gray-900">8</p>
                    </div>
                    <div className="bg-white flex-1 p-4 rounded-2xl text-center shadow-sm border border-gray-100">
                        <p className="text-[10px] text-emerald-500 uppercase font-bold mb-2 tracking-wider">Active</p>
                        <p className="text-xl font-bold text-emerald-500">3</p>
                    </div>
                </div>

                {/* Badges */}
                <div>
                    <div className="flex justify-between items-center mb-4 px-1">
                        <h2 className="font-bold text-gray-900 text-lg">My Trust Badges</h2>
                        <button
                            onClick={handleSeeAll}
                            className="text-emerald-500 text-xs font-bold hover:text-emerald-600 transition-colors"
                        >
                            See All
                        </button>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div className="bg-white p-5 rounded-2xl border border-gray-100 flex flex-col items-center text-center shadow-sm">
                            <div className="w-12 h-12 bg-amber-50 rounded-full flex items-center justify-center mb-3 text-amber-500 border border-amber-100">
                                <Clock size={24} />
                            </div>
                            <h3 className="font-bold text-sm text-gray-900">Punctual x15</h3>
                            <p className="text-[10px] text-gray-400 mt-1">Always shows up on time</p>
                        </div>
                        <div className="bg-white p-5 rounded-2xl border border-gray-100 flex flex-col items-center text-center shadow-sm">
                            <div className="w-12 h-12 bg-pink-50 rounded-full flex items-center justify-center mb-3 text-pink-500 border border-pink-100">
                                <Heart size={24} />
                            </div>
                            <h3 className="font-bold text-sm text-gray-900">Kind & Friendly x22</h3>
                            <p className="text-[10px] text-gray-400 mt-1">Great attitude</p>
                        </div>
                    </div>
                </div>

                {/* Menu Links */}
                <div className="bg-white rounded-2xl shadow-sm overflow-hidden border border-gray-100">
                    {[
                        { icon: Package, label: 'My Listings', path: '/manage-items' },
                        { icon: Settings, label: 'Edit Profile', path: '/edit-profile' },
                        { icon: MessageCircle, label: 'Notification Settings', path: '/settings/notifications' },
                        { icon: UserX, label: 'Blocked Users', path: '/settings/blocked' },
                    ].map((item, idx) => {
                        const Icon = item.icon || CheckCircle; // Fallback
                        return (
                            <button
                                key={idx}
                                onClick={() => navigate(item.path)}
                                className="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-50 border-b border-gray-100 last:border-0 transition-colors group"
                            >
                                <div className="flex items-center space-x-4">
                                    <div className="text-gray-400 bg-gray-50 p-2 rounded-lg group-hover:bg-white group-hover:text-emerald-500 transition-colors">
                                        <Icon size={20} />
                                    </div>
                                    <span className="font-medium text-gray-700 text-sm">{item.label}</span>
                                </div>
                                <div className="text-gray-300">›</div>
                            </button>
                        )
                    })}
                </div>

                <div className="pt-2">
                    <button
                        onClick={() => navigate('/')}
                        className="w-full py-4 text-center text-gray-400 text-sm font-medium hover:text-red-500 transition-colors"
                    >
                        Log Out
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Profile;