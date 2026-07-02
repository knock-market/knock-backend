import React from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { Store } from 'lucide-react';
import { buildGoogleAuthStartUrl, buildLoginUrl, resolveAuthNextPath } from '../utils/authRedirect';

const Onboarding: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const nextPath = resolveAuthNextPath(new URLSearchParams(location.search).get('next'));
  const loginUrl = buildLoginUrl(nextPath);
  const signupUrl = `/signup?next=${encodeURIComponent(nextPath)}`;

  const handleKakaoLogin = () => {
    alert('Kakao login is not available yet. Please use email login.');
    navigate(loginUrl);
  };

  const handleGoogleLogin = () => {
    window.location.href = buildGoogleAuthStartUrl(nextPath);
  };

  return (
    <div className="flex flex-col h-screen bg-white max-w-md mx-auto relative overflow-hidden">
      <div className="absolute inset-x-0 top-0 h-56 bg-emerald-50"></div>
      <div className="absolute inset-x-0 bottom-0 h-24 bg-amber-50"></div>

      <div className="flex-1 flex flex-col items-center justify-center px-8 z-10 relative">
        <div className="bg-emerald-600 p-6 rounded-lg mb-8 shadow-xl shadow-emerald-100">
          <Store className="text-white w-12 h-12" strokeWidth={2.5} />
        </div>

        <h1 className="text-3xl font-bold text-gray-900 mb-2">Knock Market</h1>
        <p className="text-center text-gray-500 mb-12 px-4 leading-relaxed">
          Open your own selling shelf.<br />
          Share the link with people who already know you.
        </p>

        <div className="w-full space-y-4">
          <button
            onClick={() => navigate(loginUrl)}
            className="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-bold py-4 rounded-full flex items-center justify-center transition-all active:scale-[0.98] shadow-lg shadow-emerald-200"
          >
            <span>Log In</span>
          </button>

          <button
            onClick={() => navigate(signupUrl)}
            className="w-full bg-white border-2 border-emerald-500 text-emerald-600 font-bold py-4 rounded-full flex items-center justify-center transition-all active:scale-[0.98]"
          >
            <span>Sign Up</span>
          </button>

          <div className="flex items-center my-6">
            <div className="flex-1 h-px bg-gray-100"></div>
            <span className="px-4 text-xs font-bold text-gray-300 uppercase tracking-widest">or continue with</span>
            <div className="flex-1 h-px bg-gray-100"></div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <button
              onClick={handleKakaoLogin}
              className="bg-[#FEE500] hover:bg-[#FDD800] text-gray-900 font-bold py-3.5 rounded-2xl flex items-center justify-center transition-all active:scale-95 shadow-sm"
            >
              <span className="text-sm">Kakao</span>
            </button>

            <button
              onClick={handleGoogleLogin}
              className="bg-white border border-gray-100 hover:bg-gray-50 text-gray-700 font-bold py-3.5 rounded-2xl flex items-center justify-center transition-all active:scale-95 shadow-sm"
            >
              <span className="text-sm">Google</span>
            </button>
          </div>

        </div>

        <p className="mt-8 text-xs text-center text-gray-400 mb-6">
          By continuing, you agree to our<br />
          <Link to="/terms" className="underline font-medium hover:text-emerald-600">Terms of Service</Link> and <Link to="/privacy" className="underline font-medium hover:text-emerald-600">Privacy Policy</Link>.
        </p>
      </div>
    </div>
  );
};

export default Onboarding;
