import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../api/endpoints';
import { Alert, Field } from '../components/ui';
import { EMAIL_RE } from '../utils/validation';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [fieldError, setFieldError] = useState('');
  const [error, setError] = useState('');
  const [sentMessage, setSentMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!EMAIL_RE.test(email.trim())) {
      setFieldError('Ingresa un email válido');
      return;
    }
    setSubmitting(true);
    try {
      const res = await authApi.forgotPassword(email.trim());
      setSentMessage(res.message);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <form className="card auth-card" onSubmit={onSubmit} noValidate>
        <h1>Recuperar contraseña</h1>
        {sentMessage ? (
          <>
            <Alert tone="success">{sentMessage}</Alert>
            <p className="muted small">
              Entorno de demo: los correos se pueden ver en <a href="http://localhost:8025" target="_blank" rel="noreferrer">Mailpit (localhost:8025)</a>.
            </p>
          </>
        ) : (
          <>
            <p className="muted">Te enviaremos un enlace para crear una nueva contraseña.</p>
            <Alert>{error}</Alert>
            <Field label="Email" name="email" type="email" autoComplete="email" required value={email}
                   error={fieldError} onChange={(e) => { setEmail(e.target.value); setFieldError(''); }} />
            <button className="btn btn-block" disabled={submitting}>
              {submitting ? 'Enviando…' : 'Enviar enlace'}
            </button>
          </>
        )}
        <div className="auth-links">
          <Link to="/login">Volver a iniciar sesión</Link>
        </div>
      </form>
    </section>
  );
}
