import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Home = () => {
  const [usuario, setUsuario] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/"); // Redirigir al login si no hay token
      return;
    }

    axios
      .get(process.env.REACT_APP_AUTH_URL + "/auth/secure", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        setUsuario(res.data);
      })
      .catch(() => {
        alert("Sesión expirada. Por favor inicia sesión nuevamente.");
        localStorage.removeItem("token");
        navigate("/");
      });
  }, [navigate]);

  if (!usuario) {
    return <p style={{ textAlign: "center", marginTop: "100px" }}>Cargando...</p>;
  }

  return (
    <div style={styles.container}>
      <nav style={styles.navbar}>
        <span style={styles.logo}>🏋️‍♂️ Tienda Deportiva</span>
        <div>
          <button style={styles.link} onClick={() => navigate("/catalog")}>Catálogo</button>
          <button style={styles.link} onClick={() => navigate("/cart")}>Carrito</button>
          <button style={styles.link} onClick={() => navigate("/orders")}>Órdenes</button>
        </div>
      </nav>

      <div style={styles.content}>
        <h1>Bienvenido, {usuario.nombres} {usuario.apellidos}</h1>
        <p>Email: {usuario.email}</p>
        <p>Dirección de envío: {usuario.direccionEnvio}</p>
        <p>Fecha de nacimiento: {usuario.fechaNacimiento}</p>
      </div>
    </div>
  );
};

const styles = {
  container: {
    fontFamily: "Arial, sans-serif",
    padding: "20px"
  },
  navbar: {
    display: "flex",
    justifyContent: "space-between",
    padding: "10px 20px",
    backgroundColor: "#007bff",
    color: "white",
    borderRadius: "6px",
    marginBottom: "30px"
  },
  logo: {
    fontWeight: "bold"
  },
  link: {
    marginLeft: "15px",
    background: "white",
    border: "none",
    padding: "6px 12px",
    borderRadius: "4px",
    cursor: "pointer"
  },
  content: {
    textAlign: "center"
  }
};

export default Home;
