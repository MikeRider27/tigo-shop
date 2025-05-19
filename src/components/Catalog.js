import React, { useEffect, useState } from "react";
import { getCatalog } from "../services/catalogService";
import { useNavigate } from "react-router-dom";

const Catalog = () => {
  const [articulos, setArticulos] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    getCatalog()
      .then((res) => setArticulos(res.data))
      .catch((err) => {
        console.error("Error al cargar catálogo", err);
        alert("Error al cargar catálogo");
        if (err.response?.status === 401) {
          localStorage.removeItem("token");
          navigate("/");
        }
      });
  }, [navigate]);

  const agregarAlCarrito = (item) => {
    alert(`"${item.nombre}" agregado al carrito`);
    // Luego conectaremos con el servicio del carrito
  };

  return (
    <div style={styles.container}>
      <h2>Catálogo de Artículos Deportivos</h2>
      <div style={styles.grid}>
        {articulos.map((item) => (
          <div key={item.id} style={styles.card}>
            <img
              src={item.imagenUrl}
              alt={item.nombre}
              style={styles.image}
              onError={(e) => {
                e.target.src = "https://via.placeholder.com/150";
              }}
            />
            <h4>{item.nombre}</h4>
            <p>{item.descripcion}</p>
            <p><strong>₲ {item.precio.toLocaleString()}</strong></p>
            <p>Stock disponible: {item.stock}</p>
            <button style={styles.button} onClick={() => agregarAlCarrito(item)}>
              Agregar al carrito
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

const styles = {
  container: {
    padding: "20px",
    fontFamily: "Arial, sans-serif",
  },
  grid: {
    display: "flex",
    flexWrap: "wrap",
    gap: "20px",
    justifyContent: "center"
  },
  card: {
    border: "1px solid #ccc",
    borderRadius: "8px",
    padding: "15px",
    width: "220px",
    textAlign: "center",
    boxShadow: "2px 2px 8px rgba(0,0,0,0.1)"
  },
  image: {
    width: "100%",
    height: "150px",
    objectFit: "contain",
    marginBottom: "10px"
  },
  button: {
    backgroundColor: "#28a745",
    color: "#fff",
    border: "none",
    padding: "8px 12px",
    borderRadius: "4px",
    cursor: "pointer"
  }
};

export default Catalog;
