import React, { useMemo, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Keyboard, X, ArrowRight, Loader2, User } from 'lucide-react';
import { authApi, groupsApi } from '../services';
import { HomeSkeleton } from '../components/Skeletons';
import ImageWithFallback from '../components/ImageWithFallback';
import { GroupResponseDto } from '../types';

const GREETINGS = [
  { title: (name: string) => `Welcome back, ${name}! 👋`, subtitle: "See what's happening in your circles today." },
  { title: (name: string) => `Hey ${name}! ✨`, subtitle: "Let's check out what's new." },
  { title: (name: string) => `Good to see you, ${name}! 🎉`, subtitle: 'Your circles are waiting.' },
];

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [joinCode, setJoinCode] = useState('');
  const [isJoining, setIsJoining] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [groups, setGroups] = useState<GroupResponseDto[]>([]);
  const [userName, setUserName] = useState('User');

  const greeting = useMemo(() => {
    const index = Math.floor(Math.random() * GREETINGS.length);
    return GREETINGS[index];
  }, []);

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      try {
        const [groupList, me] = await Promise.all([groupsApi.getMyGroups(), authApi.getMe()]);
        setGroups(groupList);
        const firstName = (me.name || me.nickname || 'User').split(' ')[0];
        setUserName(firstName);
      } catch (error) {
        console.error('Failed to fetch home data', error);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, []);

  const handleJoinSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const inviteCode = joinCode.trim();
    if (!inviteCode) return;

    setIsJoining(true);
    try {
      const joined = await groupsApi.joinGroup(inviteCode);
      setShowJoinModal(false);
      setJoinCode('');
      navigate(`/group/${joined.id}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to join group.';
      alert(message);
    } finally {
      setIsJoining(false);
    }
  };

  if (isLoading) {
    return <HomeSkeleton />;
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto relative">
      <div className="fixed bottom-24 left-0 right-0 max-w-md mx-auto z-40 px-6 flex justify-end pointer-events-none">
        <button
          onClick={() => navigate('/create')}
          className="w-14 h-14 bg-emerald-500 hover:bg-emerald-600 text-white rounded-full shadow-lg shadow-emerald-200 transition-all active:scale-90 flex items-center justify-center pointer-events-auto"
          aria-label="Sell Item"
        >
          <Plus size={28} strokeWidth={2.5} />
        </button>
      </div>

      {showJoinModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"
            onClick={() => setShowJoinModal(false)}
          ></div>
          <div className="bg-white w-full max-w-sm rounded-3xl p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
            <button
              onClick={() => setShowJoinModal(false)}
              className="absolute top-4 right-4 p-2 text-gray-400 hover:bg-gray-100 rounded-full transition-colors"
            >
              <X size={20} />
            </button>

            <div className="text-center mb-6">
              <div className="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-900">
                <Keyboard size={24} />
              </div>
              <h2 className="text-xl font-bold text-gray-900">Enter Invite Code</h2>
              <p className="text-sm text-gray-500 mt-1">Paste the code shared by your group admin.</p>
            </div>

            <form onSubmit={handleJoinSubmit}>
              <input
                type="text"
                value={joinCode}
                onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
                placeholder="e.g. KNOCK2024"
                className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-center text-lg font-mono font-bold tracking-widest focus:outline-none focus:ring-2 focus:ring-emerald-500 mb-6 uppercase placeholder:tracking-normal placeholder:font-sans placeholder:font-normal placeholder:text-gray-400"
                autoFocus
              />

              <button
                type="submit"
                disabled={!joinCode.trim() || isJoining}
                className={`w-full font-bold py-4 rounded-xl flex items-center justify-center space-x-2 transition-all active:scale-[0.98] ${joinCode.trim()
                  ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-200 hover:bg-emerald-600'
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  }`}
              >
                {isJoining ? (
                  <>
                    <Loader2 size={20} className="animate-spin" />
                    <span>Verifying...</span>
                  </>
                ) : (
                  <>
                    <span>Join Group</span>
                    <ArrowRight size={20} />
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      )}

      <div className="bg-white px-6 pt-12 pb-6 sticky top-0 z-10 shadow-sm">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">{greeting.title(userName)}</h1>
        <p className="text-gray-500 text-sm">{greeting.subtitle}</p>
      </div>

      <div className="p-6 space-y-8">
        <div className="grid grid-cols-2 gap-4">
          <button
            onClick={() => navigate('/create-group')}
            className="bg-emerald-400 hover:bg-emerald-500 transition-colors rounded-2xl p-6 flex flex-col items-center justify-center text-white shadow-lg shadow-emerald-200 active:scale-[0.98]"
          >
            <div className="bg-white/20 p-3 rounded-full mb-3">
              <Plus size={24} />
            </div>
            <span className="font-semibold">Create Group</span>
          </button>

          <button
            onClick={() => setShowJoinModal(true)}
            className="bg-gray-100 hover:bg-gray-200 transition-colors rounded-2xl p-6 flex flex-col items-center justify-center text-gray-700 active:scale-[0.98]"
          >
            <div className="bg-white p-3 rounded-full mb-3 shadow-sm">
              <Keyboard size={24} className="text-gray-800" />
            </div>
            <span className="font-semibold">Join via Code</span>
          </button>
        </div>

        <div>
          <h2 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4">Joined Communities</h2>
          <div className="space-y-4">
            {groups.length > 0 ? (
              groups.map((group) => (
                <div
                  key={group.id}
                  onClick={() => navigate(`/group/${group.id}`)}
                  className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 flex items-center space-x-4 hover:shadow-md transition-all cursor-pointer active:scale-[0.99]"
                >
                  <ImageWithFallback
                    src={group.profileImageUrl}
                    alt={group.name}
                    className="w-16 h-16 rounded-xl object-cover"
                  />
                  <div className="flex-1">
                    <h3 className="font-bold text-gray-900">{group.name}</h3>
                    <p className="text-sm text-gray-500 flex items-center mt-1">
                      <span className="w-2 h-2 rounded-full mr-2 bg-gray-300"></span>
                      {group.memberCount ?? 0} members
                    </p>
                  </div>
                  <div className="text-gray-300">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
                  </div>
                </div>
              ))
            ) : (
              <div className="bg-white rounded-3xl p-8 text-center border-2 border-dashed border-gray-100">
                <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-400">
                  <User size={32} />
                </div>
                <h3 className="font-bold text-gray-900 mb-1">No groups yet</h3>
                <p className="text-sm text-gray-500 mb-6">Join a community to start trading!</p>
                <button
                  onClick={() => setShowJoinModal(true)}
                  className="px-6 py-2 bg-emerald-500 text-white text-sm font-bold rounded-full shadow-lg shadow-emerald-100"
                >
                  Join First Group
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
