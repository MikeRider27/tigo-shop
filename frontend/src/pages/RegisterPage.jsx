import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Alert, Field } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { PASSWORD_HINT, maxBirthDate, validateRegister } from '../utils/validation';

const INITIAL = {
  firstName: '',
  lastName: '',
  shippingAddress: '',
  email: '',
  birthDate: '',
  password: '',
  confirmPassword: '',
};

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const from = useLocation().state?.from || '/';
  const [form, setForm] = useState(INITIAL);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated && !submitting) return <Navigate to={from} replace />;

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: undefined });
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const clientErrors = validateRegister(form);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length) return;

    setSubmitting(true);
    try {
      const { confirmPassword, ...payload } = form;
      await register({ ...payload, email: payload.email.trim() });
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message);
      setErrors(err.fieldErrors || {});
      setSubmitting(false);
    }
  };

  const field = (name) => ({ name, value: form[name], onChange, error: errors[name], required: true });

  return (
    <section className="auth-page">
      <form className="card auth-card auth-card-wide" onSubmit={onSubmit} noValidate>
        <h1>Crear cuenta</h1>
        <p className="muted">Todos los campos son obligatorios.</p>
        <Alert>{error}</Alert>
        <div className="grid-2">
          <Field label="Nombres" autoComplete="given-name" {...field('firstName')} />
          <Field label="Apellidos" autoComplete="family-name" {...field('lastName')} />
        </div>
        <Field label="Dirección de envío" as="textarea" rows={2} autoComplete="street-address"
               {...field('shippingAddress')} />
        <div className="grid-2">
          <Field label="Email" type="email" autoComplete="email" {...field('email')} />
          <Field label="Fecha de nacimiento" type="date" max={maxBirthDate()} hint="Debes ser mayor de 18 años."
                 {...field('birthDate')} />
        </div>
        <div className="grid-2">
          <Field label="Contraseña" type="password" autoComplete="new-password" hint={PASSWORD_HINT}
                 {...field('password')} />
          <Field label="Confirmar contraseña" type="password" autoComplete="new-password"
                 {...field('confirmPassword')} />
        </div>
        <button className="btn btn-block" disabled={submitting}>
          {submitting ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
        <div className="auth-links">
          <span>
            ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
          </span>
        </div>
      </form>
    </section>
  );
}
