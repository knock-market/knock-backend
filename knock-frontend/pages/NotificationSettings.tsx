import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { memberSettingsApi } from '../services';
import { NotificationSettingsResponseDto } from '../types';

const Toggle: React.FC<{ label: string; description?: string; checked: boolean; onChange: () => void }> = ({ label, description, checked, onChange }) => (
    <div className="flex items-center justify-between py-4">
        <div className="pr-4">
            <h3 className="text-sm font-bold text-gray-900">{label}</h3>
            {description && <p className="text-xs text-gray-500 mt-0.5">{description}</p>}
        </div>
        <button 
            onClick={onChange}
            className={`w-12 h-7 rounded-full transition-colors relative flex-shrink-0 ${checked ? 'bg-emerald-500' : 'bg-gray-200'}`}
        >
            <div className={`w-5 h-5 bg-white rounded-full absolute top-1 transition-transform shadow-sm ${checked ? 'translate-x-6' : 'translate-x-1'}`}></div>
        </button>
    </div>
);

const NotificationSettings: React.FC = () => {
  const navigate = useNavigate();
  const [settings, setSettings] = useState<NotificationSettingsResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState('');

  useEffect(() => {
    const loadSettings = async () => {
      setIsLoading(true);
      try {
        const response = await memberSettingsApi.getNotificationSettings();
        setSettings(response);
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to load settings.';
        alert(message);
      } finally {
        setIsLoading(false);
      }
    };

    loadSettings();
  }, []);

  const toggle = (key: keyof NotificationSettingsResponseDto) => {
    setSettings((prev) => {
      if (!prev) {
        return prev;
      }
      return { ...prev, [key]: !prev[key] };
    });
    setSaveMessage('');
  };

  const handleSave = async () => {
    if (!settings) {
      return;
    }

    setIsSaving(true);
    try {
      await memberSettingsApi.updateNotificationSettings(settings);
      setSaveMessage('Saved.');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to save settings.';
      alert(message);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto">
      <div className="px-4 py-4 flex items-center border-b border-gray-100 sticky top-0 bg-white z-10">
        <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft size={24} />
        </button>
        <h1 className="text-lg font-bold text-gray-900 ml-2">Notification Settings</h1>
      </div>

      <div className="px-6 py-2 divide-y divide-gray-100">
        {isLoading ? (
          <div className="py-20 flex items-center justify-center text-gray-500">
            <Loader2 size={18} className="animate-spin mr-2" />
            Loading settings...
          </div>
        ) : (
          <>
        <div className="py-2">
             <p className="text-xs font-bold text-gray-400 uppercase tracking-wider py-2">General</p>
             <Toggle 
                label="Push Notifications" 
                description="Pause all notifications"
                checked={settings?.push ?? false} 
                onChange={() => toggle('push')} 
             />
             <Toggle 
                label="Sound & Haptics" 
                checked={settings?.sound ?? false} 
                onChange={() => toggle('sound')} 
             />
        </div>

        <div className="py-2">
             <p className="text-xs font-bold text-gray-400 uppercase tracking-wider py-2">Activity</p>
             <Toggle 
                label="New Seller Items"
                description="Get notified when followed sellers post items"
                checked={settings?.newItems ?? false} 
                onChange={() => toggle('newItems')} 
             />
             <Toggle 
                label="Chat Messages" 
                description="Receive messages from buyers/sellers"
                checked={settings?.chat ?? false} 
                onChange={() => toggle('chat')} 
             />
        </div>

        <div className="py-2">
             <p className="text-xs font-bold text-gray-400 uppercase tracking-wider py-2">Others</p>
             <Toggle 
                label="Marketing & Tips" 
                description="News about Knock Market"
                checked={settings?.marketing ?? false} 
                onChange={() => toggle('marketing')} 
             />
        </div>
            <div className="py-4 flex items-center justify-between">
              <p className="text-xs text-emerald-600">{saveMessage}</p>
              <button
                onClick={handleSave}
                disabled={isSaving || !settings}
                className={`px-4 py-2 rounded-lg text-sm font-semibold transition-colors ${
                  isSaving || !settings
                    ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                    : 'bg-emerald-500 text-white hover:bg-emerald-600'
                }`}
              >
                {isSaving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default NotificationSettings;
