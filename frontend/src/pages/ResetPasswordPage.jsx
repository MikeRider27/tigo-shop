import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/endpoints';
import { Alert, Field } from '../components/ui';
import { PASSWORD_HINT, validateNewPassword } from '../utils/validation';

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token');
  const navigate = useNavigate();
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!token) {
    return (
      <section className="auth-page">
        <div className="card auth-card">
          <Alert>El enlace de recuperación no es válido.</Alert>
          <Link to="/forgot-password">Solicitar un nuevo enlace</Link>
        </div>
      </section>
    );
  }

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const clientErrors = validateNewPassword(form.newPassword, form.confirmPassword);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length) return;
    setSubmitting(true);
    try {
      await authApi.resetPassword(token, form.newPassword);
      navigate('/login', { replace: true, state: { message: 'Contraseña actualizada. Ya puedes iniciar sesión.' } });
    } catch (err) {
      setError(err.message);
      setSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <form className="card auth-card" onSubmit={onSubmit} noValidate>
        <h1>Nueva contraseña</h1>
        <Alert>{error}</Alert>
        {error && <Link to="/forgot-password">Solicitar un nuevo enlace</Link>}
        <Field label="Nueva contraseña" name="newPassword" type="password" autoComplete="new-password"
               hint={PASSWORD_HINT} value={form.newPassword} onChange={onChange} error={errors.newPassword} />
        <Field label="Confirmar contraseña" name="confirmPassword" type="password" autoComplete="new-password"
               value={form.confirmPassword} onChange={onChange} error={errors.confirmPassword} />
        <button className="btn btn-block" disabled={submitting}>
          {submitting ? 'Guardando…' : 'Guardar contraseña'}
        </button>
      </form>
    </section>
  );
}
