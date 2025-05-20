import React, { useEffect, useState } from "react";

const OrdersTable = () => {
  const [ordenes, setOrdenes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return;

    const fetchOrders = async () => {
      try {
        const resUser = await fetch(`${process.env.REACT_APP_AUTH_URL}/auth/secure`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const user = await resUser.json();

        const resOrders = await fetch(`${process.env.REACT_APP_ORDER_URL}/ordenes/email/${user.email}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const data = await resOrders.json();
        setOrdenes(data);
      } catch (err) {
        console.error("Error al cargar las órdenes:", err);
        setError("No se pudieron cargar las órdenes");
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

  if (loading) return <div style={styles.center}>⏳ Cargando órdenes...</div>;
  if (error) return <div style={{ ...styles.center, color: "red" }}>{error}</div>;
  if (ordenes.length === 0) return <div style={styles.center}>📭 No tienes órdenes registradas.</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>📋 Historial de Órdenes</h2>
      <div style={styles.tableWrapper}>
        <table style={styles.table}>
          <thead>
            <tr>
              <th>Nro. Orden</th>
              <th>Estado</th>
              <th>Fecha</th>
              <th>Dirección</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {ordenes.map((orden) => {
              const total = orden.items.reduce(
                (acc, item) => acc + item.precioUnitario * item.cantidad,
                0
              );
              return (
                <tr key={orden.id}>
                  <td><strong>{orden.numeroOrden}</strong></td>
                  <td>
                    <span style={{ color: orden.estado === "pendiente" ? "orange" : "green" }}>
                      {orden.estado}
                    </span>
                  </td>
                  <td>{new Date(orden.fecha).toLocaleString()}</td>
                  <td>{orden.direccionEnvio}</td>
                  <td><strong>₲ {total.toLocaleString()}</strong></td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

const styles = {
    container: {
      padding: "2rem",
      backgroundColor: "#fff",
      borderRadius: "12px",
      boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
      maxWidth: "1000px",
      margin: "auto",
    },
    title: {
      textAlign: "center",
      marginBottom: "2rem",
      fontSize: "1.8rem",
      color: "#007bff",
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      gap: "0.5rem"
    },
    tableWrapper: {
      overflowX: "auto"
    },
    table: {
      width: "100%",
      borderCollapse: "separate",
      borderSpacing: "0 12px", // Espaciado entre filas
      textAlign: "left",
    },
    th: {
      padding: "12px 16px",
      fontWeight: "bold",
      color: "#333",
    },
    td: {
      padding: "12px 16px",
      backgroundColor: "#f9f9f9",
      borderRadius: "8px",
    },
    estadoPendiente: {
      color: "orange",
      fontWeight: "bold"
    },
    center: {
      textAlign: "center",
      marginTop: "2rem",
      fontSize: "1.2rem",
    }
  };
  

export default OrdersTable;
