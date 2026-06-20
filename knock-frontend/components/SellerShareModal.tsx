import { Copy, Globe2, Link2Off, UserRound, X } from 'lucide-react';
import { InviteDuration, MemberResponseDto, SellerShareLinkSummaryResponseDto } from '../types';

type SellerShareModalProps = {
  sellerName: string;
  me: MemberResponseDto | null;
  shareDuration: InviteDuration;
  shareNotice: string;
  sharePath: string;
  isSharing: boolean;
  isGeneralAccessEnabled: boolean;
  currentShareLink?: SellerShareLinkSummaryResponseDto;
  onClose: () => void;
  onDurationChange: (duration: InviteDuration) => void;
  onPrimaryShareAction: () => void;
  onStopSharing: (token: string) => void;
};

const shareOptions: { value: InviteDuration; label: string }[] = [
  { value: 'ONE_HOUR', label: '1 hour' },
  { value: 'ONE_DAY', label: '24 hours' },
  { value: 'PERMANENT', label: 'No limit' },
];

const formatExpiry = (expiresAt?: string) => {
  if (!expiresAt) return 'No limit';
  return new Date(expiresAt).toLocaleString();
};

const SellerShareModal = ({
  sellerName,
  me,
  shareDuration,
  shareNotice,
  sharePath,
  isSharing,
  isGeneralAccessEnabled,
  currentShareLink,
  onClose,
  onDurationChange,
  onPrimaryShareAction,
  onStopSharing,
}: SellerShareModalProps) => (
  <div className="fixed inset-0 z-[80] flex items-end justify-center sm:items-center">
    <button type="button" aria-label="Close share settings" onClick={onClose} className="absolute inset-0 bg-black/50" />
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
          onClick={onClose}
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
              <p className="mb-2 text-[11px] font-bold uppercase tracking-wide text-gray-500">Link expires after</p>
              <div className="grid grid-cols-3 gap-2">
                {shareOptions.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => onDurationChange(option.value)}
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
            onClick={() => onStopSharing(currentShareLink.token)}
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
            onClick={onPrimaryShareAction}
            disabled={isSharing}
            className="inline-flex items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-3 text-sm font-bold text-gray-800 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:text-gray-400 focus-visible:ring-2 focus-visible:ring-emerald-600"
          >
            <Copy size={16} />
            {isSharing ? 'Creating...' : isGeneralAccessEnabled ? 'Copy link' : 'Turn on and copy link'}
          </button>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg bg-blue-600 px-5 py-3 text-sm font-bold text-white transition-colors hover:bg-blue-700 focus-visible:ring-2 focus-visible:ring-blue-600"
          >
            Done
          </button>
        </div>
      </div>
    </section>
  </div>
);

export default SellerShareModal;
