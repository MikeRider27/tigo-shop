const TOKEN_KEY = 'tigo.accessToken';

export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

let unauthorizedHandler = () => {};

/** Se registra desde AuthContext: si el token expira, se cierra la sesión. */
export function onUnauthorized(handler) {
  unauthorizedHandler = handler;
}

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function saveToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    // almacenamiento no disponible (modo privado): la sesión dura lo que la pestaña
  }
}

/**
 * Wrapper de fetch: agrega el JWT, serializa JSON y convierte las respuestas de error
 * (formato RFC 7807 { detail, errors }) en ApiError.
 */
export async function api(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (auth && token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Verifique su conexión.');
  }

  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    if (response.status === 401 && auth && token) unauthorizedHandler();
    if (response.status === 429) {
      throw new ApiError(429, 'Demasiados intentos. Espere un momento e intente de nuevo.');
    }
    throw new ApiError(response.status, data?.detail || 'Ocurrió un error inesperado', data?.errors || {});
  }
  return data;
}
