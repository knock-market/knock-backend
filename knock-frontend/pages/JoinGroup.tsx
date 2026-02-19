import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AlertCircle, Loader2 } from 'lucide-react';
import { groupsApi } from '../services';

const JoinGroup: React.FC = () => {
  const navigate = useNavigate();
  const { code } = useParams<{ code: string }>();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const joinByCode = async () => {
      if (!code) {
        setError('Invite code is missing.');
        return;
      }

      try {
        const result = await groupsApi.joinGroup(code.trim());
        navigate(`/group/${result.id}`, { replace: true });
      } catch (joinError) {
        const message =
          joinError instanceof Error
            ? joinError.message
            : 'Failed to join group with this invite link.';
        setError(message);
      }
    };

    joinByCode();
  }, [code, navigate]);

  if (!error) {
    return (
      <div className="min-h-screen bg-gray-50 max-w-md mx-auto flex flex-col items-center justify-center px-6">
        <Loader2 size={32} className="animate-spin text-emerald-500 mb-4" />
        <h1 className="text-lg font-bold text-gray-900">Joining Group</h1>
        <p className="text-sm text-gray-500 mt-2 text-center">
          We are validating your invite code and moving you to the group.
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 max-w-md mx-auto flex flex-col items-center justify-center px-6">
      <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mb-4">
        <AlertCircle size={24} />
      </div>
      <h1 className="text-lg font-bold text-gray-900">Invite Link Error</h1>
      <p className="text-sm text-gray-500 mt-2 text-center">{error}</p>
      <button
        onClick={() => navigate('/home')}
        className="mt-6 px-5 py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-xl"
      >
        Go Home
      </button>
    </div>
  );
};

export default JoinGroup;
