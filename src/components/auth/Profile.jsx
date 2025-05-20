import React, { useEffect, useState } from "react";
import axios from "axios";

const Profile = () => {
  const [form, setForm] = useState({
    nombres: "",
    apellidos: "",
    direccionEnvio: "",
    fechaNacimiento: "",
  });

  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem("token");

  useEffect(() => {
    axios
      .get(`${process.env.REACT_APP_AUTH_URL}/auth/secure`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        setForm(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error al cargar datos del usuario", err);
        setLoading(false);
      });
  }, [token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleUpdate = async () => {
    try {
      await axios.put(
        `${process.env.REACT_APP_AUTH_URL}/auth/update`,
        form,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      alert("✅ Datos actualizados correctamente");
    } catch (err) {
      alert("❌ Error al actualizar perfil");
      console.error(err);
    }
  };

  if (loading) return <p style={{ textAlign: "center" }}>Cargando perfil...</p>;

  return (
    <div style={styles.container}>
      <h2>🧍‍♂️ Mi Perfil</h2>
      <div style={styles.formGroup}>
        <label>Nombres:</label>
        <input name="nombres" value={form.nombres} onChange={handleChange} />
      </div>
      <div style={styles.formGroup}>
        <label>Apellidos:</label>
        <input name="apellidos" value={form.apellidos} onChange={handleChange} />
      </div>
      <div style={styles.formGroup}>
        <label>Dirección de Envío:</label>
        <input name="direccionEnvio" value={form.direccionEnvio} onChange={handleChange} />
      </div>
      <div style={styles.formGroup}>
        <label>Fecha de Nacimiento:</label>
        <input name="fechaNacimiento" type="date" value={form.fechaNacimiento} onChange={handleChange} />
      </div>
      <button style={styles.button} onClick={handleUpdate}>
        💾 Guardar Cambios
      </button>
    </div>
  );
};

const styles = {
  container: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "10px",
    maxWidth: "500px",
    margin: "0 auto",
  },
  formGroup: {
    marginBottom: "15px",
  },
  button: {
    backgroundColor: "#007bff",
    color: "#fff",
    padding: "10px 16px",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
};

export default Profile;
