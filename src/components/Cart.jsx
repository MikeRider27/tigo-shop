import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";

const Cart = () => {
  const [carritoItems, setCarritoItems] = useState([]);
  const [direccionEnvio, setDireccionEnvio] = useState("");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { refrescarCantidad } = useCart();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return navigate("/");

    fetch(`${process.env.REACT_APP_CART_URL}/carrito/items`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(async (items) => {
        const enriched = await Promise.all(
          items.map(async (item) => {
            const res = await fetch(`${process.env.REACT_APP_CATALOG_URL}/articulos/${item.articuloId}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            const articulo = await res.json();
            return { ...item, ...articulo };
          })
        );
        setCarritoItems(enriched);

        // Obtener dirección actual del usuario
        const resUser = await fetch(`${process.env.REACT_APP_AUTH_URL}/auth/secure`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const user = await resUser.json();
        setDireccionEnvio(user.direccionEnvio || "");
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error al cargar el carrito:", err);
        setLoading(false);
      });
  }, [navigate]);

  const eliminarDelCarrito = async (itemId) => {
    const token = localStorage.getItem("token");
    try {
      await fetch(`${process.env.REACT_APP_CART_URL}/carrito/remove?itemId=${itemId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      setCarritoItems(prev => prev.filter(item => item.id !== itemId));
      refrescarCantidad();
    } catch (err) {
      alert("Error al eliminar artículo.");
    }
  };

  const confirmarPedido = async () => {
    const token = localStorage.getItem("token");
    const formData = new URLSearchParams();
    formData.append("nuevaDireccionEnvio", direccionEnvio);
  
    try {
      const res = await fetch(`${process.env.REACT_APP_CART_URL}/carrito/confirm`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: formData.toString(),
      });
  
      if (!res.ok) throw new Error("Error al confirmar el pedido");
  
      alert("✅ ¡Pedido confirmado exitosamente!");
      setCarritoItems([]);
      refrescarCantidad();
      // Podés redirigir a órdenes si querés
      // navigate("/orders");
    } catch (err) {
      console.error(err);
      alert("❌ No se pudo confirmar el pedido.");
    }
  };
  

  const total = carritoItems.reduce((acc, item) => acc + item.precio * item.cantidad, 0);

  if (loading) return <div style={styles.center}>Cargando carrito...</div>;
  if (!carritoItems.length) return <div style={styles.center}>Tu carrito está vacío.</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>🧺 Carrito de Compras</h2>

      <div style={styles.addressBox}>
        <label style={styles.addressLabel}>Dirección de envío:</label>
        <input
          type="text"
          value={direccionEnvio}
          onChange={(e) => setDireccionEnvio(e.target.value)}
          style={styles.addressInput}
        />
      </div>

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
              <tr key={item.id}>
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
                  <button style={styles.deleteBtn} onClick={() => eliminarDelCarrito(item.id)}>
                    ❌
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <h3 style={styles.total}>Total: <span style={{ color: "#007bff" }}>₲ {total.toLocaleString()}</span></h3>

      <button style={styles.confirmBtn} onClick={confirmarPedido}>
        ✅ Confirmar Pedido
      </button>
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
  addressBox: {
    marginBottom: "1rem",
  },
  addressLabel: {
    display: "block",
    fontWeight: "bold",
    marginBottom: "0.3rem",
  },
  addressInput: {
    width: "100%",
    padding: "8px",
    borderRadius: "5px",
    border: "1px solid #ccc",
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
  confirmBtn: {
    marginTop: "1.5rem",
    backgroundColor: "#28a745",
    color: "#fff",
    border: "none",
    padding: "10px 16px",
    borderRadius: "6px",
    fontSize: "1rem",
    cursor: "pointer",
    float: "right",
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
