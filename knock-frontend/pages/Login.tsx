import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, ArrowRight, Loader2, AlertCircle } from 'lucide-react';
import { authApi } from '../services';

const resolveNextPath = (rawNext: string | null): string => {
    if (!rawNext) {
        return '/home';
    }
    const next = rawNext.trim();
    if (!next.startsWith('/') || next.startsWith('//')) {
        return '/home';
    }
    return next;
};

const Login: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const nextPath = resolveNextPath(new URLSearchParams(location.search).get('next'));

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!email || !password) return;

        setIsLoading(true);
        setError(null);

        try {
            await authApi.emailLogin({ email, password });
            navigate(nextPath, { replace: true });
        } catch (err) {
            console.error("Login failed:", err);
            setError(err instanceof Error ? err.message : 'Invalid email or password. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleGoogleLogin = () => {
        window.location.href = `/api/v1/auth/social/google/start?next=${encodeURIComponent(nextPath)}`;
    };

    return (
        <div className="flex flex-col min-h-screen bg-white max-w-md mx-auto relative overflow-hidden">
            {/* Background Decor */}
            <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-50 rounded-bl-full -mr-20 -mt-20 opacity-60"></div>

            <div className="flex-1 flex flex-col px-8 pt-20 z-10 relative">
                <button
                    onClick={() => navigate('/')}
                    className="w-10 h-10 bg-gray-50 rounded-full flex items-center justify-center text-gray-400 mb-8"
                >
                    <ArrowRight className="rotate-180" size={20} />
                </button>

                <h1 className="text-3xl font-bold text-gray-900 mb-2">Welcome back!</h1>
                <p className="text-gray-500 mb-10">Log in to reconnect with your community.</p>

                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-600 p-4 rounded-2xl mb-6 flex items-start space-x-3 animate-in fade-in slide-in-from-top-2">
                        <AlertCircle size={20} className="mt-0.5 flex-shrink-0" />
                        <p className="text-sm font-medium">{error}</p>
                    </div>
                )}

                <form onSubmit={handleLogin} className="space-y-4">
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Email Address</label>
                        <div className="relative group">
                            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 group-focus-within:text-emerald-500 transition-colors">
                                <Mail size={18} />
                            </div>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="you@email.com"
                                className="w-full bg-gray-50 border border-gray-100 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 rounded-2xl py-4 pl-12 pr-4 text-gray-900 placeholder-gray-400 outline-none transition-all"
                                required
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <div className="flex justify-between items-center ml-1">
                            <label className="text-sm font-semibold text-gray-700">Password</label>
                            <button
                                type="button"
                                onClick={() => alert('비밀번호 찾기 기능은 준비 중입니다.')}
                                className="text-xs font-bold text-emerald-600 hover:text-emerald-700"
                            >
                                Forgot Password?
                            </button>
                        </div>
                        <div className="relative group">
                            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 group-focus-within:text-emerald-500 transition-colors">
                                <Lock size={18} />
                            </div>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Min. 8 characters"
                                className="w-full bg-gray-50 border border-gray-100 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 rounded-2xl py-4 pl-12 pr-4 text-gray-900 placeholder-gray-400 outline-none transition-all"
                                required
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading || !email || !password}
                        className="w-full bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold py-4 rounded-2xl shadow-lg shadow-emerald-200 transition-all active:scale-[0.98] mt-4 flex items-center justify-center space-x-2"
                    >
                        {isLoading ? (
                            <>
                                <Loader2 size={20} className="animate-spin" />
                                <span>Logging in...</span>
                            </>
                        ) : (
                            <span>Log In</span>
                        )}
                    </button>

                    <button
                        type="button"
                        onClick={handleGoogleLogin}
                        className="w-full bg-white border border-gray-200 hover:border-gray-300 text-gray-800 font-semibold py-4 rounded-2xl transition-all active:scale-[0.98]"
                    >
                        Continue with Google
                    </button>
                </form>

                <div className="mt-6 text-center">
                    <p className="text-[11px] text-gray-400 leading-relaxed">
                        By logging in, you agree to our <br />
                        <Link to="/terms" className="underline hover:text-emerald-600 transition-colors">Terms of Service</Link> and <Link to="/privacy" className="underline hover:text-emerald-600 transition-colors">Privacy Policy</Link>
                    </p>
                </div>

                <p className="text-center text-gray-500 mt-auto pb-10">
                    Don't have an account?{' '}
                    <Link to="/signup" className="text-emerald-600 font-bold hover:underline">Sign Up</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;
