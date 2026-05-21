import React from 'react';

const Pulse: React.FC<{ className?: string }> = ({ className }) => (
  <div className={`animate-pulse bg-gray-200 ${className}`}></div>
);

export const HomeSkeleton = () => (
  <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto relative overflow-hidden">
    <div className="bg-white px-6 pt-12 pb-6 mb-6">
      <Pulse className="h-8 w-3/4 rounded-lg mb-3" />
      <Pulse className="h-4 w-1/2 rounded-md" />
    </div>
    <div className="p-6 space-y-8">
      <div className="grid grid-cols-2 gap-4">
        <Pulse className="h-32 rounded-2xl bg-emerald-50/50" />
        <Pulse className="h-32 rounded-2xl bg-gray-100" />
      </div>
      <div>
        <Pulse className="h-4 w-32 rounded mb-4" />
        <div className="space-y-4">
            {[1, 2, 3].map(i => (
                <div key={i} className="bg-white p-4 rounded-2xl flex items-center space-x-4 border border-gray-100">
                    <Pulse className="w-16 h-16 rounded-xl flex-shrink-0" />
                    <div className="flex-1 space-y-2">
                        <div className="flex justify-between">
                            <Pulse className="h-4 w-1/2 rounded" />
                            <Pulse className="h-4 w-10 rounded-full" />
                        </div>
                        <Pulse className="h-3 w-3/4 rounded" />
                    </div>
                </div>
            ))}
        </div>
      </div>
    </div>
  </div>
);

export const ItemDetailSkeleton = () => (
    <div className="bg-white min-h-screen pb-24 max-w-md mx-auto relative">
        <Pulse className="h-80 w-full bg-gray-200" />
        <div className="px-6 py-6 rounded-t-3xl -mt-6 bg-white relative z-10 space-y-6">
            <div className="flex justify-between items-start">
                <Pulse className="h-8 w-3/4 rounded-lg" />
                <Pulse className="h-6 w-16 rounded-full" />
            </div>
            <div className="flex space-x-2">
                <Pulse className="h-4 w-16 rounded" />
                <Pulse className="h-4 w-16 rounded" />
            </div>
            <div className="space-y-3 py-2">
                <Pulse className="h-4 w-full rounded" />
                <Pulse className="h-4 w-full rounded" />
                <Pulse className="h-4 w-2/3 rounded" />
            </div>
            <Pulse className="h-20 w-full rounded-xl" />
             <div className="flex items-center space-x-3 pt-6 border-t border-gray-100">
                <Pulse className="w-12 h-12 rounded-full" />
                <div className="space-y-2 flex-1">
                     <Pulse className="h-4 w-1/3 rounded" />
                     <Pulse className="h-3 w-1/4 rounded" />
                </div>
                <Pulse className="h-8 w-20 rounded-lg" />
             </div>
        </div>
        <div className="fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-gray-100 flex space-x-4 z-50 max-w-md mx-auto">
             <Pulse className="w-14 h-14 rounded-full" />
             <Pulse className="flex-1 h-14 rounded-xl" />
        </div>
    </div>
);

export const ProfileSkeleton = () => (
    <div className="bg-gray-50 min-h-screen pb-24 max-w-md mx-auto">
        <div className="bg-white pt-12 pb-8 rounded-b-[2.5rem] shadow-sm flex flex-col items-center border-b border-gray-100">
            <Pulse className="w-24 h-24 rounded-full mb-4 border-4 border-gray-50" />
            <Pulse className="h-7 w-40 rounded mb-2" />
            <Pulse className="h-4 w-24 rounded mb-8" />
            <Pulse className="w-40 h-40 rounded-full ring-8 ring-gray-50" />
            <Pulse className="h-4 w-32 rounded mt-6" />
        </div>
        <div className="p-6 space-y-6">
             <div className="flex space-x-3">
                 <Pulse className="flex-1 h-20 rounded-2xl bg-white shadow-sm" />
                 <Pulse className="flex-1 h-20 rounded-2xl bg-white shadow-sm" />
                 <Pulse className="flex-1 h-20 rounded-2xl bg-white shadow-sm" />
             </div>
             <div className="grid grid-cols-2 gap-4">
                 <Pulse className="h-36 rounded-2xl bg-white shadow-sm" />
                 <Pulse className="h-36 rounded-2xl bg-white shadow-sm" />
             </div>
             <div className="space-y-2">
                 {[1,2,3].map(i => <Pulse key={i} className="h-16 w-full rounded-xl bg-white shadow-sm" />)}
             </div>
        </div>
    </div>
);
