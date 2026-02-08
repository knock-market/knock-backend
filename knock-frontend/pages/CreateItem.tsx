import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, Camera, Image as ImageIcon, ChevronDown, X, Loader2 } from 'lucide-react';
import { itemsApi, imagesApi, groupsApi } from '../services';
import { GroupResponseDto } from '../types';

const CreateItem: React.FC = () => {
    const navigate = useNavigate();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [transactionType, setTransactionType] = useState<'free' | 'sale'>('free');
    const [isAutoReserve, setIsAutoReserve] = useState(true);
    const [price, setPrice] = useState<string>('1000');

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [category, setCategory] = useState('');
    const [groups, setGroups] = useState<GroupResponseDto[]>([]);
    const [groupId, setGroupId] = useState('');
    const [imageUrls, setImageUrls] = useState<string[]>([]);
    const [isUploading, setIsUploading] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const preSelectedGroupId = queryParams.get('groupId');

    useEffect(() => {
        groupsApi.getMyGroups()
            .then(res => {
                const fetchedGroups = res.data || [];
                setGroups(fetchedGroups);

                // Priority: 1. URL parameter, 2. First group in list
                if (preSelectedGroupId) {
                    setGroupId(preSelectedGroupId);
                } else if (fetchedGroups.length > 0) {
                    setGroupId(String(fetchedGroups[0].id));
                }
            })
            .catch(err => {
                console.error("Failed to fetch groups:", err);
            });
    }, [preSelectedGroupId]);

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (!files || files.length === 0) return;

        setIsUploading(true);
        try {
            const filesArray = Array.from(files) as File[];
            const uploadPromises = filesArray.map(file => imagesApi.upload(file));
            const results = await Promise.all(uploadPromises);
            const newUrls = results.map((res: any) => res.data.imageUrl);
            setImageUrls(prev => [...prev, ...newUrls].slice(0, 5));
        } catch (err) {
            console.error("Upload failed:", err);
            alert("Failed to upload image.");
        } finally {
            setIsUploading(false);
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    const removeImage = (url: string) => {
        setImageUrls(prev => prev.filter(u => u !== url));
    };

    return (
        <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
            <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
                <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
                    <ArrowLeft size={24} />
                </button>
                <h1 className="text-lg font-bold text-gray-900 ml-2">Register Item</h1>
            </div>

            <div className="p-6 space-y-8">
                {/* Photo Upload */}
                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-3">Photos ({imageUrls.length}/5)</label>
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        multiple
                        accept="image/*"
                        className="hidden"
                    />
                    <div className="flex space-x-3 overflow-x-auto no-scrollbar py-2">
                        <button
                            onClick={() => fileInputRef.current?.click()}
                            disabled={isUploading || imageUrls.length >= 5}
                            className={`flex-shrink-0 w-20 h-20 rounded-2xl border-2 border-dashed flex flex-col items-center justify-center transition-colors ${isUploading ? 'bg-gray-50 border-gray-200 text-gray-400' : 'border-emerald-300 text-emerald-500 bg-emerald-50 hover:bg-emerald-100'
                                }`}
                        >
                            {isUploading ? (
                                <Loader2 size={24} className="animate-spin" />
                            ) : (
                                <Camera size={24} />
                            )}
                            <span className="text-[10px] font-bold mt-1">{isUploading ? 'Uploading' : 'Add'}</span>
                        </button>

                        {imageUrls.map((url, idx) => (
                            <div key={idx} className="relative flex-shrink-0 w-20 h-20 rounded-2xl overflow-hidden shadow-sm border border-gray-100">
                                <img src={url} alt="Item" className="w-full h-full object-cover" />
                                <button
                                    onClick={() => removeImage(url)}
                                    className="absolute top-1 right-1 bg-black/50 text-white p-1 rounded-full hover:bg-black/70 transition-colors"
                                >
                                    <X size={12} />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Group Selection */}
                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">Select Group</label>
                    <div className="relative">
                        <select
                            value={groupId}
                            onChange={(e) => setGroupId(e.target.value)}
                            disabled={groups.length === 0}
                            className="w-full bg-gray-50 border border-gray-200 text-gray-900 text-sm rounded-xl px-4 py-3 appearance-none focus:outline-none focus:ring-2 focus:ring-emerald-500 disabled:opacity-50"
                        >
                            {groups.length === 0 ? (
                                <option value="" disabled>No circles joined</option>
                            ) : (
                                groups.map(g => (
                                    <option key={g.id} value={g.id}>{g.name}</option>
                                ))
                            )}
                        </select>
                        <div className="absolute right-4 top-3.5 pointer-events-none w-3 h-3 bg-yellow-400 rounded-sm"></div>
                    </div>
                </div>

                {/* Basic Info */}
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">Item Name</label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            placeholder="What are you selling?"
                            className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 text-gray-900"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">Description</label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            rows={4}
                            placeholder="Describe the item's features, any defects, or pickup instructions."
                            className="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        ></textarea>
                    </div>
                </div>

                {/* Category Selection */}
                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">Category <span className="text-gray-400 font-normal">(Optional)</span></label>
                    <div className="relative">
                        <select
                            value={category}
                            onChange={(e) => setCategory(e.target.value)}
                            className="w-full bg-gray-50 border border-gray-200 text-gray-900 text-sm rounded-xl px-4 py-3 appearance-none focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        >
                            <option value="" disabled>Select Category</option>
                            <option value="CLOTHING">Clothing</option>
                            <option value="FURNITURE">Furniture</option>
                            <option value="DIGITAL_DEVICE">Digital / Electronics</option>
                            <option value="BOOKS">Books</option>
                            <option value="ETC">Other</option>
                        </select>
                        <div className="absolute right-4 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-400">
                            <ChevronDown size={20} />
                        </div>
                    </div>
                </div>

                {/* Transaction Type */}
                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-3">Transaction Type</label>
                    <div className="grid grid-cols-2 gap-3">
                        <button
                            onClick={() => setTransactionType('free')}
                            className={`py-3 rounded-xl text-sm font-bold transition-all ${transactionType === 'free'
                                ? 'bg-emerald-500 text-white shadow-md'
                                : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                                }`}
                        >
                            Free
                        </button>
                        <button
                            onClick={() => {
                                setTransactionType('sale');
                                setPrice('1000');
                            }}
                            className={`py-3 rounded-xl text-sm font-bold transition-all ${transactionType === 'sale'
                                ? 'bg-emerald-500 text-white shadow-md'
                                : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                                }`}
                        >
                            Sale
                        </button>
                    </div>

                    {transactionType === 'sale' && (
                        <div className="mt-4 animate-in fade-in slide-in-from-top-1 duration-200">
                            <label className="block text-sm font-semibold text-gray-700 mb-2">Price (₩)</label>
                            <input
                                type="number"
                                min="1"
                                value={price}
                                onChange={(e) => {
                                    const val = e.target.value;
                                    if (val === '') {
                                        setPrice('');
                                        return;
                                    }
                                    const num = parseInt(val, 10);
                                    if (num === 0) {
                                        setTransactionType('free');
                                        return;
                                    }
                                    if (num > 0) {
                                        setPrice(val);
                                    }
                                }}
                                placeholder="1000"
                                className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 text-gray-900 font-medium"
                            />
                        </div>
                    )}
                </div>

                {/* Auto Reserve Rule */}
                <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                    <div>
                        <span className="block text-sm font-semibold text-gray-900">First-come-first-served</span>
                        <span className="text-xs text-gray-500">Auto-confirm first requester</span>
                    </div>
                    <button
                        onClick={() => setIsAutoReserve(!isAutoReserve)}
                        className={`w-12 h-7 rounded-full transition-colors relative ${isAutoReserve ? 'bg-emerald-500' : 'bg-gray-300'
                            }`}
                    >
                        <div className={`w-5 h-5 bg-white rounded-full absolute top-1 transition-transform ${isAutoReserve ? 'left-6' : 'left-1'}`}></div>
                    </button>
                </div>

                {isAutoReserve && (
                    <div className="bg-emerald-50 p-4 rounded-xl flex items-start space-x-3">
                        <div className="bg-emerald-200 text-emerald-700 rounded-full w-5 h-5 flex items-center justify-center text-xs font-bold mt-0.5">i</div>
                        <p className="text-xs text-emerald-800 leading-relaxed">
                            The first person to request will be automatically reserved. Switch off to manually select your preferred buyer.
                        </p>
                    </div>
                )}

                <div className="pt-4">
                    <button
                        onClick={() => {
                            if (!title || !description) {
                                alert("Please fill in required fields.");
                                return;
                            }
                            setIsSubmitting(true);
                            itemsApi.createItem({
                                title,
                                description,
                                category,
                                groupId,
                                itemType: transactionType === 'sale' ? 'SELL' : 'GIVE',
                                price: transactionType === 'sale' ? Number(price) : 0,
                                imageUrls: imageUrls
                            })
                                .then(() => {
                                    alert("Item posted successfully!");
                                    navigate('/home');
                                })
                                .catch(err => {
                                    console.error(err);
                                    alert("Failed to post item.");
                                })
                                .finally(() => setIsSubmitting(false));
                        }}
                        disabled={isSubmitting || isUploading}
                        className={`w-full bg-emerald-500 hover:bg-emerald-600 text-white font-bold py-4 rounded-xl shadow-lg shadow-emerald-200 transition-all active:scale-[0.98] ${isSubmitting || isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}
                    >
                        {isSubmitting ? 'Posting...' : isUploading ? 'Uploading photos...' : 'Post Item'}
                    </button>
                </div>

            </div>
        </div>
    );
};

export default CreateItem;