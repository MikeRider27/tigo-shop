import { createContext, useContext, useState } from "react";

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cantidadTotal, setCantidadTotal] = useState(0);

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

      setCantidadTotal((prev) => prev + cantidad);
    } catch (error) {
      console.error("Error al agregar al carrito:", error);
    }
  };

  return (
    <CartContext.Provider value={{ cantidadTotal, agregarAlCarrito }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
