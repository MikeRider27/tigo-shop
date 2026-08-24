import React, { useEffect } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";

const Layout = () => {
  const navigate = useNavigate();
  const { cantidadTotal, refrescarCantidad } = useCart();

  useEffect(() => {
    refrescarCantidad();
  }, [refrescarCantidad]);

  return (
    <div style={styles.layout}>
      {/* Sidebar */}
      <aside style={styles.sidebar}>
        <div style={styles.sidebarHeader}>
          <h2 style={styles.appName}>
            🛒 <strong>App Tienda</strong>
          </h2>
        </div>
        <ul style={styles.menu}>
          {/* Secciones principales */}
          <li style={styles.menuItem} onClick={() => navigate("/home")}>
            🏠 Inicio
          </li>
          <li style={styles.menuItem} onClick={() => navigate("/profile")}>
            👤 Mi Perfil
          </li>
          <li
            style={styles.menuItem}
            onClick={() => navigate("/change-password")}
          >
            🔐 Cambiar Contraseña
          </li>

          {/* Funcionalidades del sistema */}
          <li style={styles.menuItem} onClick={() => navigate("/catalog")}>
            🛍️ Catálogo
          </li>

          <li style={styles.menuItem} onClick={() => navigate("/cart")}>
            🧺 Carrito
            {cantidadTotal > 0 && (
              <span style={styles.badge}>{cantidadTotal}</span>
            )}
          </li>

          <li style={styles.menuItem} onClick={() => navigate("/pedidos")}>
            📦 Mis Pedidos
          </li>
          <li style={styles.menuItem} onClick={() => navigate("/order")}>
            📋 Historial de Pedidos
          </li>

          {/* Salida */}
          <li
            style={{ ...styles.menuItem, color: "#ff4d4f", fontWeight: "bold" }}
            onClick={() => {
              localStorage.removeItem("token");
              navigate("/");
            }}
          >
            🚪 Cerrar sesión
          </li>
        </ul>
      </aside>

      {/* Área principal */}
      <div style={styles.main}>
        <header style={styles.header}>
          <h3>Panel Principal</h3>
        </header>
        <main style={styles.content}>
          <div style={styles.contentBox}>
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

const styles = {
  layout: {
    display: "flex",
    height: "100vh",
    fontFamily: "Segoe UI, sans-serif",
    backgroundColor: "#f0f2f5",
  },
  sidebar: {
    width: "220px",
    backgroundColor: "#002b5c",
    color: "white",
    display: "flex",
    flexDirection: "column",
    padding: "20px 10px",
  },
  sidebarHeader: {
    marginBottom: "30px",
    textAlign: "center",
  },
  appName: {
    fontSize: "1.2rem",
    margin: 0,
  },
  menu: {
    listStyle: "none",
    padding: 0,
    margin: 0,
    flexGrow: 1,
  },
  menuItem: {
    padding: "10px 12px",
    margin: "6px 0",
    borderRadius: "6px",
    cursor: "pointer",
    transition: "background 0.2s",
    position: "relative",
  },
  badge: {
    backgroundColor: "red",
    color: "white",
    borderRadius: "50%",
    padding: "2px 6px",
    fontSize: "0.75rem",
    marginLeft: "6px",
  },
  main: {
    flexGrow: 1,
    display: "flex",
    flexDirection: "column",
  },
  header: {
    padding: "10px 20px",
    backgroundColor: "#007bff",
    color: "white",
    fontWeight: "bold",
    boxShadow: "0 1px 4px rgba(0,0,0,0.1)",
  },
  content: {
    padding: "20px",
    overflowY: "auto",
    flexGrow: 1,
    backgroundColor: "#f0f2f5",
  },
  contentBox: {
    backgroundColor: "white",
    padding: "20px",
    borderRadius: "8px",
    boxShadow: "0 2px 6px rgba(0,0,0,0.05)",
  },
};

export default Layout;
