import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

const PrivacyPolicy: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="flex flex-col min-h-screen bg-white max-w-md mx-auto relative">
            <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
                <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
                    <ArrowLeft size={24} />
                </button>
                <h1 className="text-lg font-bold text-gray-900 ml-2">Privacy Policy</h1>
            </div>

            <div className="p-6 space-y-6 text-gray-600 text-sm leading-relaxed">
                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">1. Information We Collect</h2>
                    <p>We collect information to provide better services to all our users. This includes information you provide us (like your name, email address, and profile photo) and information we collect when you use our services (like your device information and location data if permitted).</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">2. How We Use Information</h2>
                    <p>We use the information we collect from all our services to provide, maintain, protect and improve them, to develop new ones, and to protect Knock Market and our users.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">3. Information We Share</h2>
                    <p>We do not share personal information with companies, organizations, or individuals outside of Knock Market unless one of the following circumstances applies: with your consent, for external processing, or for legal reasons.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">4. Information Security</h2>
                    <p>We work hard to protect Knock Market and our users from unauthorized access to or unauthorized alteration, disclosure or destruction of information we hold. We use encryption to keep your data private while in transit.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">5. Your Controls</h2>
                    <p>You have choices regarding the information we collect and how it's used. You can manage your profile information, manage your notification settings, and delete your account at any time.</p>
                </section>

                <div className="pt-10 pb-6 text-xs text-gray-400 border-t border-gray-50">
                    Last updated: February 10, 2026
                </div>
            </div>
        </div>
    );
};

export default PrivacyPolicy;
