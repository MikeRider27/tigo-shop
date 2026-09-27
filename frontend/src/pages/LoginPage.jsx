import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Alert, Field } from '../components/ui';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || '/';
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) return <Navigate to={from} replace />;

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(form.email.trim(), form.password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <form className="card auth-card" onSubmit={onSubmit} noValidate>
        <h1>Iniciar sesión</h1>
        <p className="muted">Ingresa para gestionar tu carrito y tus compras.</p>
        {location.state?.message && <Alert tone="success">{location.state.message}</Alert>}
        <Alert>{error}</Alert>
        <Field label="Email" name="email" type="email" autoComplete="email" required
               value={form.email} onChange={onChange} />
        <Field label="Contraseña" name="password" type="password" autoComplete="current-password" required
               value={form.password} onChange={onChange} />
        <button className="btn btn-block" disabled={submitting || !form.email || !form.password}>
          {submitting ? 'Ingresando…' : 'Ingresar'}
        </button>
        <div className="auth-links">
          <Link to="/forgot-password">¿Olvidaste tu contraseña?</Link>
          <span>
            ¿No tienes cuenta? <Link to="/register" state={{ from }}>Regístrate</Link>
          </span>
        </div>
      </form>
    </section>
  );
}
