import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { cartApi } from '../api/endpoints';
import { useAuth } from './AuthContext';

const EMPTY_CART = { items: [], totalItems: 0, total: 0 };
const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(EMPTY_CART);
  // false hasta recibir el carrito del servidor (evita mostrar "vacío" mientras carga).
  const [loaded, setLoaded] = useState(false);

  const refresh = useCallback(async () => {
    setCart(await cartApi.get());
    setLoaded(true);
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      refresh().catch(() => {
        setCart(EMPTY_CART);
        setLoaded(true);
      });
    } else {
      setCart(EMPTY_CART);
      setLoaded(false);
    }
  }, [isAuthenticated, refresh]);

  const value = useMemo(
    () => ({
      cart,
      loaded,
      refresh,
      add: async (productId, quantity = 1) => setCart(await cartApi.add(productId, quantity)),
      update: async (productId, quantity) => setCart(await cartApi.update(productId, quantity)),
      remove: async (productId) => {
        await cartApi.remove(productId);
        await refresh();
      },
      reset: () => setCart(EMPTY_CART),
    }),
    [cart, loaded, refresh],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  return useContext(CartContext);
}
