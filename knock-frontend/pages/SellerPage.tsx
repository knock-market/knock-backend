import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Link2Off, Package, Plus, Share2 } from 'lucide-react';
import ListingFilterBar from '../components/ListingFilterBar';
import ListingPagination from '../components/ListingPagination';
import SellerShareModal from '../components/SellerShareModal';
import SellerShelfItemCard from '../components/SellerShelfItemCard';
import { authApi, itemsApi, sellerShareApi } from '../services';
import { InviteDuration, ItemSummaryResponseDto, MemberResponseDto, SellerShareLinkSummaryResponseDto } from '../types';
import {
  hasActiveListingFilters,
  ListingFilters,
  readListingFilters,
  toListingQueryParams,
  toSearchParams,
} from '../utils/listingFilters';

type SellerMeta = { id: number; nickname: string; profileImageUrl?: string; };

const SellerPage = () => {
  const { memberId } = useParams<{ memberId: string }>();
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const filters = readListingFilters(searchParams);
  const activeFilters = hasActiveListingFilters(filters);
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
        token ? sellerShareApi.getShop(token, toListingQueryParams(filters))
          : itemsApi.getSellerItems(sellerId, toListingQueryParams(filters)),
        authApi.getMe(),
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
  }, [hasValidSellerId, searchParams, sellerId, token]);

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

  const applyFilters = (nextFilters: ListingFilters) => {
    setSearchParams(toSearchParams({ ...nextFilters, page: 0 }));
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
        <SellerShareModal
          sellerName={sellerName}
          me={me}
          shareDuration={shareDuration}
          shareNotice={shareNotice}
          sharePath={sharePath}
          isSharing={isSharing}
          isGeneralAccessEnabled={isGeneralAccessEnabled}
          currentShareLink={currentShareLink}
          onClose={() => setIsShareModalOpen(false)}
          onDurationChange={setShareDuration}
          onPrimaryShareAction={handlePrimaryShareAction}
          onStopSharing={stopSharing}
        />
      )}

      <section className="px-4 pt-4">
        <ListingFilterBar filters={filters} onApply={applyFilters} />
      </section>

      <div className="p-4 grid grid-cols-2 gap-4">
        {items.length > 0 ? (
          items.map((item) => (
            <div key={item.id}>
              <SellerShelfItemCard
                onClick={() => navigate(targetPath(item))}
                item={item}
              />
            </div>
          ))
        ) : (
          <div className="col-span-2 flex flex-col items-center justify-center py-24 text-center">
            <div className="w-20 h-20 bg-white rounded-lg flex items-center justify-center text-gray-400 mb-6 border border-gray-100">
              <Package size={40} strokeWidth={1.5} />
            </div>
            <h2 className="text-lg font-bold text-gray-900 mb-2">No items yet</h2>
            <p className="text-sm text-gray-500 max-w-[220px] mx-auto leading-relaxed">
              {activeFilters
                ? 'No shelf items match these filters.'
                : isOwnPage ? 'Post your first item before sharing your shelf.' : 'This shared shelf has no public items right now.'}
            </p>
          </div>
        )}
      </div>

      {items.length > 0 && <ListingPagination filters={filters} itemCount={items.length} onChange={setSearchParams} />}
    </div>
  );
};

export default SellerPage;
