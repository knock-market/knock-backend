import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, User, AtSign, ArrowRight, Loader2, AlertCircle, CheckCircle } from 'lucide-react';
import { authApi } from '../services';

const Signup: React.FC = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        name: '',
        nickname: '',
    });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSignup = async (e: React.FormEvent) => {
        e.preventDefault();
        const { email, password, name, nickname } = formData;
        if (!email || !password || !name || !nickname) return;

        setIsLoading(true);
        setError(null);

        try {
            await authApi.signup({
                email,
                password,
                name,
                nickname,
                profileImageUrl: '' // Optional for now
            });

            // Auto login after signup
            await authApi.emailLogin({ email, password });
            navigate('/home');
        } catch (err: any) {
            console.error("Signup failed:", err);
            setError(err.response?.data?.message || 'Failed to create account. Please check your information and try again.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-white max-w-md mx-auto relative overflow-hidden">
            {/* Background Decor */}
            <div className="absolute bottom-0 left-0 w-64 h-64 bg-amber-50 rounded-tr-full -ml-20 -mb-20 opacity-60"></div>

            <div className="flex-1 flex flex-col px-8 pt-16 z-10 pb-10">
                <button
                    onClick={() => navigate('/')}
                    className="w-10 h-10 bg-gray-50 rounded-full flex items-center justify-center text-gray-400 mb-6"
                >
                    <ArrowRight className="rotate-180" size={20} />
                </button>

                <h1 className="text-3xl font-bold text-gray-900 mb-2">Create Account</h1>
                <p className="text-gray-500 mb-8">Join the trust-based marketplace today.</p>

                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-600 p-4 rounded-2xl mb-6 flex items-start space-x-3 animate-in fade-in slide-in-from-top-2">
                        <AlertCircle size={20} className="mt-0.5 flex-shrink-0" />
                        <p className="text-sm font-medium">{error}</p>
                    </div>
                )}

                <form onSubmit={handleSignup} className="space-y-4">
                    {/* Full Name */}
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Full Name</label>
                        <div className="relative group">
                            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 group-focus-within:text-emerald-500 transition-colors">
                                <User size={18} />
                            </div>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="Alex Thompson"
                                className="w-full bg-gray-50 border border-gray-100 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 rounded-2xl py-4 pl-12 pr-4 text-gray-900 placeholder-gray-400 outline-none transition-all"
                                required
                            />
                        </div>
                    </div>

                    {/* Nickname */}
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Community Nickname</label>
                        <div className="relative group">
                            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 group-focus-within:text-emerald-500 transition-colors">
                                <AtSign size={18} />
                            </div>
                            <input
                                type="text"
                                name="nickname"
                                value={formData.nickname}
                                onChange={handleChange}
                                placeholder="GreenTrader"
                                className="w-full bg-gray-50 border border-gray-100 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 rounded-2xl py-4 pl-12 pr-4 text-gray-900 placeholder-gray-400 outline-none transition-all"
                                required
                            />
                        </div>
                    </div>

                    {/* Email */}
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Email Address</label>
                        <div className="relative group">
                            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 group-focus-within:text-emerald-500 transition-colors">
                                <Mail size={18} />
                            </div>
                            <input
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="you@email.com"
                                className="w-full bg-gray-50 border border-gray-100 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 rounded-2xl py-4 pl-12 pr-4 text-gray-900 placeholder-gray-400 outline-none transition-all"
                                required
                            />
                        </div>
                    </div>

                    {/* Password */}
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Password</label>
                        <div className="relative group">
                            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 group-focus-within:text-emerald-500 transition-colors">
                                <Lock size={18} />
                            </div>
                            <input
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                placeholder="Min. 8 characters"
                                className="w-full bg-gray-50 border border-gray-100 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 rounded-2xl py-4 pl-12 pr-4 text-gray-900 placeholder-gray-400 outline-none transition-all"
                                required
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading || !formData.email || !formData.password || !formData.name || !formData.nickname}
                        className="w-full bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold py-4 rounded-2xl shadow-lg shadow-emerald-200 transition-all active:scale-[0.98] mt-6 flex items-center justify-center space-x-2"
                    >
                        {isLoading ? (
                            <>
                                <Loader2 size={20} className="animate-spin" />
                                <span>Creating account...</span>
                            </>
                        ) : (
                            <span>Create Account</span>
                        )}
                    </button>
                </form>

                <p className="text-center text-xs text-gray-400 mt-6 px-4">
                    By signing up, you agree to our <span className="underline font-medium">Terms</span> & <span className="underline font-medium">Privacy Policy</span>.
                </p>

                <p className="text-center text-gray-500 mt-8">
                    Already have an account?{' '}
                    <Link to="/login" className="text-emerald-600 font-bold hover:underline">Log In</Link>
                </p>
            </div>
        </div>
    );
};

export default Signup;
