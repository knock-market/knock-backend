import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Store, User, Bell } from 'lucide-react';

const BottomNav: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { icon: Store, label: 'Market', path: '/home' },
    { icon: Bell, label: 'Notifications', path: '/notifications' },
    { icon: User, label: 'MyPage', path: '/profile' },
  ];

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-3 pb-6 flex justify-around items-center z-50 max-w-md mx-auto shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)]"
      aria-label="Primary navigation"
    >
      {navItems.map((item) => {
        // Active state logic:
        const isActive = location.pathname === item.path ||
          (item.label === 'Market' && location.pathname === '/home') ||
          (item.label === 'MyPage' && location.pathname === '/manage-items');

        return (
          <button
            key={item.label}
            onClick={() => navigate(item.path)}
            className={`flex flex-col items-center justify-center space-y-1.5 w-full rounded-lg py-1 transition-colors duration-200 focus-visible:ring-2 focus-visible:ring-emerald-600 ${isActive ? 'text-emerald-700' : 'text-gray-500 hover:text-gray-700'
              }`}
            aria-current={isActive ? 'page' : undefined}
          >
            <item.icon size={26} strokeWidth={isActive ? 2.5 : 2} />
            <span className={`text-[10px] font-medium ${isActive ? 'font-bold' : ''}`}>
              {item.label}
            </span>
          </button>
        );
      })}
    </nav>
  );
};

export default BottomNav;
