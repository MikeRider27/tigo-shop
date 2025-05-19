import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const Cart = () => {
  const [carritoItems, setCarritoItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return navigate("/");

    fetch(`${process.env.REACT_APP_CART_URL}/carrito`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(async (data) => {
        const enriched = await Promise.all(
          data.items.map(async (item) => {
            const res = await fetch(`${process.env.REACT_APP_CATALOG_URL}/articulos/${item.articuloId}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            const articulo = await res.json();
            return { ...item, ...articulo };
          })
        );
        setCarritoItems(enriched);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error al cargar el carrito:", err);
        setLoading(false);
      });
  }, [navigate]);

  const eliminarDelCarrito = async (articuloId) => {
    const token = localStorage.getItem("token");
    try {
      await fetch(`${process.env.REACT_APP_CART_URL}/carrito/${articuloId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      setCarritoItems(prev => prev.filter(item => item.articuloId !== articuloId));
    } catch (err) {
      alert("Error al eliminar artículo.");
    }
  };

  const total = carritoItems.reduce((acc, item) => acc + item.precio * item.cantidad, 0);

  if (loading) return <div style={styles.center}>Cargando carrito...</div>;
  if (!carritoItems.length) return <div style={styles.center}>Tu carrito está vacío.</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>🧺 Carrito de Compras</h2>
      <div style={{ overflowX: "auto" }}>
        <table style={styles.table}>
          <thead>
            <tr>
              <th>Imagen</th>
              <th>Producto</th>
              <th>Precio</th>
              <th>Cantidad</th>
              <th>Subtotal</th>
              <th>Acción</th>
            </tr>
          </thead>
          <tbody>
            {carritoItems.map((item) => (
              <tr key={item.articuloId}>
                <td>
                  <img
                    src={item.imagenUrl || "https://via.placeholder.com/60"}
                    alt={item.nombre}
                    style={styles.image}
                  />
                </td>
                <td>{item.nombre}</td>
                <td>₲ {item.precio.toLocaleString()}</td>
                <td>{item.cantidad}</td>
                <td>₲ {(item.precio * item.cantidad).toLocaleString()}</td>
                <td>
                  <button style={styles.deleteBtn} onClick={() => eliminarDelCarrito(item.articuloId)}>
                    ❌
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <h3 style={styles.total}>Total: <span style={{ color: "#007bff" }}>₲ {total.toLocaleString()}</span></h3>
    </div>
  );
};

const styles = {
  container: {
    backgroundColor: "#fff",
    padding: "1.5rem",
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
  },
  title: {
    marginBottom: "1rem",
    textAlign: "center",
    color: "#333",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    textAlign: "center",
  },
  image: {
    width: "60px",
    height: "60px",
    borderRadius: "6px",
    objectFit: "cover",
  },
  deleteBtn: {
    backgroundColor: "red",
    color: "#fff",
    border: "none",
    padding: "5px 12px",
    borderRadius: "5px",
    cursor: "pointer",
    fontSize: "1rem",
  },
  total: {
    textAlign: "right",
    marginTop: "1.2rem",
    fontSize: "1.2rem",
    fontWeight: "bold",
  },
  center: {
    textAlign: "center",
    fontSize: "1.1rem",
    marginTop: "2rem",
  },
};

export default Cart;
