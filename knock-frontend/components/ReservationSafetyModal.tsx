import { AlertCircle, ShieldCheck } from 'lucide-react';

type ReservationSafetyModalProps = {
  locationName?: string;
  locationAddress?: string;
  isSubmitting: boolean;
  onCancel: () => void;
  onConfirm: () => void;
};

const safeMeetupChecklist = [
  'Meet in a public, well-lit place during busy hours.',
  'Inspect the item in person before sending payment.',
  'Do not send advance transfers, deposits, passwords, or verification codes.',
  'Keep payment and meetup details inside Knock until the trade is complete.',
];

const ReservationSafetyModal = ({
  locationName,
  locationAddress,
  isSubmitting,
  onCancel,
  onConfirm,
}: ReservationSafetyModalProps) => {
  const pickupLabel = locationName || locationAddress || 'the selected pickup location';

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center px-6">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onCancel}></div>
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="reservation-safety-title"
        className="bg-white w-full max-w-sm rounded-lg p-6 relative z-10 shadow-2xl animate-in fade-in zoom-in duration-200"
      >
        <div className="flex flex-col items-center text-center">
          <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center mb-4 text-emerald-700">
            <AlertCircle size={24} strokeWidth={2.5} />
          </div>
          <h2 id="reservation-safety-title" className="text-xl font-bold text-gray-900 mb-2">
            Request Reservation?
          </h2>
          <p className="text-sm text-gray-500 mb-4 leading-relaxed">
            This will notify the seller that you are interested. Use {pickupLabel} only after confirming it feels safe.
          </p>
          <div className="w-full rounded-lg border border-emerald-100 bg-emerald-50 p-4 text-left mb-5">
            <div className="flex items-center gap-2 text-emerald-800">
              <ShieldCheck size={18} />
              <p className="text-sm font-bold">Safe meetup checklist</p>
            </div>
            <ul className="mt-3 space-y-2 text-xs leading-5 text-emerald-900">
              {safeMeetupChecklist.map((item) => (
                <li key={item} className="flex gap-2">
                  <span aria-hidden="true">•</span>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
          <div className="flex space-x-3 w-full">
            <button
              onClick={onCancel}
              className="flex-1 py-3 bg-gray-100 text-gray-700 font-bold rounded-lg hover:bg-gray-200 transition-colors focus-visible:ring-2 focus-visible:ring-gray-500"
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              disabled={isSubmitting}
              className="flex-1 py-3 bg-emerald-600 text-white font-bold rounded-lg hover:bg-emerald-700 shadow-lg shadow-emerald-200 transition-colors disabled:opacity-60 focus-visible:ring-2 focus-visible:ring-emerald-700"
            >
              {isSubmitting ? 'Submitting...' : 'Confirm'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReservationSafetyModal;
