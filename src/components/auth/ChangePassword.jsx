import React, { useState } from "react";
import axios from "axios";

const ChangePassword = () => {
  const [form, setForm] = useState({
    oldPassword: "",
    newPassword: "",
  });
  const [mensaje, setMensaje] = useState("");

  const token = localStorage.getItem("token");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const { oldPassword, newPassword } = form;
  
    try {
      const res = await fetch(`${process.env.REACT_APP_AUTH_URL}/auth/update-password`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ oldPassword, newPassword }),
      });
  
      if (!res.ok) throw new Error("Error al cambiar la contraseña");
  
      alert("Contraseña actualizada correctamente. Por favor, inicia sesión nuevamente.");
      localStorage.removeItem("token");
      window.location.href = "/";
    } catch (err) {
      setMensaje("Error al actualizar la contraseña");
      console.error(err);
    }
  };
  
  

  return (
    <div style={styles.container}>
      <h2>🔐 Cambiar Contraseña</h2>
      <div style={styles.formGroup}>
        <label>Contraseña actual:</label>
        <input
          type="password"
          name="oldPassword"
          value={form.oldPassword}
          onChange={handleChange}
          style={styles.input}
        />
      </div>
      <div style={styles.formGroup}>
        <label>Nueva contraseña:</label>
        <input
          type="password"
          name="newPassword"
          value={form.newPassword}
          onChange={handleChange}
          style={styles.input}
        />
      </div>
      <button onClick={handleSubmit} style={styles.button}>💾 Guardar</button>
      {mensaje && <p style={styles.message}>{mensaje}</p>}
    </div>
  );
};

const styles = {
  container: {
    backgroundColor: "#fff",
    padding: "20px",
    maxWidth: "500px",
    margin: "0 auto",
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
  },
  formGroup: {
    marginBottom: "15px",
  },
  input: {
    width: "100%",
    padding: "8px",
    border: "1px solid #ccc",
    borderRadius: "5px",
  },
  button: {
    backgroundColor: "#007bff",
    color: "#fff",
    padding: "10px 16px",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontSize: "1rem",
  },
  message: {
    marginTop: "10px",
    fontWeight: "bold",
  }
};

export default ChangePassword;
