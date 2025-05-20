import React, { useEffect, useState } from "react";

const Orders = () => {
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) return;

    fetch(`${process.env.REACT_APP_CART_URL}/carrito/pedidos`, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
    })
      .then((res) => res.json())
      .then(async (data) => {
        // Para cada pedido, enriquecer los items
        const enriched = await Promise.all(
          data.map(async (pedido) => {
            const items = await Promise.all(
              pedido.items.map(async (item) => {
                const res = await fetch(
                  `${process.env.REACT_APP_CATALOG_URL}/articulos/${item.articuloId}`,
                  {
                    headers: { Authorization: `Bearer ${token}` },
                  }
                );
                const articulo = await res.json();
                return { ...item, ...articulo };
              })
            );
            return { ...pedido, items };
          })
        );

        setPedidos(enriched);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error al cargar pedidos:", err);
        setLoading(false);
      });
  }, [token]);

  if (loading) return <div style={styles.center}>Cargando pedidos...</div>;
  if (!pedidos.length)
    return <div style={styles.center}>No hay pedidos registrados.</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>📦 Historial de Pedidos</h2>
      {pedidos.map((pedido) => (
        <div key={pedido.id} style={styles.orderBox}>
          <h3>Pedido #{pedido.id}</h3>
          <p>
            <strong>Dirección:</strong> {pedido.direccionEnvio}
          </p>
          <table style={styles.table}>
            <thead>
              <tr>
                <th>Imagen</th>
                <th>Producto</th>
                <th>Precio</th>
                <th>Cantidad</th>
                <th>Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {pedido.items.map((item, idx) => (
                <tr key={idx}>
                  <td>
                    <img
                      src={item.imagenUrl || "https://via.placeholder.com/50"}
                      alt={item.nombre}
                      style={styles.image}
                    />
                  </td>
                  <td>{item.nombre}</td>
                  <td>₲ {item?.precio ? item.precio.toLocaleString() : "-"}</td>
                  <td>
                    ₲{" "}
                    {item?.precio && item?.cantidad
                      ? (item.precio * item.cantidad).toLocaleString()
                      : "-"}
                  </td>

                  <td>₲ {(item.precio * item.cantidad).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
    </div>
  );
};

const styles = {
  container: {
    padding: "1rem",
    fontFamily: "Arial, sans-serif",
  },
  title: {
    textAlign: "center",
    marginBottom: "1rem",
  },
  orderBox: {
    backgroundColor: "#fff",
    padding: "1rem",
    borderRadius: "8px",
    marginBottom: "1rem",
    boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "0.5rem",
    textAlign: "center",
  },
  image: {
    width: "50px",
    height: "50px",
    objectFit: "cover",
    borderRadius: "4px",
  },
  center: {
    textAlign: "center",
    marginTop: "2rem",
    fontSize: "1.1rem",
  },
};

export default Orders;
