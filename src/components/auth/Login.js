import React from "react";
import axios from "axios";
import { login } from "../../services/authService";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";

const Login = () => {
  const { register, handleSubmit } = useForm();
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    try {
      const res = await login(data);
      localStorage.setItem("token", res.data.token);
      navigate("/home");
    } catch (err) {
      alert("Credenciales inválidas");
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Iniciar Sesión</h2>
      <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
        <input
          type="email"
          placeholder="Email"
          {...register("email")}
          style={styles.input}
        />
        <input
          type="password"
          placeholder="Password"
          {...register("password")}
          style={styles.input}
        />
        <button type="submit" style={styles.button}>Ingresar</button>
        <p style={styles.linkText}>
          ¿No tienes cuenta? <a href="/register">Regístrate</a>
        </p>
      </form>
    </div>
  );
};

const styles = {
  container: {
    maxWidth: "350px",
    margin: "100px auto",
    padding: "20px",
    border: "1px solid #ccc",
    borderRadius: "8px",
    fontFamily: "Arial, sans-serif",
    textAlign: "center"
  },
  title: {
    marginBottom: "20px"
  },
  form: {
    display: "flex",
    flexDirection: "column"
  },
  input: {
    padding: "10px",
    marginBottom: "10px",
    borderRadius: "4px",
    border: "1px solid #ccc",
    fontSize: "14px"
  },
  button: {
    padding: "10px",
    backgroundColor: "#28a745",
    color: "#fff",
    fontWeight: "bold",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer"
  },
  linkText: {
    marginTop: "10px",
    fontSize: "14px"
  }
};

export default Login;
