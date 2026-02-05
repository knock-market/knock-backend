import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Camera, Check } from 'lucide-react';
import { CURRENT_USER } from '../constants';

const EditProfile: React.FC = () => {
  const navigate = useNavigate();
  const [name, setName] = useState(CURRENT_USER.name);
  const [role, setRole] = useState(CURRENT_USER.role);
  const [isLoading, setIsLoading] = useState(false);

  const handleSave = () => {
    setIsLoading(true);
    // Simulate API call
    setTimeout(() => {
        setIsLoading(false);
        navigate(-1);
    }, 1000);
  };

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto relative">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Edit Profile</h1>
      </div>

      <div className="p-6">
        {/* Avatar Edit */}
        <div className="flex flex-col items-center mb-8">
            <div className="relative">
                <img 
                    src={CURRENT_USER.avatar} 
                    alt="Profile" 
                    className="w-28 h-28 rounded-full border-4 border-gray-50 object-cover" 
                />
                <button className="absolute bottom-0 right-0 bg-emerald-500 text-white p-2 rounded-full border-4 border-white hover:bg-emerald-600 transition-colors shadow-sm">
                    <Camera size={20} />
                </button>
            </div>
            <p className="text-sm text-emerald-600 font-bold mt-4">Change Profile Photo</p>
        </div>

        {/* Form Fields */}
        <div className="space-y-6">
            <div>
                <label className="block text-sm font-bold text-gray-900 mb-2">Display Name</label>
                <input 
                    type="text" 
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all"
                />
            </div>

            <div>
                <label className="block text-sm font-bold text-gray-900 mb-2">Organization / Role</label>
                <input 
                    type="text" 
                    value={role}
                    onChange={(e) => setRole(e.target.value)}
                    className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all"
                />
                <p className="text-xs text-gray-500 mt-2">This will be displayed on your profile card.</p>
            </div>
        </div>
      </div>

      <div className="fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-gray-100 max-w-md mx-auto">
          <button 
            onClick={handleSave}
            disabled={isLoading}
            className="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-bold py-4 rounded-xl shadow-lg shadow-emerald-200 transition-all active:scale-[0.98] flex items-center justify-center space-x-2"
          >
              {isLoading ? (
                  <span>Saving...</span>
              ) : (
                  <>
                    <Check size={20} />
                    <span>Save Changes</span>
                  </>
              )}
          </button>
      </div>
    </div>
  );
};

export default EditProfile;