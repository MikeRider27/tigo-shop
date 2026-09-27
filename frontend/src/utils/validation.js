// Mismas reglas que el backend (la validación del cliente es UX; la del servidor es la que protege).
export const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
export const PASSWORD_RE = /^(?=.*[A-Za-z])(?=.*\d).{8,72}$/;
export const PASSWORD_HINT = 'Mínimo 8 caracteres, con letras y números.';
export const MIN_AGE = 18;

/** Edad cumplida a la fecha `today` para una fecha "YYYY-MM-DD". */
export function ageOn(birthDate, today = new Date()) {
  const [y, m, d] = birthDate.split('-').map(Number);
  let age = today.getFullYear() - y;
  const month = today.getMonth() + 1;
  if (month < m || (month === m && today.getDate() < d)) age -= 1;
  return age;
}

/** Fecha máxima seleccionable en el date picker para cumplir la edad mínima. */
export function maxBirthDate(today = new Date()) {
  const d = new Date(today.getFullYear() - MIN_AGE, today.getMonth(), today.getDate());
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

const required = (value) => (value ?? '').toString().trim().length > 0;

export function validateProfile(f, today) {
  const errors = {};
  if (!required(f.firstName)) errors.firstName = 'Los nombres son obligatorios';
  if (!required(f.lastName)) errors.lastName = 'Los apellidos son obligatorios';
  if (!required(f.shippingAddress)) errors.shippingAddress = 'La dirección de envío es obligatoria';
  if (!required(f.birthDate)) errors.birthDate = 'La fecha de nacimiento es obligatoria';
  else if (ageOn(f.birthDate, today) < MIN_AGE) errors.birthDate = 'Debe ser mayor de 18 años';
  return errors;
}

export function validateRegister(f, today) {
  const errors = validateProfile(f, today);
  if (!required(f.email)) errors.email = 'El email es obligatorio';
  else if (!EMAIL_RE.test(f.email.trim())) errors.email = 'Formato de email inválido';
  if (!required(f.password)) errors.password = 'La contraseña es obligatoria';
  else if (!PASSWORD_RE.test(f.password)) errors.password = PASSWORD_HINT;
  if (f.password !== f.confirmPassword) errors.confirmPassword = 'Las contraseñas no coinciden';
  return errors;
}

export function validateNewPassword(password, confirm) {
  const errors = {};
  if (!PASSWORD_RE.test(password || '')) errors.newPassword = PASSWORD_HINT;
  if (password !== confirm) errors.confirmPassword = 'Las contraseñas no coinciden';
  return errors;
}
