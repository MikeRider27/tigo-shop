import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Home = () => {
  const [usuario, setUsuario] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/");
      return;
    }

    axios
      .get(process.env.REACT_APP_AUTH_URL + "/auth/secure", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => setUsuario(res.data))
      .catch(() => {
        alert("Sesión expirada. Por favor inicia sesión nuevamente.");
        localStorage.removeItem("token");
        navigate("/");
      });
  }, [navigate]);

  if (!usuario) {
    return <p style={styles.loading}>Cargando...</p>;
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Bienvenido, {usuario.nombres} {usuario.apellidos}</h2>
      <p style={styles.text}><strong>Email:</strong> {usuario.email}</p>
      <p style={styles.text}><strong>Dirección de envío:</strong> {usuario.direccionEnvio}</p>
      <p style={styles.text}><strong>Fecha de nacimiento:</strong> {usuario.fechaNacimiento}</p>
    </div>
  );
};

const styles = {
  loading: {
    textAlign: "center",
    marginTop: "100px",
    fontSize: "1.1rem"
  },
  container: {
    textAlign: "center",
    padding: "30px"
  },
  title: {
    fontSize: "1.8rem",
    marginBottom: "20px"
  },
  text: {
    marginBottom: "10px",
    fontSize: "1rem",
    color: "#333"
  }
};

export default Home;
