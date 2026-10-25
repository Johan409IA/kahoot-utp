export class ApiError extends Error {
  constructor(message, status) { super(message); this.status = status; }
}
export function createApi({ getSession = () => null, fetcher = globalThis.fetch, onUnauthorized = () => {}, demo = null } = {}) {
  return async function api(path, { method = 'GET', body, signal } = {}) {
    if (demo) return demo(path, { method, body });
    const token = getSession()?.token;
    let response;
    try {
      response = await fetcher(`/api${path}`, { method,
        headers: { Accept: 'application/json', ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}), ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        ...(body !== undefined ? { body: JSON.stringify(body) } : {}), signal: signal || AbortSignal.timeout(12000) });
    } catch (error) {
      if (error.name === 'AbortError') throw error;
      throw new ApiError('No pudimos conectar con AdaptCode. Comprueba que el servidor esté disponible y vuelve a intentar.', 0);
    }
    const raw = await response.text();
    let data;
    try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
    if (!response.ok) {
      if (response.status === 401 && token) onUnauthorized();
      const message = typeof data === 'object' && data ? (data.error || data.mensaje || Object.values(data).join(' ')) : `No se pudo completar la solicitud (${response.status}).`;
      throw new ApiError(message, response.status);
    }
    return data;
  };
}
