const AUTH_DEFAULT_NEXT_PATH = '/home';

export const resolveAuthNextPath = (rawNext: string | null): string => {
  if (!rawNext) {
    return AUTH_DEFAULT_NEXT_PATH;
  }

  const next = rawNext.trim();
  if (
    !next.startsWith('/') ||
    next.startsWith('//') ||
    next.includes('\r') ||
    next.includes('\n')
  ) {
    return AUTH_DEFAULT_NEXT_PATH;
  }

  return next;
};

export const buildGoogleAuthStartUrl = (nextPath: string): string => (
  `/api/v1/auth/social/google/start?next=${encodeURIComponent(nextPath)}`
);

export const buildLoginUrl = (nextPath: string): string => (
  `/login?next=${encodeURIComponent(nextPath)}`
);
