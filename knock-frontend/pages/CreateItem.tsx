import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, Camera, ChevronDown, Loader2, X } from 'lucide-react';
import { groupsApi, imagesApi, itemsApi } from '../services';
import { GroupResponseDto, ItemCategory } from '../types';

const CreateItem: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [transactionType, setTransactionType] = useState<'free' | 'sale'>('free');
  const [price, setPrice] = useState('1000');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<ItemCategory>(ItemCategory.ETC);
  const [groups, setGroups] = useState<GroupResponseDto[]>([]);
  const [groupId, setGroupId] = useState('');
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const preSelectedGroupId = new URLSearchParams(location.search).get('groupId');

  useEffect(() => {
    const loadGroups = async () => {
      try {
        const fetchedGroups = await groupsApi.getMyGroups();
        setGroups(fetchedGroups);

        if (preSelectedGroupId) {
          setGroupId(preSelectedGroupId);
        } else if (fetchedGroups.length > 0) {
          setGroupId(String(fetchedGroups[0].id));
        }
      } catch (error) {
        console.error('Failed to fetch groups:', error);
      }
    };

    loadGroups();
  }, [preSelectedGroupId]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setIsUploading(true);
    try {
      const selectedFiles = Array.from(files as FileList);
      const uploadPromises = selectedFiles.map((file: File) => imagesApi.upload(file));
      const uploaded = await Promise.all(uploadPromises);
      const newUrls = uploaded.map((file) => file.imageUrl);
      setImageUrls((prev) => [...prev, ...newUrls].slice(0, 5));
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to upload image.';
      alert(message);
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const removeImage = (url: string) => {
    setImageUrls((prev) => prev.filter((value) => value !== url));
  };

  const handleSubmit = async () => {
    if (!title.trim() || !description.trim() || !groupId) {
      alert('Please fill in required fields.');
      return;
    }

    const parsedPrice = Number(price);
    if (transactionType === 'sale') {
      const isInvalidSalePrice = !price.trim() || !Number.isFinite(parsedPrice) || !Number.isInteger(parsedPrice) || parsedPrice <= 0;
      if (isInvalidSalePrice) {
        alert('Please enter a valid sale price greater than 0.');
        return;
      }
    }

    setIsSubmitting(true);
    try {
      await itemsApi.createItem({
        title: title.trim(),
        description: description.trim(),
        category,
        groupId: Number(groupId),
        itemType: transactionType === 'sale' ? 'SELL' : 'GIVE',
        price: transactionType === 'sale' ? parsedPrice : 0,
        imageUrls,
      });
      alert('Item posted successfully!');
      navigate('/home');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to post item.';
      alert(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button
          onClick={() => navigate(-1)}
          className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600"
          aria-label="Go back"
        >
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Register Item</h1>
      </div>

      <div className="p-6 space-y-8">
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
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading || imageUrls.length >= 5}
              className={`flex-shrink-0 w-20 h-20 rounded-lg border-2 border-dashed flex flex-col items-center justify-center transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${isUploading ? 'bg-gray-50 border-gray-200 text-gray-400' : 'border-emerald-300 text-emerald-700 bg-emerald-50 hover:bg-emerald-100'}`}
              aria-label="Add item photos"
            >
              {isUploading ? <Loader2 size={24} className="animate-spin" /> : <Camera size={24} />}
              <span className="text-[10px] font-bold mt-1">{isUploading ? 'Uploading' : 'Add'}</span>
            </button>

            {imageUrls.map((url, idx) => (
              <div key={idx} className="relative flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden shadow-sm border border-gray-100">
                <img src={url} alt={`Item preview ${idx + 1}`} className="w-full h-full object-cover" loading="lazy" decoding="async" />
                <button
                  type="button"
                  onClick={() => removeImage(url)}
                  className="absolute top-1 right-1 bg-black/60 text-white p-1 rounded-lg hover:bg-black/80 transition-colors focus-visible:ring-2 focus-visible:ring-white"
                  aria-label={`Remove item preview ${idx + 1}`}
                >
                  <X size={12} />
                </button>
              </div>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">Select Group</label>
          <div className="relative">
            <select
              value={groupId}
              onChange={(e) => setGroupId(e.target.value)}
              disabled={groups.length === 0}
              className="w-full bg-gray-50 border border-gray-200 text-gray-900 text-sm rounded-lg px-4 py-3 appearance-none focus:outline-none focus:ring-2 focus:ring-emerald-600 disabled:opacity-50"
            >
              {groups.length === 0 ? (
                <option value="" disabled>No circles joined</option>
              ) : (
                groups.map((group) => (
                  <option key={group.id} value={group.id}>{group.name}</option>
                ))
              )}
            </select>
            <div className="absolute right-4 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-400">
              <ChevronDown size={20} />
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Item Name</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="What are you selling?"
              className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
              placeholder="Describe the item and pickup instructions."
              className="w-full bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900"
            ></textarea>
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">Category</label>
          <div className="relative">
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as ItemCategory)}
              className="w-full bg-gray-50 border border-gray-200 text-gray-900 text-sm rounded-lg px-4 py-3 appearance-none focus:outline-none focus:ring-2 focus:ring-emerald-600"
            >
              <option value={ItemCategory.CLOTHING}>Clothing</option>
              <option value={ItemCategory.FURNITURE}>Furniture</option>
              <option value={ItemCategory.DIGITAL_DEVICE}>Digital / Electronics</option>
              <option value={ItemCategory.BOOKS}>Books</option>
              <option value={ItemCategory.ETC}>Other</option>
            </select>
            <div className="absolute right-4 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-400">
              <ChevronDown size={20} />
            </div>
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">Transaction Type</label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setTransactionType('free')}
              className={`py-3 rounded-lg text-sm font-bold transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${transactionType === 'free' ? 'bg-emerald-600 text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              Free
            </button>
            <button
              type="button"
              onClick={() => {
                setTransactionType('sale');
                setPrice('1000');
              }}
              className={`py-3 rounded-lg text-sm font-bold transition-colors focus-visible:ring-2 focus-visible:ring-emerald-600 ${transactionType === 'sale' ? 'bg-emerald-600 text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
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
                step="1"
                inputMode="numeric"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                placeholder="1000"
                className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-600 text-gray-900 font-medium"
              />
            </div>
          )}
        </div>

        <div className="pt-4">
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitting || isUploading}
            className={`w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-4 rounded-lg shadow-lg shadow-emerald-200 transition-colors focus-visible:ring-2 focus-visible:ring-emerald-700 focus-visible:ring-offset-2 ${isSubmitting || isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {isSubmitting ? 'Posting...' : isUploading ? 'Uploading photos...' : 'Post Item'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateItem;
