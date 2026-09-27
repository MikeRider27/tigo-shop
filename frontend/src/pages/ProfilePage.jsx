import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../api/endpoints';
import { Alert, Field } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { PASSWORD_HINT, maxBirthDate, validateNewPassword, validateProfile } from '../utils/validation';

function ProfileForm() {
  const { user, setUser } = useAuth();
  const [form, setForm] = useState({
    firstName: user.firstName,
    lastName: user.lastName,
    shippingAddress: user.shippingAddress,
    birthDate: user.birthDate,
  });
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState(null);
  const [saving, setSaving] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setStatus(null);
    const clientErrors = validateProfile(form);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length) return;
    setSaving(true);
    try {
      setUser(await userApi.update(form));
      setStatus({ tone: 'success', text: 'Tus datos se actualizaron correctamente.' });
    } catch (err) {
      setErrors(err.fieldErrors || {});
      setStatus({ tone: 'danger', text: err.message });
    } finally {
      setSaving(false);
    }
  };

  const field = (name) => ({ name, value: form[name], onChange, error: errors[name], required: true });

  return (
    <form className="card pad" onSubmit={onSubmit} noValidate>
      <h2>Datos personales</h2>
      {status && <Alert tone={status.tone}>{status.text}</Alert>}
      <Field label="Email" name="email" value={user.email} disabled hint="El email identifica tu cuenta y no se puede cambiar." />
      <div className="grid-2">
        <Field label="Nombres" autoComplete="given-name" {...field('firstName')} />
        <Field label="Apellidos" autoComplete="family-name" {...field('lastName')} />
      </div>
      <Field label="Dirección de envío" as="textarea" rows={2} {...field('shippingAddress')} />
      <Field label="Fecha de nacimiento" type="date" max={maxBirthDate()} {...field('birthDate')} />
      <button className="btn" disabled={saving}>{saving ? 'Guardando…' : 'Guardar cambios'}</button>
    </form>
  );
}

function PasswordForm() {
  const { renewSession } = useAuth();
  const empty = { currentPassword: '', newPassword: '', confirmPassword: '' };
  const [form, setForm] = useState(empty);
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState(null);
  const [saving, setSaving] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setStatus(null);
    const clientErrors = validateNewPassword(form.newPassword, form.confirmPassword);
    if (!form.currentPassword) clientErrors.currentPassword = 'Ingresa tu contraseña actual';
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length) return;
    setSaving(true);
    try {
      renewSession(await userApi.changePassword(form.currentPassword, form.newPassword));
      setForm(empty);
      setStatus({ tone: 'success', text: 'Contraseña actualizada. Se cerraron tus sesiones en otros dispositivos.' });
    } catch (err) {
      setStatus({ tone: 'danger', text: err.message });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="card pad" onSubmit={onSubmit} noValidate>
      <h2>Cambiar contraseña</h2>
      {status && <Alert tone={status.tone}>{status.text}</Alert>}
      <Field label="Contraseña actual" name="currentPassword" type="password" autoComplete="current-password"
             value={form.currentPassword} onChange={onChange} error={errors.currentPassword} />
      <div className="grid-2">
        <Field label="Nueva contraseña" name="newPassword" type="password" autoComplete="new-password"
               hint={PASSWORD_HINT} value={form.newPassword} onChange={onChange} error={errors.newPassword} />
        <Field label="Confirmar contraseña" name="confirmPassword" type="password" autoComplete="new-password"
               value={form.confirmPassword} onChange={onChange} error={errors.confirmPassword} />
      </div>
      <button className="btn" disabled={saving}>{saving ? 'Guardando…' : 'Actualizar contraseña'}</button>
    </form>
  );
}

function DeleteAccount() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');

  const onDelete = async () => {
    if (!window.confirm('Esta acción elimina tu cuenta de forma permanente. ¿Deseas continuar?')) return;
    try {
      await userApi.remove();
      logout();
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="card pad danger-zone">
      <h2>Eliminar cuenta</h2>
      <Alert>{error}</Alert>
      <p className="muted small">Tus órdenes se conservan por obligaciones contables, pero no podrás volver a ingresar.</p>
      <button className="btn btn-danger" onClick={onDelete}>Eliminar mi cuenta</button>
    </div>
  );
}

export default function ProfilePage() {
  return (
    <section className="profile">
      <h1>Mi perfil</h1>
      <ProfileForm />
      <PasswordForm />
      <DeleteAccount />
    </section>
  );
}
