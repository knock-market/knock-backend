import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, UserX, Unlock } from 'lucide-react';

// Mock Data for Blocked Users
const MOCK_BLOCKED = [
    { id: 'b1', name: 'Spam Bot 3000', date: 'Blocked on Jan 12, 2024' },
    { id: 'b2', name: 'Rude Buyer', date: 'Blocked on Dec 05, 2023' },
];

const BlockedUsers: React.FC = () => {
  const navigate = useNavigate();
  const [blockedUsers, setBlockedUsers] = useState(MOCK_BLOCKED);

  const handleUnblock = (id: string) => {
      if (window.confirm("Are you sure you want to unblock this user?")) {
          setBlockedUsers(prev => prev.filter(u => u.id !== id));
      }
  };

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Blocked Users</h1>
      </div>

      <div className="p-4">
        {blockedUsers.length > 0 ? (
            <div className="space-y-4">
                {blockedUsers.map(user => (
                    <div key={user.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-gray-100">
                        <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center text-gray-500">
                                <UserX size={20} />
                            </div>
                            <div>
                                <h3 className="text-sm font-bold text-gray-900">{user.name}</h3>
                                <p className="text-xs text-gray-500">{user.date}</p>
                            </div>
                        </div>
                        <button 
                            onClick={() => handleUnblock(user.id)}
                            className="px-3 py-1.5 text-xs font-bold text-gray-600 border border-gray-300 rounded-lg hover:bg-white hover:text-emerald-600 hover:border-emerald-200 transition-colors flex items-center space-x-1"
                        >
                            <Unlock size={12} />
                            <span>Unblock</span>
                        </button>
                    </div>
                ))}
            </div>
        ) : (
            <div className="flex flex-col items-center justify-center py-20 text-center">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center text-gray-400 mb-4">
                    <UserX size={32} />
                </div>
                <h3 className="text-lg font-bold text-gray-900">No blocked users</h3>
                <p className="text-sm text-gray-500 mt-2">
                    You haven't blocked anyone yet.<br/>
                    Blocked users cannot see your posts or message you.
                </p>
            </div>
        )}
      </div>
    </div>
  );
};

export default BlockedUsers;