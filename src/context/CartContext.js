import { createContext, useContext, useState, useCallback } from "react";

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cantidadTotal, setCantidadTotal] = useState(0);

  const refrescarCantidad = useCallback(async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setCantidadTotal(0);
      return;
    }

    try {
      const res = await fetch(`${process.env.REACT_APP_CART_URL}/carrito/items`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) return;
      const items = await res.json();
      setCantidadTotal(items.reduce((acc, item) => acc + item.cantidad, 0));
    } catch (error) {
      console.error("Error al obtener el carrito:", error);
    }
  }, []);

  const agregarAlCarrito = async (articuloId, cantidad = 1) => {
    const token = localStorage.getItem("token");

    try {
      await fetch(`${process.env.REACT_APP_CART_URL}/carrito/agregar`, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          Authorization: `Bearer ${token}`,
        },
        body: new URLSearchParams({
          articuloId,
          cantidad,
        }),
      });

      await refrescarCantidad();
    } catch (error) {
      console.error("Error al agregar al carrito:", error);
    }
  };

  return (
    <CartContext.Provider value={{ cantidadTotal, agregarAlCarrito, refrescarCantidad }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
