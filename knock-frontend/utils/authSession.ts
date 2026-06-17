const AUTH_SESSION_HINT_KEY = 'knock.auth.session';

const canUseLocalStorage = (): boolean => (
  typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
);

export const hasAuthSessionHint = (): boolean => {
  if (!canUseLocalStorage()) {
    return false;
  }

  try {
    return window.localStorage.getItem(AUTH_SESSION_HINT_KEY) === 'true';
  } catch {
    return false;
  }
};

export const markAuthSession = (): void => {
  if (!canUseLocalStorage()) {
    return;
  }

  try {
    window.localStorage.setItem(AUTH_SESSION_HINT_KEY, 'true');
  } catch {
    // Ignore storage failures; server session remains the source of truth.
  }
};

export const clearAuthSession = (): void => {
  if (!canUseLocalStorage()) {
    return;
  }

  try {
    window.localStorage.removeItem(AUTH_SESSION_HINT_KEY);
  } catch {
    // Ignore storage failures; protected API responses still enforce auth.
  }
};
