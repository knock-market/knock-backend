import React, { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Camera, Check, Copy, Loader2, Share } from 'lucide-react';
import { groupsApi, imagesApi } from '../services';

const CreateGroup: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [inviteLink, setInviteLink] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    try {
      const uploaded = await imagesApi.upload(file, 'groups');
      setImageUrl(uploaded.imageUrl);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to upload image.';
      alert(message);
    } finally {
      setIsUploading(false);
    }
  };

  const handleCreate = async () => {
    if (!name.trim()) return;

    setIsSubmitting(true);
    try {
      const created = await groupsApi.createGroup({
        name: name.trim(),
        description: description.trim(),
        imageUrl: imageUrl || undefined,
      });

      const code = created.inviteCode;
      if (!code) {
        navigate('/home');
        return;
      }

      setInviteLink(`${window.location.origin}/#/join/${code}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to create group.';
      alert(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCopy = () => {
    if (!inviteLink) return;

    navigator.clipboard.writeText(inviteLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleShare = async () => {
    if (!inviteLink) return;

    if (navigator.share) {
      try {
        await navigator.share({
          title: `Join ${name}`,
          text: `Join my group \"${name}\" on Knock Market!`,
          url: inviteLink,
        });
      } catch (error) {
        console.error(error);
      }
      return;
    }

    handleCopy();
  };

  if (inviteLink) {
    return (
      <div className="bg-white min-h-screen max-w-md mx-auto flex flex-col items-center justify-center p-6 text-center animate-in fade-in duration-300">
        <div className="w-24 h-24 bg-emerald-100 rounded-full flex items-center justify-center mb-6 text-emerald-500 shadow-sm">
          <Check size={48} strokeWidth={3} />
        </div>
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Group Created!</h1>
        <p className="text-gray-500 mb-8 leading-relaxed">
          Your group <span className="font-semibold text-gray-900">"{name}"</span> is ready.<br />
          Share the link below to invite members.
        </p>

        <div className="w-full bg-gray-50 border border-gray-200 rounded-2xl p-2 mb-6 flex items-center pr-2">
          <div className="flex-1 px-4 py-2 overflow-hidden text-left">
            <p className="text-xs text-gray-400 font-bold uppercase mb-0.5">Invite Link</p>
            <p className="text-sm text-gray-800 font-medium truncate font-mono">{inviteLink}</p>
          </div>
          <button
            onClick={handleCopy}
            className="p-3 rounded-xl bg-white border border-gray-200 text-gray-600 hover:text-emerald-600 hover:border-emerald-200 shadow-sm transition-all active:scale-95"
          >
            {copied ? <Check size={20} /> : <Copy size={20} />}
          </button>
        </div>

        <button
          onClick={handleShare}
          className="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-bold py-4 rounded-xl shadow-lg shadow-emerald-200 mb-4 flex items-center justify-center space-x-2 transition-all active:scale-[0.98]"
        >
          <Share size={20} />
          <span>Share Invite Link</span>
        </button>

        <button
          onClick={() => navigate('/home')}
          className="text-gray-400 font-medium text-sm py-4 hover:text-gray-900 transition-colors"
        >
          Done
        </button>
      </div>
    );
  }

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600">
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Create New Group</h1>
      </div>

      <div className="p-6 space-y-8">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">Group Cover Image</label>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*"
            className="hidden"
          />
          <div className="flex justify-center">
            <button
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
              className="w-full h-44 rounded-2xl border-2 border-dashed border-emerald-300 flex flex-col items-center justify-center text-emerald-500 bg-emerald-50 hover:bg-emerald-100 transition-all overflow-hidden relative shadow-sm"
            >
              {imageUrl ? (
                <img src={imageUrl} alt="Cover" className="w-full h-full object-cover" />
              ) : (
                <>
                  {isUploading ? <Loader2 size={32} className="animate-spin" /> : <Camera size={32} />}
                  <span className="text-sm font-bold mt-2">{isUploading ? 'Uploading...' : 'Upload Cover Photo'}</span>
                </>
              )}
              {imageUrl && !isUploading && (
                <div className="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity">
                  <Camera size={32} className="text-white" />
                </div>
              )}
            </button>
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Group Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., Incheon Univ. Class of 2024"
              className="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 text-gray-900"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">Description</label>
            <textarea
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="What is this group for? Who can join?"
              className="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 text-gray-900"
            ></textarea>
          </div>
        </div>

        <div className="pt-4">
          <button
            onClick={handleCreate}
            disabled={!name.trim() || isUploading || isSubmitting}
            className={`w-full font-bold py-4 rounded-xl shadow-lg transition-all active:scale-[0.98] ${name.trim() && !isUploading && !isSubmitting
              ? 'bg-emerald-500 hover:bg-emerald-600 text-white shadow-emerald-200'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
          >
            {isSubmitting ? <Loader2 size={24} className="animate-spin mx-auto" /> : 'Create Group'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateGroup;
