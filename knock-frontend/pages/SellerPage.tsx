import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Copy, Globe2, Link2Off, Package, Plus, Share2, UserRound, X } from 'lucide-react';
import ImageWithFallback from '../components/ImageWithFallback';
import { authApi, itemsApi, sellerShareApi } from '../services';
import {
  InviteDuration,
  ItemStatus,
  ItemSummaryResponseDto,
  ItemType,
  MemberResponseDto,
  SellerShareLinkSummaryResponseDto,
} from '../types';

type SellerMeta = {
  id: number;
  nickname: string;
  profileImageUrl?: string;
};

const SellerPage = () => {
  const { memberId } = useParams<{ memberId: string }>();
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const sellerId = memberId ? Number(memberId) : NaN;
  const hasValidSellerId = Boolean(token) || (Number.isInteger(sellerId) && sellerId > 0);

  const [items, setItems] = useState<ItemSummaryResponseDto[]>([]);
  const [me, setMe] = useState<MemberResponseDto | null>(null);
  const [sellerMeta, setSellerMeta] = useState<SellerMeta | null>(null);
  const [sharePath, setSharePath] = useState('');
  const [shareDuration, setShareDuration] = useState<InviteDuration>('ONE_HOUR');
  const [shareLinks, setShareLinks] = useState<SellerShareLinkSummaryResponseDto[]>([]);
  const [shareNotice, setShareNotice] = useState('');
  const [isSharing, setIsSharing] = useState(false);
  const [isShareModalOpen, setIsShareModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  useEffect(() => {
    const load = async () => {
      if (!hasValidSellerId) {
        setItems([]);
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setLoadError('');
      const [itemResult, meResult] = await Promise.allSettled([
        token ? sellerShareApi.getShop(token) : itemsApi.getSellerItems(sellerId),
        authApi.getOptionalMe(),
      ]);

      if (itemResult.status === 'fulfilled') {
        if (Array.isArray(itemResult.value)) {
          setItems(itemResult.value);
          setSellerMeta(null);
        } else {
          setItems(itemResult.value.items);
          setSellerMeta({
            id: itemResult.value.sellerId,
            nickname: itemResult.value.sellerNickname || itemResult.value.sellerName,
            profileImageUrl: itemResult.value.sellerProfileImageUrl,
          });
        }
      } else {
        setItems([]);
        setSellerMeta(null);
        setLoadError(token ? 'This share link is no longer available.' : 'Seller not found.');
      }
      setMe(meResult.status === 'fulfilled' ? meResult.value : null);
      setIsLoading(false);
    };

    load();
  }, [hasValidSellerId, sellerId, token]);

  const seller = items.find((item) => item.writerId === sellerId) || items[0];
  const resolvedSellerId = token ? sellerMeta?.id || seller?.writerId : sellerId;
  const isOwnPage = me?.id === resolvedSellerId && !token;
  const sellerName = sellerMeta?.nickname || seller?.writerNickname || (isOwnPage ? me?.nickname : undefined)
    || `Seller #${resolvedSellerId || sellerId}`;
  const subtitle = isOwnPage
    ? 'Your public shelf, ready to share'
    : 'A personal shelf shared by this seller';
  const targetPath = useMemo(
    () => (item: ItemSummaryResponseDto) => isOwnPage ? `/manage-item/${item.id}` : `/item/${item.publicId}`,
    [isOwnPage]
  );

  const shareOptions: { value: InviteDuration; label: string }[] = [
    { value: 'ONE_HOUR', label: '1 hour' },
    { value: 'ONE_DAY', label: '24 hours' },
    { value: 'PERMANENT', label: 'No limit' },
  ];
  const currentShareLink = shareLinks[0];
  const isCurrentShareExpired = currentShareLink?.expiresAt
    ? new Date(currentShareLink.expiresAt).getTime() < Date.now()
    : false;
  const isGeneralAccessEnabled = Boolean(currentShareLink?.active && !isCurrentShareExpired);

  const buildAbsoluteShareUrl = (path: string) =>
    `${window.location.origin}${window.location.pathname}#${path}`;

  const refreshShareLinks = async () => {
    if (!isOwnPage) return;
    const links = await sellerShareApi.getMyLinks();
    setShareLinks(links);
  };

  useEffect(() => {
    refreshShareLinks().catch(() => setShareLinks([]));
  }, [isOwnPage]);

  const createShareLink = async () => {
    setIsSharing(true);
    setShareNotice('');
    try {
      const result = await sellerShareApi.create(shareDuration);
      const nextPath = buildAbsoluteShareUrl(result.path);
      setSharePath(nextPath);
      await navigator.clipboard.writeText(nextPath);
      setShareNotice('Link access is on. The link was copied.');
      await refreshShareLinks();
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to create share link.';
      setShareNotice(message);
    } finally {
      setIsSharing(false);
    }
  };

  const stopSharing = async (tokenValue: string) => {
    setIsSharing(true);
    setShareNotice('');
    try {
      await sellerShareApi.deactivate(tokenValue);
      await refreshShareLinks();
      setSharePath('');
      setShareNotice('Link access is now restricted.');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to restrict link access.';
      setShareNotice(message);
    } finally {
      setIsSharing(false);
    }
  };

  const copyShareLink = async (path: string) => {
    try {
      const nextPath = buildAbsoluteShareUrl(path);
      setSharePath(nextPath);
      await navigator.clipboard.writeText(nextPath);
      setShareNotice('Link copied.');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to copy link.';
      setShareNotice(message);
    }
  };

  const formatExpiry = (expiresAt?: string) => {
    if (!expiresAt) return 'No limit';
    return new Date(expiresAt).toLocaleString();
  };

  const handlePrimaryShareAction = async () => {
    if (isGeneralAccessEnabled && currentShareLink) {
      await copyShareLink(currentShareLink.path);
      return;
    }

    await createShareLink();
  };

  if (isLoading) {
    return <div className="min-h-screen bg-gray-50 flex items-center justify-center text-gray-500">Loading shelf...</div>;
  }

  if (!hasValidSellerId) {
    return <div className="p-8 text-center text-gray-500">Seller not found</div>;
  }

  if (loadError) {
    return (
      <div className="bg-white min-h-screen max-w-md mx-auto">
        <div className="px-4 py-5 border-b border-gray-100">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="p-2 -ml-2 rounded-lg text-gray-600 transition-colors hover:bg-gray-100 focus-visible:ring-2 focus-visible:ring-emerald-600"
            aria-label="Go back"
          >
            <ArrowLeft size={24} />
          </button>
        </div>
        <div className="flex min-h-[60vh] flex-col items-center justify-center px-6 text-center">
          <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-lg border border-gray-100 bg-gray-50 text-gray-400">
            <Link2Off size={36} strokeWidth={1.6} />
          </div>
          <h1 className="text-xl font-bold text-gray-900">Link unavailable</h1>
          <p className="mt-3 max-w-[260px] text-sm leading-relaxed text-gray-500">{loadError}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto">
      <div className={`px-4 py-5 sticky top-0 z-20 ${isOwnPage ? 'bg-emerald-700 text-white' : 'bg-white text-gray-900 border-b border-gray-100'}`}>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className={`p-2 -ml-2 rounded-lg transition-colors focus-visible:ring-2 ${isOwnPage ? 'hover:bg-white/10 focus-visible:ring-white' : 'hover:bg-gray-100 focus-visible:ring-emerald-600'}`}
          aria-label="Go back"
        >
          <ArrowLeft size={24} />
        </button>
        <div className="mt-6 flex items-center justify-between gap-4">
          <div>
            <p className={`text-xs font-semibold uppercase tracking-wide ${isOwnPage ? 'text-emerald-100' : 'text-emerald-700'}`}>
              {isOwnPage ? 'My shelf' : 'Shared shelf'}
            </p>
            <h1 className="mt-1 text-2xl font-bold leading-tight">{sellerName}</h1>
            <p className={`mt-2 text-sm ${isOwnPage ? 'text-emerald-50' : 'text-gray-500'}`}>{subtitle}</p>
          </div>
          {isOwnPage && (
            <div className="flex shrink-0 flex-col gap-2">
              <button
                type="button"
                onClick={() => navigate('/create')}
                className="inline-flex items-center gap-2 rounded-lg bg-white px-3 py-2 text-sm font-bold text-emerald-800 shadow-sm focus-visible:ring-2 focus-visible:ring-white"
              >
                <Plus size={16} />
                Add
              </button>
              <button
                type="button"
                onClick={() => setIsShareModalOpen(true)}
                className="inline-flex items-center gap-2 rounded-lg bg-emerald-900/40 px-3 py-2 text-sm font-bold text-white focus-visible:ring-2 focus-visible:ring-white"
              >
                <Share2 size={16} />
                Share
              </button>
            </div>
          )}
        </div>
      </div>

      {isOwnPage && isShareModalOpen && (
        <div className="fixed inset-0 z-[80] flex items-end justify-center sm:items-center">
          <button
            type="button"
            aria-label="Close share settings"
            onClick={() => setIsShareModalOpen(false)}
            className="absolute inset-0 bg-black/50"
          />
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="share-shelf-title"
            className="relative max-h-[88vh] w-full max-w-md overflow-y-auto rounded-t-2xl bg-white p-5 shadow-2xl sm:rounded-2xl"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs font-bold uppercase tracking-wide text-emerald-700">Share shelf</p>
                <h2 id="share-shelf-title" className="mt-1 text-xl font-bold text-gray-900">Share "{sellerName}"</h2>
                <p className="mt-2 text-sm leading-relaxed text-gray-500">
                  Manage one public link for people who already know you.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setIsShareModalOpen(false)}
                className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 focus-visible:ring-2 focus-visible:ring-emerald-600"
                aria-label="Close"
              >
                <X size={20} />
              </button>
            </div>

            <div className="mt-6">
              <p className="text-sm font-bold text-gray-900">People with access</p>
              <div className="mt-3 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-full bg-gray-100 text-gray-500">
                  {me?.profileImageUrl ? (
                    <img src={me.profileImageUrl} alt="" className="h-full w-full object-cover" />
                  ) : (
                    <UserRound size={20} />
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-bold text-gray-900">{me?.nickname || sellerName} (you)</p>
                  <p className="truncate text-xs text-gray-500">{me?.email || 'Shelf owner'}</p>
                </div>
                <span className="text-xs font-semibold text-gray-400">Owner</span>
              </div>
            </div>

            <div className="mt-6">
              <p className="text-sm font-bold text-gray-900">General access</p>
              <div className="mt-3 flex gap-3 rounded-lg bg-gray-50 px-3 py-4">
                <div className={`flex h-10 w-10 items-center justify-center rounded-full ${isGeneralAccessEnabled ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-200 text-gray-500'}`}>
                  <Globe2 size={20} />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-bold text-gray-900">
                      {isGeneralAccessEnabled ? 'Anyone with the link' : 'Restricted'}
                    </p>
                    {currentShareLink && (
                      <span className={`rounded-md px-2 py-1 text-[10px] font-bold ${isGeneralAccessEnabled ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-200 text-gray-600'}`}>
                        {isGeneralAccessEnabled ? 'Active' : 'Off'}
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-xs leading-relaxed text-gray-500">
                    {isGeneralAccessEnabled
                      ? 'People with this link can view your public shelf.'
                      : 'Only you can access this shelf link right now.'}
                  </p>

                  <div className="mt-4">
                    <p className="mb-2 text-[11px] font-bold uppercase tracking-wide text-gray-500">
                      Link expires after
                    </p>
                    <div className="grid grid-cols-3 gap-2">
                      {shareOptions.map((option) => (
                        <button
                          key={option.value}
                          type="button"
                          onClick={() => setShareDuration(option.value)}
                          className={`rounded-lg border px-2 py-2 text-[11px] font-bold transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${shareDuration === option.value ? 'border-emerald-700 bg-white text-emerald-800' : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300'}`}
                        >
                          {option.label}
                        </button>
                      ))}
                    </div>
                  </div>

                  {currentShareLink && (
                    <dl className="mt-4 grid grid-cols-3 gap-2 text-xs">
                      <div>
                        <dt className="text-gray-500">Clicks</dt>
                        <dd className="mt-1 font-bold text-gray-900">{currentShareLink.clickCount}</dd>
                      </div>
                      <div>
                        <dt className="text-gray-500">Used</dt>
                        <dd className="mt-1 font-bold text-gray-900">{currentShareLink.useCount}</dd>
                      </div>
                      <div>
                        <dt className="text-gray-500">Expires</dt>
                        <dd className="mt-1 truncate font-bold text-gray-900">{formatExpiry(currentShareLink.expiresAt)}</dd>
                      </div>
                    </dl>
                  )}
                </div>
              </div>
            </div>

            {(shareNotice || sharePath) && (
              <div className="mt-4 rounded-lg border border-emerald-100 bg-emerald-50 px-3 py-2">
                {shareNotice && <p className="text-xs font-semibold text-emerald-800">{shareNotice}</p>}
                {sharePath && <p className="mt-1 break-all text-[11px] text-emerald-700">{sharePath}</p>}
              </div>
            )}

            <div className="mt-6 flex items-center justify-between gap-3">
              {isGeneralAccessEnabled && currentShareLink ? (
                <button
                  type="button"
                  onClick={() => stopSharing(currentShareLink.token)}
                  disabled={isSharing}
                  className="inline-flex items-center justify-center gap-2 rounded-lg border border-red-100 px-4 py-3 text-sm font-bold text-red-700 transition-colors hover:bg-red-50 focus-visible:ring-2 focus-visible:ring-red-500"
                >
                  <Link2Off size={16} />
                  Restrict
                </button>
              ) : (
                <span />
              )}
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={handlePrimaryShareAction}
                  disabled={isSharing}
                  className="inline-flex items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-3 text-sm font-bold text-gray-800 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:text-gray-400 focus-visible:ring-2 focus-visible:ring-emerald-600"
                >
                  <Copy size={16} />
                  {isSharing ? 'Creating...' : isGeneralAccessEnabled ? 'Copy link' : 'Turn on and copy link'}
                </button>
                <button
                  type="button"
                  onClick={() => setIsShareModalOpen(false)}
                  className="rounded-lg bg-blue-600 px-5 py-3 text-sm font-bold text-white transition-colors hover:bg-blue-700 focus-visible:ring-2 focus-visible:ring-blue-600"
                >
                  Done
                </button>
              </div>
            </div>
          </section>
        </div>
      )}

      <div className="p-4 grid grid-cols-2 gap-4">
        {items.length > 0 ? (
          items.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => navigate(targetPath(item))}
              className="text-left bg-white rounded-lg overflow-hidden shadow-sm border border-gray-200 hover:border-emerald-300 hover:shadow-md transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
            >
              <div className="relative aspect-square bg-gray-100 overflow-hidden">
                <ImageWithFallback src={item.thumbnailUrl} alt={item.title} className="w-full h-full object-cover" />
                {item.status !== ItemStatus.ON_SALE && (
                  <span className="absolute top-2 left-2 bg-gray-900 text-white text-[10px] font-bold px-2 py-1 rounded-md">
                    {item.status}
                  </span>
                )}
              </div>
              <div className="p-3">
                <h2 className="font-semibold text-sm text-gray-900 truncate">{item.title}</h2>
                <p className={`mt-1 text-sm font-bold ${item.type === ItemType.GIVE ? 'text-emerald-700' : 'text-gray-900'}`}>
                  {item.price === 0 ? 'Free' : `₩${item.price.toLocaleString()}`}
                </p>
                <p className="mt-2 text-[10px] text-gray-500 truncate">
                  {item.tradeLocationName || item.tradeLocationAddress || 'Pickup by arrangement'}
                </p>
              </div>
            </button>
          ))
        ) : (
          <div className="col-span-2 flex flex-col items-center justify-center py-24 text-center">
            <div className="w-20 h-20 bg-white rounded-lg flex items-center justify-center text-gray-400 mb-6 border border-gray-100">
              <Package size={40} strokeWidth={1.5} />
            </div>
            <h2 className="text-lg font-bold text-gray-900 mb-2">No items yet</h2>
            <p className="text-sm text-gray-500 max-w-[220px] mx-auto leading-relaxed">
              {isOwnPage ? 'Post your first item before sharing your shelf.' : 'This shared shelf has no public items right now.'}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default SellerPage;
