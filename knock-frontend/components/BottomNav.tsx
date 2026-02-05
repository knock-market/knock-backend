import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Users, Heart, User, Bell } from 'lucide-react';

const BottomNav: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { icon: Users, label: 'Groups', path: '/home' },
    { icon: Heart, label: 'Likes', path: '/saved' },
    { icon: Bell, label: 'Notifications', path: '/notifications' },
    { icon: User, label: 'MyPage', path: '/profile' },
  ];

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-3 pb-6 flex justify-around items-center z-50 max-w-md mx-auto shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)]">
      {navItems.map((item) => {
        // Active state logic:
        // /home encompasses the main Groups view.
        // /group/:id should also highlight Groups
        // /saved is for Likes.
        // /notifications is for Notifications.
        // /profile is for My Page.
        // /manage-items should highlight My Page.
        const isActive = location.pathname === item.path || 
          (item.label === 'Groups' && (location.pathname === '/home' || location.pathname === '/groups' || location.pathname.startsWith('/group/'))) ||
          (item.label === 'MyPage' && location.pathname === '/manage-items');
        
        return (
          <button
            key={item.label}
            onClick={() => navigate(item.path)}
            className={`flex flex-col items-center justify-center space-y-1.5 w-full transition-all duration-200 active:scale-95 ${
              isActive ? 'text-emerald-600' : 'text-gray-400 hover:text-gray-600'
            }`}
          >
            <item.icon size={26} strokeWidth={isActive ? 2.5 : 2} fill={isActive && item.label === 'Likes' ? 'currentColor' : 'none'} />
            <span className={`text-[10px] font-medium ${isActive ? 'font-bold' : ''}`}>
              {item.label}
            </span>
          </button>
        );
      })}
    </div>
  );
};

export default BottomNav;