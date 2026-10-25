export const labels = {
  BASICO: 'Básico', INTERMEDIO: 'Intermedio', AVANZADO: 'Avanzado',
  SELECCION_MULTIPLE: 'Selección múltiple', ORDENAR_BLOQUES: 'Ordenar bloques',
  PREDECIR_SALIDA: 'Predecir la salida', SELECCIONAR_FRAGMENTO: 'Seleccionar código',
  BORRADOR: 'Borrador', LISTO: 'Listo para usar', SALA_ESPERA: 'Sala de espera',
  EN_CURSO: 'En curso', PAUSADA: 'Pausada', FINALIZADA: 'Finalizada',
  PROPIA: 'Pregunta propia', COPIA: 'Copia privada', INSTITUCIONAL: 'Institucional',
  ESTUDIANTE_UTP: 'Estudiante UTP', DOCENTE_UTP: 'Docente UTP', USUARIO_EXTERNO: 'Autodidacta',
};
export const badgeInfo = {
  PRIMER_PASO: ['Primer paso', 'Completa tu primer micro-reto.', 'flag'],
  EXPLORADOR: ['Explorador', 'Completa un reto de cada curso.', 'compass'],
  DOMINIO_BASICO: ['Dominio básico', 'Completa los retos básicos de un tema.', 'shield'],
  PERSEVERANTE: ['Perseverante', 'Resuelve un reto tras tres o más intentos.', 'flame'],
  CURSO_COMPLETADO: ['Curso completado', 'Completa todos los retos de un curso.', 'award'],
};
export function esc(value = '') {
  return String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}
export function percent(done, total) { return total ? Math.min(100, Math.round(done / total * 100)) : 0; }
export function parseRoute(hash = '') {
  const [path, query = ''] = hash.replace(/^#/, '').split('?');
  return { path: path || '/', params: new URLSearchParams(query) };
}
export function formatTime(ms) { return `${((ms || 0) / 1000).toLocaleString('es-PE', { maximumFractionDigits: 1 })} s`; }
export function readStored(storage, key, fallback = null) {
  try { return JSON.parse(storage.getItem(key)) ?? fallback; } catch { return fallback; }
}
export function validSession(session, now = Date.now()) {
  return session?.token && session?.usuarioId && session.expiresAt > now ? session : null;
}
export function evaluationPayload(reto, answer, start, usoAyuda = false) {
  return { microRetoId: reto.id, alternativaId: reto.tipo === 'ORDENAR_BLOQUES' ? null : Number(answer),
    ordenBloques: reto.tipo === 'ORDENAR_BLOQUES' ? answer.map(Number) : null,
    usoAyuda, tiempoMs: Math.max(0, Date.now() - start) };
}
export function courseStyle(name = '') {
  if (/python/i.test(name)) return { key: 'python', icon: 'code', subtitle: 'Dale vida a tus ideas.', description: 'Explora expresiones, lógica y el lenguaje de las posibilidades.' };
  if (/datos/i.test(name)) return { key: 'database', icon: 'database', subtitle: 'Conecta la información.', description: 'Comprende tus datos y encuentra respuestas con SQL.' };
  return { key: 'algorithms', icon: 'workflow', subtitle: 'Piensa. Resuelve. Crea.', description: 'Construye las bases de la lógica y resuelve problemas paso a paso.' };
}
