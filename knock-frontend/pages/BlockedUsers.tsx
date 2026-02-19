import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2, UserX } from 'lucide-react';
import { memberBlockApi } from '../services';
import { BlockedUserResponseDto } from '../types';

const BlockedUsers: React.FC = () => {
  const navigate = useNavigate();
  const [blockedUsers, setBlockedUsers] = useState<BlockedUserResponseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [processingId, setProcessingId] = useState<number | null>(null);

  useEffect(() => {
    const loadBlockedUsers = async () => {
      setIsLoading(true);
      try {
        const users = await memberBlockApi.getBlockedUsers();
        setBlockedUsers(users);
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to load blocked users.';
        alert(message);
      } finally {
        setIsLoading(false);
      }
    };

    loadBlockedUsers();
  }, []);

  const handleUnblock = async (memberId: number) => {
    setProcessingId(memberId);
    try {
      await memberBlockApi.unblockUser(memberId);
      setBlockedUsers((prev) => prev.filter((user) => user.id !== memberId));
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to unblock user.';
      alert(message);
    } finally {
      setProcessingId(null);
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
        {isLoading ? (
          <div className="py-20 flex items-center justify-center text-gray-500">
            <Loader2 size={18} className="animate-spin mr-2" />
            Loading blocked users...
          </div>
        ) : blockedUsers.length > 0 ? (
          <div className="space-y-3">
            {blockedUsers.map((user) => (
              <div key={user.id} className="border border-gray-100 rounded-xl px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-gray-900">{user.name}</p>
                  <p className="text-xs text-gray-500 mt-1">
                    Blocked at {new Date(user.blockedAt).toLocaleString()}
                  </p>
                </div>
                <button
                  onClick={() => handleUnblock(user.id)}
                  disabled={processingId === user.id}
                  className={`text-xs font-semibold px-3 py-1.5 rounded-lg transition-colors ${
                    processingId === user.id
                      ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                      : 'bg-gray-900 text-white hover:bg-black'
                  }`}
                >
                  {processingId === user.id ? '...' : 'Unblock'}
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
            <p className="text-sm text-gray-500 mt-2">You have not blocked anyone.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default BlockedUsers;
