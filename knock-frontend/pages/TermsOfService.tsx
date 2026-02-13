import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

const TermsOfService: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="flex flex-col min-h-screen bg-white max-w-md mx-auto relative">
            <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
                <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
                    <ArrowLeft size={24} />
                </button>
                <h1 className="text-lg font-bold text-gray-900 ml-2">Terms of Service</h1>
            </div>

            <div className="p-6 space-y-6 text-gray-600 text-sm leading-relaxed">
                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">1. Introduction</h2>
                    <p>Welcome to Knock Market. By using our service, you agree to these terms. Please read them carefully.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">2. Using our Services</h2>
                    <p>You must follow any policies made available to you within the Services. Do not misuse our Services. For example, do not interfere with our Services or try to access them using a method other than the interface and the instructions that we provide.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">3. Your Content in our Services</h2>
                    <p>Our Services allow you to upload, submit, store, send or receive content. You retain ownership of any intellectual property rights that you hold in that content. In short, what belongs to you stays yours.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">4. Privacy Protection</h2>
                    <p>Knock Market's privacy policies explain how we treat your personal data and protect your privacy when you use our Services. By using our Services, you agree that Knock Market can use such data in accordance with our privacy policies.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">5. About Software in our Services</h2>
                    <p>When a Service requires or includes downloadable software, this software may update automatically on your device once a new version or feature is available.</p>
                </section>

                <section>
                    <h2 className="text-gray-900 font-bold text-base mb-2">6. Modifying and Terminating our Services</h2>
                    <p>We are constantly changing and improving our Services. We may add or remove functionalities or features, and we may suspend or stop a Service altogether.</p>
                </section>

                <div className="pt-10 pb-6 text-xs text-gray-400 border-t border-gray-50">
                    Last updated: February 10, 2026
                </div>
            </div>
        </div>
    );
};

export default TermsOfService;
