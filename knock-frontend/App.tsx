import React from 'react';
import { HashRouter, Routes, Route, useLocation } from 'react-router-dom';
import Onboarding from './pages/Onboarding';
import Home from './pages/Home';
import GroupFeed from './pages/GroupFeed';
import ItemDetail from './pages/ItemDetail';
import CreateItem from './pages/CreateItem';
import CreateGroup from './pages/CreateGroup';
import Profile from './pages/Profile';
import Notifications from './pages/Notifications';
import EditProfile from './pages/EditProfile';
import NotificationSettings from './pages/NotificationSettings';
import BlockedUsers from './pages/BlockedUsers';
import ManageItems from './pages/ManageItems';
import ManageItemDetail from './pages/ManageItemDetail';
import Bookmarks from './pages/Bookmarks';
import Login from './pages/Login';
import Signup from './pages/Signup';
import BottomNav from './components/BottomNav';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();

  // Show BottomNav only on these specific main routes and group feed
  const showBottomNav =
    ['/home', '/groups', '/saved', '/notifications', '/profile', '/manage-items'].includes(location.pathname) ||
    location.pathname.startsWith('/group/');

  return (
    <>
      <div className="min-h-screen bg-gray-100">
        {children}
      </div>
      {showBottomNav && <BottomNav />}
    </>
  );
};

const App: React.FC = () => {
  return (
    <HashRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Onboarding />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/home" element={<Home />} />
          <Route path="/groups" element={<Home />} />
          <Route path="/group/:id" element={<GroupFeed />} />
          <Route path="/item/:id" element={<ItemDetail />} />
          <Route path="/create" element={<CreateItem />} />
          <Route path="/create-group" element={<CreateGroup />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/edit-profile" element={<EditProfile />} />
          <Route path="/manage-items" element={<ManageItems />} />
          <Route path="/manage-item/:id" element={<ManageItemDetail />} />
          <Route path="/settings/notifications" element={<NotificationSettings />} />
          <Route path="/settings/blocked" element={<BlockedUsers />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/saved" element={<Bookmarks />} />
        </Routes>
      </Layout>
    </HashRouter>
  );
};

export default App;