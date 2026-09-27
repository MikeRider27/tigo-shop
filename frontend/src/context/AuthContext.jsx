import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getToken, onUnauthorized, saveToken } from '../api/client';
import { authApi, userApi } from '../api/endpoints';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  // Si hay token guardado, se valida contra /users/me antes de mostrar la app.
  const [loading, setLoading] = useState(Boolean(getToken()));

  const logout = useCallback(() => {
    saveToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    onUnauthorized(logout);
    if (!getToken()) return;
    userApi
      .me()
      .then(setUser)
      .catch(logout)
      .finally(() => setLoading(false));
  }, [logout]);

  const startSession = useCallback((auth) => {
    saveToken(auth.accessToken);
    setUser(auth.user);
    return auth.user;
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      login: async (email, password) => startSession(await authApi.login(email, password)),
      register: async (data) => startSession(await authApi.register(data)),
      logout,
      setUser,
      // Tras cambiar la contraseña el token anterior queda revocado: se guarda el nuevo.
      renewSession: startSession,
    }),
    [user, loading, logout, startSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
