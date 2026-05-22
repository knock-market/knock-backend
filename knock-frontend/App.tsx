import React, { useEffect, useState } from 'react';
import { HashRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import Onboarding from './pages/Onboarding';
import Home from './pages/Home';
import ItemDetail from './pages/ItemDetail';
import CreateItem from './pages/CreateItem';
import PickupLocationPicker from './pages/PickupLocationPicker';
import Profile from './pages/Profile';
import Notifications from './pages/Notifications';
import EditProfile from './pages/EditProfile';
import NotificationSettings from './pages/NotificationSettings';
import BlockedUsers from './pages/BlockedUsers';
import ManageItems from './pages/ManageItems';
import ManageItemDetail from './pages/ManageItemDetail';
import SellerPage from './pages/SellerPage';
import Bookmarks from './pages/Bookmarks';
import Login from './pages/Login';
import Signup from './pages/Signup';
import TermsOfService from './pages/TermsOfService';
import PrivacyPolicy from './pages/PrivacyPolicy';
import BottomNav from './components/BottomNav';
import { authApi } from './services';

const PUBLIC_PATHS = new Set([
  '/',
  '/login',
  '/signup',
  '/terms',
  '/privacy',
]);

const isPublicPath = (pathname: string): boolean => {
  if (PUBLIC_PATHS.has(pathname)) {
    return true;
  }
  return /^\/item\/\d+$/.test(pathname) || /^\/seller\/\d+$/.test(pathname) || /^\/shop\/[A-Za-z0-9_-]+$/.test(pathname);
};

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();

  const showBottomNav =
    ['/home', '/saved', '/notifications', '/profile', '/manage-items'].includes(location.pathname);

  return (
    <>
      <div className="min-h-screen bg-gray-100">
        {children}
      </div>
      {showBottomNav && <BottomNav />}
    </>
  );
};

const AuthGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const [isChecking, setIsChecking] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [checkedPathname, setCheckedPathname] = useState('');

  useEffect(() => {
    if (isPublicPath(location.pathname)) {
      setIsChecking(false);
      setCheckedPathname(location.pathname);
      return;
    }

    let mounted = true;
    setIsChecking(true);

    authApi.getMe()
      .then(() => {
        if (mounted) {
          setIsAuthenticated(true);
          setCheckedPathname(location.pathname);
        }
      })
      .catch(() => {
        if (mounted) {
          setIsAuthenticated(false);
          setCheckedPathname(location.pathname);
        }
      })
      .finally(() => {
        if (mounted) {
          setIsChecking(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, [location.pathname]);

  if (isPublicPath(location.pathname)) {
    return <>{children}</>;
  }

  if (isChecking || checkedPathname !== location.pathname) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center text-gray-500">
        Checking session...
      </div>
    );
  }

  if (!isAuthenticated) {
    const next = encodeURIComponent(`${location.pathname}${location.search}`);
    return <Navigate to={`/login?next=${next}`} replace />;
  }

  return <>{children}</>;
};

const App: React.FC = () => {
  return (
    <HashRouter>
      <AuthGuard>
        <Layout>
          <Routes>
            <Route path="/" element={<Onboarding />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/home" element={<Home />} />
            <Route path="/item/:id" element={<ItemDetail />} />
            <Route path="/seller/:memberId" element={<SellerPage />} />
            <Route path="/shop/:token" element={<SellerPage />} />
            <Route path="/create" element={<CreateItem />} />
            <Route path="/create/location" element={<PickupLocationPicker />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/edit-profile" element={<EditProfile />} />
            <Route path="/manage-items" element={<ManageItems />} />
            <Route path="/manage-item/:id" element={<ManageItemDetail />} />
            <Route path="/settings/notifications" element={<NotificationSettings />} />
            <Route path="/settings/blocked" element={<BlockedUsers />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/saved" element={<Bookmarks />} />
            <Route path="/terms" element={<TermsOfService />} />
            <Route path="/privacy" element={<PrivacyPolicy />} />
          </Routes>
        </Layout>
      </AuthGuard>
    </HashRouter>
  );
};

export default App;
