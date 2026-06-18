import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ShieldCheck } from 'lucide-react';

const prohibitedExamples = [
  'Weapons, illegal drugs, stolen goods, counterfeit items, and regulated products.',
  'Listings that request off-platform payment, deposits, passwords, or verification codes.',
  'Items that hide material defects, ownership issues, or unsafe pickup instructions.',
];

const safeTradeGuidance = [
  'Use a public, well-lit pickup place and prefer daytime meetup windows.',
  'Inspect the item before payment and keep chat/payment evidence until completion.',
  'Report suspicious listings or sellers; block a seller when you do not want further interactions.',
];

const MarketplaceItemPolicy: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col min-h-screen bg-white max-w-md mx-auto relative">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button
          onClick={() => navigate(-1)}
          className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
          aria-label="Go back"
        >
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Marketplace Item Policy</h1>
      </div>

      <div className="p-6 space-y-6 text-gray-600 text-sm leading-relaxed">
        <section className="rounded-lg border border-emerald-100 bg-emerald-50 p-4">
          <div className="flex items-center gap-2 text-emerald-800">
            <ShieldCheck size={20} />
            <h2 className="font-bold text-base">Before you post or reserve</h2>
          </div>
          <p className="mt-2">
            Knock warns about risky listings before posting, but users are responsible for keeping trades legal,
            transparent, and safe.
          </p>
        </section>

        <section>
          <h2 className="text-gray-900 font-bold text-base mb-2">Prohibited or high-risk listings</h2>
          <ul className="space-y-2 list-disc pl-5">
            {prohibitedExamples.map((item) => <li key={item}>{item}</li>)}
          </ul>
        </section>

        <section>
          <h2 className="text-gray-900 font-bold text-base mb-2">Safe meetup guidance</h2>
          <ul className="space-y-2 list-disc pl-5">
            {safeTradeGuidance.map((item) => <li key={item}>{item}</li>)}
          </ul>
        </section>

        <section>
          <h2 className="text-gray-900 font-bold text-base mb-2">What happens after a report or block?</h2>
          <p>
            Public item pages stay visible, but new reservations, bookmarks, reviews, and notification-producing
            interactions are restricted between blocked members. Reports are private from the reported seller.
          </p>
        </section>

        <div className="pt-10 pb-6 text-xs text-gray-400 border-t border-gray-50">
          Last updated: June 18, 2026 · Policy version 2026-06-18.p0
        </div>
      </div>
    </div>
  );
};

export default MarketplaceItemPolicy;
