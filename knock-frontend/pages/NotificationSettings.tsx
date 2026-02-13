import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

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
  const [settings, setSettings] = useState({
    push: true,
    newItems: true,
    chat: true,
    marketing: false,
    sound: true
  });

  const toggle = (key: keyof typeof settings) => {
    setSettings(prev => ({ ...prev, [key]: !prev[key] }));
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
        <div className="py-2">
             <p className="text-xs font-bold text-gray-400 uppercase tracking-wider py-2">General</p>
             <Toggle 
                label="Push Notifications" 
                description="Pause all notifications"
                checked={settings.push} 
                onChange={() => toggle('push')} 
             />
             <Toggle 
                label="Sound & Haptics" 
                checked={settings.sound} 
                onChange={() => toggle('sound')} 
             />
        </div>

        <div className="py-2">
             <p className="text-xs font-bold text-gray-400 uppercase tracking-wider py-2">Activity</p>
             <Toggle 
                label="New Items in Groups" 
                description="Get notified when items are posted in your groups"
                checked={settings.newItems} 
                onChange={() => toggle('newItems')} 
             />
             <Toggle 
                label="Chat Messages" 
                description="Receive messages from buyers/sellers"
                checked={settings.chat} 
                onChange={() => toggle('chat')} 
             />
        </div>

        <div className="py-2">
             <p className="text-xs font-bold text-gray-400 uppercase tracking-wider py-2">Others</p>
             <Toggle 
                label="Marketing & Tips" 
                description="News about Knock Market"
                checked={settings.marketing} 
                onChange={() => toggle('marketing')} 
             />
        </div>
      </div>
    </div>
  );
};

export default NotificationSettings;