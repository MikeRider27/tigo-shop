import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useCart } from "../context/CartContext"; // Importa el contexto del carrito

const Catalog = () => {
  const [articulos, setArticulos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { agregarAlCarrito } = useCart(); // Usa el método global para agregar al carrito

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/");
      return;
    }

    axios
      .get(`${process.env.REACT_APP_CATALOG_URL}/articulos`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        setArticulos(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error al cargar catálogo", err);
        setError("Error al cargar el catálogo.");
        setLoading(false);
        if (err.response?.status === 401) {
          localStorage.removeItem("token");
          navigate("/");
        }
      });
  }, [navigate]);

  const agregar = async (item) => {
    try {
      await agregarAlCarrito(item.id, 1); // Agrega 1 unidad al carrito
    } catch (err) {
      alert("No se pudo agregar al carrito");
      console.error(err);
    }
  };

  if (loading) return <div style={styles.centerText}>Cargando...</div>;
  if (error) return <div style={{ ...styles.centerText, color: "red" }}>{error}</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Catálogo de Artículos</h2>
      <div style={styles.grid}>
        {articulos.map((item) => (
          <div key={item.id} style={styles.card}>
            <img
              src={item.imagenUrl || "https://via.placeholder.com/150"}
              alt={item.nombre}
              style={styles.image}
              onError={(e) => {
                e.target.src = "https://via.placeholder.com/150";
              }}
            />
            <h4 style={styles.name}>{item.nombre}</h4>
            <p style={styles.description}>{item.descripcion}</p>
            <p style={styles.price}>₲ {item.precio.toLocaleString()}</p>
            <p style={styles.stock}>Stock: {item.stock}</p>
            <button style={styles.button} onClick={() => agregar(item)}>
              Agregar
            </button>
          </div>
        ))}
      </div>
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
  grid: {
    display: "grid",
    gap: "1rem",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
  },
  card: {
    border: "1px solid #ccc",
    borderRadius: "8px",
    padding: "1rem",
    textAlign: "center",
    boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
    backgroundColor: "#fff",
  },
  image: {
    width: "100%",
    height: "160px",
    objectFit: "cover",
    marginBottom: "0.5rem",
    borderRadius: "4px",
  },
  name: {
    margin: "0.5rem 0 0.25rem",
    fontSize: "1rem",
  },
  description: {
    fontSize: "0.9rem",
    margin: "0 0 0.25rem",
    color: "#555",
  },
  price: {
    fontWeight: "bold",
    margin: "0.25rem 0",
  },
  stock: {
    fontSize: "0.85rem",
    color: "#666",
  },
  button: {
    marginTop: "0.5rem",
    padding: "0.5rem 1rem",
    border: "none",
    backgroundColor: "#28a745",
    color: "#fff",
    borderRadius: "4px",
    cursor: "pointer",
    fontSize: "0.9rem",
  },
  centerText: {
    textAlign: "center",
    marginTop: "3rem",
    fontSize: "1.1rem",
  },
};

export default Catalog;
