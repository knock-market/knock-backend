import React, { useDeferredValue, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, LogOut, Package, Plus, Search, SlidersHorizontal, X } from 'lucide-react';
import { groupsApi, itemsApi } from '../services';
import { GroupResponseDto, ItemCategory, ItemStatus, ItemSummaryResponseDto, ItemType } from '../types';
import { CATEGORY_LABELS } from '../constants';
import { GroupFeedSkeleton } from '../components/Skeletons';
import ImageWithFallback from '../components/ImageWithFallback';
import SellerFilterBar from '../components/SellerFilterBar';
const GroupFeed: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const groupId = id ? Number(id) : NaN;
  const hasValidGroupId = Number.isInteger(groupId) && groupId > 0;
  const [group, setGroup] = useState<GroupResponseDto | null>(null);
  const [items, setItems] = useState<ItemSummaryResponseDto[]>([]);
  const [activeFilter, setActiveFilter] = useState('All');
  const [activeSellerId, setActiveSellerId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [showInfo, setShowInfo] = useState(false);
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLeaving, setIsLeaving] = useState(false);
  const filters = useMemo(() => ['All', ...Object.values(ItemCategory), 'Free'], []);
  useEffect(() => {
    const load = async () => {
      if (!hasValidGroupId) {
        setGroup(null);
        setItems([]);
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      try {
        const [groupResult, itemList] = await Promise.all([groupsApi.getGroup(groupId), itemsApi.getItems(groupId)]);
        setGroup(groupResult);
        setItems(itemList);
      } catch (error) {
        console.error('Failed to fetch group feed', error);
        setGroup(null);
        setItems([]);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, [groupId, hasValidGroupId]);
  const deferredSearchTerm = useDeferredValue(searchTerm);
  const filteredItems = items.filter((item) => {
    const matchesFilter = activeFilter === 'All'
      || (activeFilter === 'Free' && (item.type === ItemType.GIVE || item.price === 0))
      || item.category === activeFilter;
    const normalizedSearchTerm = deferredSearchTerm.trim().toLowerCase();
    const matchesSearch = !normalizedSearchTerm
      || item.title.toLowerCase().includes(normalizedSearchTerm)
      || CATEGORY_LABELS[item.category]?.toLowerCase().includes(normalizedSearchTerm);
    const matchesSeller = activeSellerId === null || item.writerId === activeSellerId;
    return matchesFilter && matchesSearch && matchesSeller;
  });
  const handleLeaveGroup = async () => {
    if (!hasValidGroupId) return;
    setIsLeaving(true);
    try {
      await groupsApi.leaveGroup(groupId);
      setShowLeaveConfirm(false);
      navigate('/home');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to leave group.';
      alert(message);
    } finally {
      setIsLeaving(false);
    }
  };
  if (isLoading) {
    return <GroupFeedSkeleton />;
  }
  if (!group) {
    return <div className="p-8 text-center text-gray-500">Group not found</div>;
  }
  return (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto relative">
      <div className="fixed bottom-24 left-0 right-0 max-w-md mx-auto z-40 px-6 flex justify-end pointer-events-none">
        <button
          onClick={() => navigate(`/create?groupId=${groupId}`)}
          className="w-14 h-14 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg shadow-lg shadow-emerald-200 transition-colors flex items-center justify-center pointer-events-auto focus-visible:ring-2 focus-visible:ring-emerald-700 focus-visible:ring-offset-2"
          aria-label="Sell Item"
        >
          <Plus size={28} strokeWidth={2.5} />
        </button>
      </div>

      {showLeaveConfirm && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center px-6">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setShowLeaveConfirm(false)}
          ></div>
          <div className="bg-white w-full max-w-sm rounded-lg p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex flex-col items-center text-center">
              <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center mb-4 text-red-600">
                <LogOut size={24} strokeWidth={2.5} />
              </div>
              <h2 className="text-xl font-bold text-gray-900 mb-2">Leave Group?</h2>
              <p className="text-sm text-gray-500 mb-6 leading-relaxed">
                Are you sure you want to leave <span className="font-bold text-gray-900">{group.name}</span>?
              </p>
              <div className="flex space-x-3 w-full">
                <button
                  onClick={() => setShowLeaveConfirm(false)}
                  className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-lg hover:bg-gray-200 transition-colors focus-visible:ring-2 focus-visible:ring-gray-500"
                >
                  Cancel
                </button>
                <button
                  onClick={handleLeaveGroup}
                  disabled={isLeaving}
                  className="flex-1 py-3 bg-red-600 text-white font-bold rounded-lg hover:bg-red-700 shadow-lg shadow-red-200 transition-colors disabled:opacity-60 focus-visible:ring-2 focus-visible:ring-red-700"
                >
                  {isLeaving ? 'Leaving...' : 'Leave'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {showInfo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-6">
          <div
            className="absolute inset-0 bg-black/40 backdrop-blur-sm"
            onClick={() => setShowInfo(false)}
          ></div>
          <div className="bg-white w-full max-w-xs rounded-lg p-6 shadow-2xl relative z-10">
            <button
              onClick={() => setShowInfo(false)}
              className="absolute top-4 right-4 p-2 bg-gray-100 rounded-lg text-gray-500 hover:bg-gray-200 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
              aria-label="Close group information"
            >
              <X size={16} />
            </button>

            <div className="flex flex-col items-center text-center">
              <div className="w-20 h-20 rounded-lg overflow-hidden mb-4 shadow-md border-2 border-white">
                <ImageWithFallback src={group.profileImageUrl} alt={group.name} className="w-full h-full object-cover" />
              </div>
              <h2 className="text-lg font-bold text-gray-900 mb-1">{group.name}</h2>
              <span className="bg-emerald-100 text-emerald-700 text-[10px] font-bold px-2 py-0.5 rounded-lg mb-5">
                {group.memberCount ?? 0} Members
              </span>

              <div className="bg-gray-50 rounded-lg p-4 w-full text-left">
                <h3 className="text-xs font-bold text-gray-400 uppercase mb-2">About Group</h3>
                <p className="text-sm text-gray-600 leading-relaxed">
                  {group.description || 'This is a private group for sharing items.'}
                </p>
              </div>

              <button
                onClick={() => {
                  setShowInfo(false);
                  setShowLeaveConfirm(true);
                }}
                className="w-full mt-6 py-3 bg-red-50 text-red-600 font-bold rounded-lg hover:bg-red-100 transition-colors flex items-center justify-center space-x-2 focus-visible:ring-2 focus-visible:ring-red-600"
              >
                <LogOut size={18} />
                <span>Leave Group</span>
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="bg-white sticky top-0 z-20 shadow-sm">
        <div className="px-4 py-4 flex items-center justify-between">
          <button
            onClick={() => navigate('/home')}
            className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
            aria-label="Go back"
          >
            <ArrowLeft size={24} />
          </button>
          <div className="text-center">
            <h1 className="font-bold text-gray-900 truncate max-w-[200px]">{group.name}</h1>
            <p className="text-xs text-gray-500">{group.memberCount ?? 0} Members</p>
          </div>
          <button
            onClick={() => setShowInfo(true)}
            className="p-2 -mr-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
            aria-label="Open group information"
          >
            <div className="w-6 h-6 flex items-center justify-center border border-gray-400 rounded-full text-[10px] font-bold">i</div>
          </button>
        </div>

        <div className="px-4 pb-4 space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="text"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder={`Search items in ${group.name.split(' ')[0]}...`}
              className="w-full bg-gray-100 text-gray-800 text-sm rounded-lg py-2.5 pl-10 pr-11 focus:outline-none focus:ring-2 focus:ring-emerald-600"
            />
            {searchTerm ? (
              <button
                type="button"
                onClick={() => setSearchTerm('')}
                className="absolute right-3 top-2.5 text-gray-500 hover:text-gray-800 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 rounded-lg"
                aria-label="Clear search"
              >
                <X size={18} />
              </button>
            ) : (
              <SlidersHorizontal className="absolute right-3 top-2.5 text-gray-400" size={18} aria-hidden="true" />
            )}
          </div>

          <div className="flex space-x-2 overflow-x-auto no-scrollbar pb-1">
            {filters.map((filter) => (
              <button
                key={filter}
                onClick={() => setActiveFilter(filter)}
                className={`px-4 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${activeFilter === filter
                  ? 'bg-emerald-600 text-white shadow-md shadow-emerald-200'
                  : 'bg-white text-gray-600 border border-gray-200 hover:border-emerald-300'
                  }`}
              >
                {filter === 'All' || filter === 'Free' ? filter : CATEGORY_LABELS[filter as ItemCategory]}
              </button>
            ))}
          </div>

          <SellerFilterBar items={items} activeSellerId={activeSellerId} onChange={setActiveSellerId} />
        </div>
      </div>

      <div className="p-4 grid grid-cols-2 gap-4">
        {filteredItems.length > 0 ? (
          filteredItems.map((item) => (
            <div
              key={item.id}
              onClick={() => navigate(`/item/${item.id}`)}
              className="bg-white rounded-lg overflow-hidden shadow-sm border border-gray-200 hover:border-emerald-300 hover:shadow-md transition-colors cursor-pointer group"
            >
              <div className="relative aspect-square overflow-hidden bg-gray-100">
                <ImageWithFallback
                  src={item.thumbnailUrl}
                  alt={item.title}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
                <div className="absolute top-2 left-2">
                  {item.type === ItemType.GIVE ? (
                    <span className="bg-emerald-600 text-white text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wide">Free</span>
                  ) : (
                    <span className="bg-white/90 text-gray-900 text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wide">Sale</span>
                  )}
                </div>
                {item.status !== ItemStatus.ON_SALE && (
                  <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                    <span className="text-white font-bold border-2 border-white px-3 py-1 rounded-lg transform -rotate-12">
                      {item.status}
                    </span>
                  </div>
                )}
              </div>
              <div className="p-3">
                <h3 className="font-medium text-gray-900 text-sm truncate mb-1">{item.title}</h3>
                <span className={`font-bold text-sm ${item.type === ItemType.GIVE ? 'text-emerald-600' : 'text-gray-900'}`}>
                  {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                </span>
                <div className="mt-2 text-[10px] text-gray-400 flex items-center justify-between">
                  <span className="truncate">{item.writerNickname || item.postedAt || 'Just now'}</span>
                  <span>{item.likesCount ?? 0} likes</span>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="col-span-2 flex flex-col items-center justify-center py-24 text-center">
            <div className="w-20 h-20 bg-gray-50 rounded-lg flex items-center justify-center text-gray-400 mb-6">
              <Package size={40} strokeWidth={1.5} />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">No items here yet</h3>
            <p className="text-sm text-gray-500 max-w-[220px] mx-auto leading-relaxed">
              New listings in this group will appear here.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default GroupFeed;
