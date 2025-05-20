import React from "react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as Yup from "yup";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const schema = Yup.object().shape({
  nombres: Yup.string().required("Campo obligatorio"),
  apellidos: Yup.string().required("Campo obligatorio"),
  direccion: Yup.string().required("Campo obligatorio"),
  email: Yup.string().email("Formato inválido").required("Campo obligatorio"),
  fechaNacimiento: Yup.date()
    .required("Campo obligatorio")
    .test("edad", "Debes ser mayor de 18", function (value) {
      const hoy = new Date();
      const nacimiento = new Date(value);
      const edad = hoy.getFullYear() - nacimiento.getFullYear();
      return edad >= 18;
    }),
  password: Yup.string().min(6).required("Campo obligatorio"),
});

const Register = () => {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema) });

  const onSubmit = async (data) => {
    try {
      await axios.post(
        process.env.REACT_APP_AUTH_URL + "/auth/register",
        data
      );
      alert("Registro exitoso");
      navigate("/");
    } catch (err) {
      alert("Error al registrar: " + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Registro</h2>
      <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
        <input placeholder="Nombres" {...register("nombres")} style={styles.input} />
        <p style={styles.error}>{errors.nombres?.message}</p>

        <input placeholder="Apellidos" {...register("apellidos")} style={styles.input} />
        <p style={styles.error}>{errors.apellidos?.message}</p>

        <input placeholder="Dirección de Envío" {...register("direccion")} style={styles.input} />
        <p style={styles.error}>{errors.direccion?.message}</p>

        <input type="email" placeholder="Email" {...register("email")} style={styles.input} />
        <p style={styles.error}>{errors.email?.message}</p>

        <input type="date" {...register("fechaNacimiento")} style={styles.input} />
        <p style={styles.error}>{errors.fechaNacimiento?.message}</p>

        <input type="password" placeholder="Password" {...register("password")} style={styles.input} />
        <p style={styles.error}>{errors.password?.message}</p>

        <button type="submit" style={styles.button}>Registrarse</button>
        <p style={styles.linkText}>
          ¿Ya tienes cuenta? <a href="/">Inicia sesión</a>
        </p>
      </form>
    </div>
  );
};

const styles = {
  container: {
    maxWidth: "400px",
    margin: "80px auto",
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
    marginBottom: "6px",
    borderRadius: "4px",
    border: "1px solid #ccc",
    fontSize: "14px"
  },
  button: {
    padding: "10px",
    backgroundColor: "#007bff",
    color: "#fff",
    fontWeight: "bold",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
    marginTop: "10px"
  },
  error: {
    color: "red",
    fontSize: "12px",
    margin: "0 0 10px",
    textAlign: "left"
  },
  linkText: {
    marginTop: "10px",
    fontSize: "14px"
  }
};

export default Register;
