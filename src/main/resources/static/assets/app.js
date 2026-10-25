import { createApi } from './api.js';
import { esc, percent, labels, badgeInfo, courseStyle, readStored, validSession, parseRoute } from './core.js';
import { icon } from './icons.js';
import { renderPractice } from './practice.js';
import { renderTeacher } from './teacher.js';

const demoMode = new URLSearchParams(location.search).get('demo') === '1';
const sessionKey = demoMode ? 'adaptcode.demo.session' : 'adaptcode.session';
const defaults = { tema: 'NORMAL', tamanoTexto: 'NORMAL', reduccionMovimiento: false, sonidoActivado: true, modoDaltonismo: false };
const ctx = { session: validSession(readStored(sessionStorage, sessionKey)), courses: [], retos: [], temas: [], progress: [], badges: [],
  preferences: { ...defaults, ...readStored(localStorage, 'adaptcode.preferences', {}) }, demoMode, icon, esc, labels };
let actions = {}, generation = 0, catalogLoaded = false, personalLoaded = false, catalogError = '', toastTimer;
let demo;
if (demoMode) { demo = (await import('./demo.js')).createDemo(); }
ctx.api = createApi({ getSession: () => ctx.session, demo, onUnauthorized: () => {
  ctx.session = null; ctx.progress = []; ctx.badges = []; sessionStorage.removeItem(sessionKey); personalLoaded = false;
  toast('Tu sesión terminó. Inicia sesión para continuar.');
  queueMicrotask(() => { if (!location.hash.startsWith('#/acceso')) location.hash = '/acceso'; });
} });
ctx.teacherPath = resource => `/docentes/${ctx.session.usuarioId}/${resource}`;
ctx.userPath = resource => `/usuarios/${ctx.session.usuarioId}/${resource}`;
ctx.isTeacher = () => ctx.session?.rol === 'DOCENTE_UTP';
ctx.toast = toast;
ctx.go = path => { if (location.hash === `#${path}`) render(); else location.hash = path; };
ctx.on = (name, fn) => { actions[name] = fn; };
ctx.bind = (selector, event, fn) => document.querySelector(selector)?.addEventListener(event, e => run(() => fn(e), e.submitter));
ctx.reloadPersonal = async () => { personalLoaded = false; await loadPersonal(); };
ctx.invalidateCatalog = () => { catalogLoaded = false; };
ctx.modal = modal;
ctx.empty = empty;
ctx.requireSession = () => {
  if (ctx.session) return true;
  ctx.go('/acceso'); toast('Inicia sesión para guardar tu aprendizaje.'); return false;
};
ctx.applyPreferences = applyPreferences;
applyPreferences();

function toast(message) {
  const box = document.querySelector('#toast'); box.textContent = message; box.classList.add('show');
  clearTimeout(toastTimer); toastTimer = setTimeout(() => box.classList.remove('show'), 5500);
}
async function run(fn, button) {
  if (button?.disabled) return;
  if (button) button.disabled = true;
  try { await fn(); } catch (error) { if (error.name !== 'AbortError') toast(error.message || 'No se pudo completar la acción.'); }
  finally { if (button?.isConnected) button.disabled = false; }
}
function modal(title, content, submit) {
  const dialog = document.querySelector('#modal');
  if (dialog.open) dialog.close();
  dialog.innerHTML = `<div class="modal-heading"><h2 id="modal-title">${esc(title)}</h2><button class="icon-button" data-close aria-label="Cerrar ventana">${icon('close')}</button></div>${content}`;
  dialog.querySelector('[data-close]').onclick = () => dialog.close();
  if (submit) dialog.querySelector('form').addEventListener('submit', async e => {
    e.preventDefault(); const button = e.submitter; button.disabled = true;
    const alert = dialog.querySelector('[role="alert"]');
    if (alert) alert.textContent = '';
    try { if (await submit(new FormData(e.target), e.target) !== false) dialog.close(); }
    catch (error) { if (alert) alert.textContent = error.message; else toast(error.message); }
    finally { if (button.isConnected) button.disabled = false; }
  });
  dialog.showModal(); return dialog;
}
function applyPreferences() {
  const p = ctx.preferences;
  document.documentElement.dataset.contrast = p.tema === 'ALTO_CONTRASTE' ? 'high' : 'normal';
  document.documentElement.dataset.text = p.tamanoTexto;
  document.documentElement.dataset.motion = p.reduccionMovimiento ? 'reduced' : 'normal';
  document.documentElement.dataset.color = p.modoDaltonismo ? 'accessible' : 'normal';
  localStorage.setItem('adaptcode.preferences', JSON.stringify(p));
}
async function loadCatalog() {
  if (catalogLoaded) return;
  const courses = await ctx.api('/cursos');
  const groups = await Promise.all(courses.map(async course => {
    const [temas, retos] = await Promise.all([ctx.api(`/cursos/${course.id}/temas`), ctx.api(`/micro-retos?cursoId=${course.id}`)]);
    return { temas: temas.map(t => ({ ...t, cursoId: course.id })), retos: retos.map(r => ({ ...r, cursoId: course.id })) };
  }));
  ctx.courses = courses; ctx.temas = groups.flatMap(g => g.temas); ctx.retos = groups.flatMap(g => g.retos);
  catalogLoaded = true; catalogError = '';
}
async function loadPersonal() {
  if (!ctx.session || personalLoaded) return;
  const [progress, badges, preferences] = await Promise.all([
    ctx.api(ctx.userPath('progreso')), ctx.api(ctx.userPath('insignias')),
    ctx.api(ctx.userPath('preferencias')).catch(e => { if (e.status === 404) return null; throw e; }),
  ]);
  ctx.progress = progress; ctx.badges = badges;
  if (preferences) { ctx.preferences = { ...defaults, ...preferences }; applyPreferences(); }
  personalLoaded = true;
}
function empty(title, description, action = '') {
  return `<div class="empty">${icon('compass')}<h3>${esc(title)}</h3><p>${esc(description)}</p>${action}</div>`;
}
function shell(path) {
  const isTeacher = ctx.isTeacher(), logged = !!ctx.session;
  const nav = [['/', 'home', 'Mi inicio'], ['/cursos', 'book', 'Mis cursos'], ['/retos', 'code', 'Micro-retos'], ['/progreso', 'chart', 'Mi progreso'], ['/insignias', 'award', 'Mis insignias']];
  if (isTeacher) nav.push(['/docente/preguntas', 'edit', 'Mis preguntas'], ['/docente/quizzes', 'book', 'Mis quizzes'], ['/docente/salas', 'users', 'Mis salas']);
  else if (ctx.session?.rol !== 'USUARIO_EXTERNO') nav.push(['/salas', 'users', 'Salas en vivo']);
  const name = ctx.session?.nombres || 'Visitante';
  const selected = path.startsWith('/reto/') ? '/retos' : path.startsWith('/docente/preguntas/') ? '/docente/preguntas' : path.startsWith('/docente/salas/') ? '/docente/salas' : path;
  return `<aside class="sidebar" id="sidebar"><button class="icon-button sidebar-dismiss" data-action="close-menu" aria-label="Cerrar navegación">${icon('close')}</button><a class="brand" href="#/" aria-label="AdaptCode UTP, inicio"><span class="brand-symbol">${icon('code')}</span><span>Adapt<span class="brand-code">Code</span><small>UTP · APRENDE A TU RITMO</small></span></a>
    <div class="workspace-label">TU ESPACIO DE APRENDIZAJE</div><nav aria-label="Navegación principal">${nav.map(([url,i,label]) => `<a href="#${url}" class="nav-link ${selected === url ? 'active' : ''}" ${selected === url ? 'aria-current="page"' : ''}>${icon(i)}<span>${label}</span>${url === '/salas' ? '<span class="live-dot"></span>' : ''}</a>`).join('')}</nav>
    <div class="sidebar-bottom"><div class="sidebar-note">${icon('bulb')}<strong>Cada intento cuenta.</strong><p>Equivocarte también es parte de aprender.</p></div><a class="nav-link ${path === '/ajustes' ? 'active' : ''}" href="#/ajustes">${icon('settings')}<span>Accesibilidad</span></a><button class="nav-link" data-action="help">${icon('help')}<span>¿Necesitas una mano?</span></button></div>
    <div class="institution"><span class="utp-mark">UTP</span><span>Universidad Tecnológica<br>del Perú</span></div></aside>
    <div class="page"><header class="topbar"><div class="breadcrumb"><button class="icon-button menu-toggle" data-action="menu" aria-label="Abrir navegación" aria-expanded="false">${icon('menu')}</button><span>Mi espacio</span>${icon('chevron')}<strong>${esc(nav.find(n => n[0] === selected)?.[2] || (path === '/ajustes' ? 'Accesibilidad' : path === '/acceso' ? 'Bienvenido' : 'Práctica'))}</strong></div>
      <div class="topbar-actions"><span class="learning-label">${icon('shield')} Un espacio para aprender</span><button class="profile" data-action="${logged ? 'profile' : 'login'}"><span class="avatar">${esc(name.charAt(0).toUpperCase())}</span><span><strong>${esc(name.split(' ')[0])}</strong><small>${esc(logged ? labels[ctx.session.rol] : 'Explora y aprende')}</small></span>${icon('chevron')}</button></div></header>
      ${demoMode ? '<div class="demo-banner">Vista de demostración · Datos de ejemplo guardados en este navegador. <a href="/">Salir de la demo</a></div>' : ''}
      <main id="main" tabindex="-1"><div class="loading" role="status"><span class="spinner"></span> Cargando tu espacio…</div></main><footer class="footer"><span>Hecho para aprender. Diseñado para todos.</span><span>AdaptCode UTP <span class="footer-dot">·</span> ${new Date().getFullYear()}</span></footer></div>`;
}
function courseCards() {
  return ctx.courses.map((c, index) => {
    const style = courseStyle(c.nombre), retos = ctx.retos.filter(r => r.cursoId === c.id);
    const done = ctx.progress.filter(p => p.completado && retos.some(r => r.id === p.microRetoId)).length;
    return `<article class="course-card ${style.key}"><div class="course-cover"><span class="course-number">RUTA 0${index + 1}</span><div class="course-art">${icon(style.icon)}</div><span class="course-cover-label">${esc(style.subtitle)}</span><span class="art-grid" aria-hidden="true"></span></div><div class="course-body"><div class="row"><h3>${esc(c.nombre)}</h3><span class="course-mini-icon">${icon(style.icon)}</span></div><p>${esc(style.description)}</p><div class="course-meta">${icon('book')} ${ctx.temas.filter(t => t.cursoId === c.id).length} ${ctx.temas.filter(t => t.cursoId === c.id).length === 1 ? 'tema' : 'temas'} <span>·</span> ${retos.length} ${retos.length === 1 ? 'micro-reto' : 'micro-retos'}</div><div class="row progress-label"><span>${ctx.session ? `${done} de ${retos.length} completados` : 'Tu próximo paso comienza aquí'}</span><strong>${ctx.session ? `${percent(done, retos.length)}%` : ''}</strong></div><progress value="${done}" max="${retos.length || 1}" aria-label="Progreso en ${esc(c.nombre)}"></progress><a class="course-link" href="#/retos?curso=${c.id}">Explorar curso ${icon('arrow')}</a></div></article>`;
  }).join('');
}
function warning() {
  return catalogError ? `<div class="notice warning" role="alert">${icon('alert')}<div><strong>El catálogo no está disponible</strong><p>${esc(catalogError)}</p><button class="text-button" data-action="retry">Volver a intentar</button> <a href="/?demo=1#/">Explorar la demo</a></div></div>` : '';
}
function activityCard() {
  if (ctx.isTeacher()) return `<section class="join-card"><span class="tag">${icon('users')} TU PRÓXIMA CLASE</span><h3>Ideas que se comparten.</h3><p>Prepara un quiz, crea una sala e invita a tu clase a aprender.</p><a class="button dark" href="#/docente/salas">Gestionar mis salas ${icon('arrow')}</a><a class="text-link" href="#/docente/quizzes">Preparar un quiz →</a></section>`;
  if (ctx.session?.rol === 'USUARIO_EXTERNO') return `<section class="join-card"><span class="tag">${icon('compass')} APRENDE A TU RITMO</span><h3>Tu curiosidad marca el camino.</h3><p>Explora los tres cursos y practica sin límite de intentos.</p><a class="button dark" href="#/retos">Elegir mi próximo reto ${icon('arrow')}</a></section>`;
  return `<section class="join-card"><span class="tag">${icon('users')} APRENDE EN EQUIPO</span><h3>¿Tu clase ya empezó?</h3><p>Ingresa el PIN de tu docente y comparte el reto.</p><form id="quick-pin"><label class="sr-only" for="quick-pin-input">PIN de la sala</label><input id="quick-pin-input" name="pin" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" placeholder="PIN de 6 dígitos" required><button class="button dark" type="submit">Unirme a una sala ${icon('arrow')}</button></form><span class="join-footnote">${icon('shield')} Para estudiantes UTP</span></section>`;
}
function renderHome(view) {
  const firstName = ctx.session?.nombres.split(' ')[0], done = ctx.progress.filter(p => p.completado).length;
  const points = ctx.progress.filter(p => p.puntoDominio).length;
  const firstBadge = ctx.badges.length ? badgeInfo[ctx.badges[ctx.badges.length - 1].tipo] : null;
  view(`<section class="page-heading"><div><div class="eyebrow">UN POCO DE PRÁCTICA, MUCHAS POSIBILIDADES</div><h1>${firstName ? `¡Hola, ${esc(firstName)}!` : '¡Qué bueno verte por aquí!'} <span class="greeting-star">✳</span></h1><p>Es un buen día para aprender algo nuevo. Vamos a tu ritmo.</p></div><span class="date-label">${new Intl.DateTimeFormat('es-PE', { weekday: 'long', day: 'numeric', month: 'long', timeZone: 'America/Lima' }).format(new Date())}</span></section>
    <section class="hero"><div class="hero-copy"><span class="hero-tag"><span></span> TU CÓDIGO. TU CAMINO.</span><h2>Pequeños retos.<br><em>Grandes aprendizajes.</em></h2><p>Practica, descubre por qué y vuelve a intentarlo.<br>Aquí, comprender es lo que más cuenta.</p><a class="button light" href="#/retos">Explorar micro-retos ${icon('arrow')}</a><div class="hero-footnote">${icon('check')} Sin presión de tiempo ${icon('check')} A tu propio ritmo</div></div>
    <div class="hero-art" aria-hidden="true"><span class="orbit orbit-one"></span><span class="orbit orbit-two"></span><span class="floating-symbol symbol-one">{ }</span><span class="floating-symbol symbol-two">+</span><div class="code-window"><div class="code-window-bar"><i></i><i></i><i></i><span>mi_aprendizaje.py</span></div><div class="code-lines"><span class="line-no">01</span><span><b>while</b> aprendiendo:</span><span class="line-no">02</span><span>  intentar<span class="code-blue">()</span></span><span class="line-no">03</span><span>  <b>if</b> hay_error:</span><span class="line-no">04</span><span>    comprender<span class="code-blue">()</span></span><span class="line-no">05</span><span>  <b>else</b>:</span><span class="line-no">06</span><span>    seguir_creciendo<span class="code-blue">()</span></span></div><div class="code-terminal"><span>❯</span> Cada intento es un avance<span class="cursor"></span></div></div><div class="success-sticker">${icon('check')} ¡Vas por buen camino!</div><span class="tiny-star">✳</span></div></section>
    <section class="stats-grid" aria-label="Resumen de tu aprendizaje">${[
      ['check', 'Retos completados', ctx.session ? done : '—', ctx.session ? 'Cada uno es un paso adelante' : 'Inicia sesión para ver tu avance', 'mint'],
      ['spark', 'Puntos de dominio', ctx.session ? points : '—', 'Comprender tiene su recompensa', 'blue'],
      ['award', 'Insignias obtenidas', ctx.session ? ctx.badges.length : '—', 'Pequeñas victorias, grandes motivos', 'peach'],
    ].map(([i,title,value,sub,color]) => `<article class="stat-card"><span class="stat-icon ${color}">${icon(i)}</span><div><span>${title}</span><strong>${value}<small>${i === 'spark' ? ' pts' : ''}</small></strong><p>${sub}</p></div></article>`).join('')}</section>
    ${warning()}<div class="dashboard-columns"><section><div class="section-heading"><div><h2>Tu próxima gran idea empieza aquí</h2><p>Tres caminos para poner en práctica lo que sabes.</p></div><a class="text-link" href="#/cursos">Ver cursos ${icon('arrow')}</a></div><div class="courses-grid">${courseCards() || empty('Pronto habrá más por descubrir', 'Los cursos aparecerán aquí cuando el catálogo esté disponible.')}</div></section>
    <aside class="home-aside">${activityCard()}
    <section class="milestone-card"><div class="row"><h3>${ctx.badges.length ? 'Tu último logro' : 'Tu siguiente logro'}</h3>${icon('spark')}</div><div class="milestone-icon">${icon(firstBadge?.[2] || 'flag')}</div><strong>${esc(firstBadge?.[0] || 'El primer paso')}</strong><p>${firstBadge ? 'Ya es parte de tu historia. Sigue explorando.' : 'Todo empieza con un intento. Completa tu primer micro-reto.'}</p><a class="text-link" href="#/insignias">Conoce las insignias ${icon('arrow')}</a></section></aside></div>
    <section class="encouragement">${icon('bulb')}<p><strong>No tienes que saberlo todo para empezar.</strong> Solo necesitas curiosidad y ganas de intentarlo.</p><span>Un reto a la vez.</span></section>`);
  ctx.bind('#quick-pin', 'submit', e => { e.preventDefault(); ctx.go(`/salas?pin=${new FormData(e.target).get('pin')}`); });
}
function renderCatalog(view, params, coursesOnly = false) {
  if (coursesOnly) { view(`<div class="page-heading"><div><div class="eyebrow">ELIGE TU CAMINO</div><h1>Mis cursos</h1><p>Construye tu conocimiento, un concepto a la vez.</p></div></div>${warning()}<div class="courses-grid full-courses">${courseCards() || empty('El catálogo está vacío', 'Aún no hay cursos disponibles.')}</div>`); return; }
  const course = params.get('curso') || '', tema = params.get('tema') || '', level = params.get('nivel') || '', search = params.get('q') || '';
  const filtered = ctx.retos.filter(r => (!course || String(r.cursoId) === course) && (!tema || String(r.temaId) === tema) && (!level || r.nivel === level) && (!search || r.enunciado.toLocaleLowerCase('es').includes(search.toLocaleLowerCase('es'))));
  view(`<div class="page-heading"><div><div class="eyebrow">PRACTICAR ES DESCUBRIR</div><h1>Un reto para cada idea.</h1><p>Elige qué quieres aprender. Los intentos son ilimitados.</p></div><span class="pill">${ctx.retos.length} micro-retos disponibles</span></div>${warning()}
    <form id="filters" class="filter-bar"><label>Curso<select name="curso"><option value="">Todos los cursos</option>${ctx.courses.map(c => `<option value="${c.id}" ${String(c.id) === course ? 'selected' : ''}>${esc(c.nombre)}</option>`).join('')}</select></label><label>Tema<select name="tema"><option value="">Todos los temas</option>${ctx.temas.filter(t => !course || String(t.cursoId) === course).map(t => `<option value="${t.id}" ${String(t.id) === tema ? 'selected' : ''}>${esc(t.nombre)}</option>`).join('')}</select></label><label>Nivel<select name="nivel"><option value="">Todos los niveles</option>${['BASICO','INTERMEDIO','AVANZADO'].map(l => `<option value="${l}" ${l === level ? 'selected' : ''}>${labels[l]}</option>`).join('')}</select></label><label class="search-field">Buscar un reto<div>${icon('search')}<input type="search" name="q" value="${esc(search)}" placeholder="¿Qué quieres practicar?"></div></label><button class="button dark" type="submit">Filtrar</button></form>
    <div class="section-heading"><h2>${filtered.length} ${filtered.length === 1 ? 'reto para explorar' : 'retos para explorar'}</h2><a class="text-link" href="#/retos">Limpiar filtros</a></div><div class="challenge-grid">${filtered.map(r => {
      const c = ctx.courses.find(c => c.id === r.cursoId), s = courseStyle(c?.nombre), p = ctx.progress.find(p => p.microRetoId === r.id);
      return `<article class="challenge-card"><div class="row"><span class="stat-icon ${s.key}">${icon(s.icon)}</span><span class="pill ${p?.completado ? 'completed' : ''}">${p?.completado ? '✓ Completado' : labels[r.nivel]}</span></div><span class="overline">${esc(c?.nombre)} · ${esc(ctx.temas.find(t => t.id === r.temaId)?.nombre)}</span><h3>${esc(r.enunciado)}</h3><span class="challenge-type">${icon('code')} ${labels[r.tipo]}</span><a class="course-link" href="#/reto/${r.id}">${p?.completado ? 'Volver a practicar' : 'Practicar ahora'} ${icon('arrow')}</a></article>`;
    }).join('') || empty('No encontramos retos con esos filtros', 'Prueba con otro tema, nivel o palabra.')}</div>`);
  ctx.bind('#filters', 'submit', e => { e.preventDefault(); const q = new URLSearchParams(new FormData(e.target)); ctx.go(`/retos?${q}`); });
  ctx.bind('[name="curso"]', 'change', e => { const data = new FormData(document.querySelector('#filters')); data.set('tema', ''); ctx.go(`/retos?${new URLSearchParams(data)}`); });
}
function renderProgress(view) {
  if (!ctx.session) return view(authPrompt('Tu aprendizaje merece su propio espacio.', 'Inicia sesión para ver tus retos completados y puntos de dominio.'));
  view(`<div class="page-heading"><div><div class="eyebrow">MIRA TODO LO QUE ESTÁS CONSTRUYENDO</div><h1>Mi progreso</h1><p>Tu avance se mide en lo que comprendes, un reto a la vez.</p></div><a class="button dark" href="#/retos">Seguir practicando ${icon('arrow')}</a></div><div class="courses-grid full-courses">${courseCards()}</div><div class="section-heading"><h2>Tu recorrido</h2><span>Un punto de dominio por reto resuelto sin solución</span></div><div class="panel">${ctx.progress.length ? `<div class="table-scroll"><table><thead><tr><th>Micro-reto</th><th>Resultado</th><th>Dominio</th><th></th></tr></thead><tbody>${ctx.progress.map(p => `<tr><td>${esc(ctx.retos.find(r => r.id === p.microRetoId)?.enunciado || `Micro-reto ${p.microRetoId}`)}</td><td>${p.completado ? (p.resueltoConAyuda ? 'Completado con ayuda' : 'Completado') : 'En práctica'}</td><td>${p.puntoDominio ? '✓ 1 punto' : '—'}</td><td><a class="text-link" href="#/reto/${p.microRetoId}">Ver reto ${icon('arrow')}</a></td></tr>`).join('')}</tbody></table></div>` : empty('Tu historia está por empezar', 'Completa un micro-reto y verás tu progreso aquí.')}</div>`);
}
function renderBadges(view) {
  view(`<div class="page-heading"><div><div class="eyebrow">CADA AVANCE TIENE SU HISTORIA</div><h1>Pequeñas victorias.</h1><p>Insignias que celebran tu aprendizaje y tu perseverancia.</p></div><span class="pill">${ctx.badges.length} de 5 obtenidas</span></div>${!ctx.session ? authPrompt('Tus logros te esperan.', 'Inicia sesión para empezar a coleccionar tus insignias.') : ''}<div class="badge-grid">${Object.entries(badgeInfo).map(([key,[title,description,i]]) => {
    const obtained = ctx.badges.some(b => b.tipo === key);
    return `<article class="badge-card ${obtained ? 'earned' : ''}"><div class="badge-emblem">${icon(i)}</div><span class="pill">${obtained ? '✓ Obtenida' : 'Por descubrir'}</span><h2>${title}</h2><p>${description}</p></article>`;
  }).join('')}</div><p class="muted">Las insignias se conceden cuando el servidor confirma que cumpliste su condición.</p>`);
}
function authPrompt(title, text) {
  return `<div class="auth-prompt">${icon('shield')}<div><h2>${title}</h2><p>${text}</p></div><a class="button dark" href="#/acceso">Iniciar sesión ${icon('arrow')}</a></div>`;
}
function renderAuth(view, mode = 'login', data = {}) {
  const titles = { login: ['¡Sigamos aprendiendo!', 'Tu siguiente descubrimiento está a un inicio de sesión.'], registro: ['Tu camino empieza aquí.', 'Crea tu cuenta y aprende a tu propio ritmo.'], verificar: ['Un paso más y listo.', 'Confirma tu cuenta con el código de seis dígitos.'], recuperar: ['Volvamos a tu espacio.', 'Solicita un código para restablecer tu contraseña.'], restablecer: ['Una nueva contraseña.', 'Usa el código de recuperación para continuar.'] };
  const [title,subtitle] = titles[mode];
  view(`<div class="auth-layout"><section class="auth-story"><span class="hero-tag">APRENDE. COMPRENDE. CRECE.</span><h1>Lo que hoy parece un reto,<br><em>mañana será parte de ti.</em></h1><div class="auth-braces">{ <span>tú puedes</span> }</div><p>Algoritmos, Python y Bases de Datos.<br>Un espacio accesible para intentarlo las veces que necesites.</p><a class="text-link" href="#/cursos">Explorar los cursos ${icon('arrow')}</a></section><section class="auth-panel"><div class="eyebrow">BIENVENIDO A ADAPTCODE UTP</div><h2>${title}</h2><p>${subtitle}</p><form id="auth-form" class="stack-form">
      ${mode === 'registro' ? '<div class="form-grid"><label>Nombres<input name="nombres" autocomplete="given-name" maxlength="100" required></label><label>Apellidos<input name="apellidos" autocomplete="family-name" maxlength="100" required></label></div><div class="form-grid"><label>Género<select name="genero" required><option value="" disabled selected>Selecciona</option><option value="MASCULINO">Masculino</option><option value="FEMENINO">Femenino</option></select></label><label>Fecha de nacimiento<input type="date" name="fechaNacimiento" max="' + new Date().toISOString().slice(0, 10) + '" required></label></div>' : ''}
      <label>Correo electrónico<input type="email" name="correo" autocomplete="username" maxlength="254" value="${esc(data.correo)}" placeholder="tu.correo@utp.edu.pe" required></label>
      ${['verificar','restablecer'].includes(mode) ? '<label>Código de verificación<input name="codigo" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" maxlength="6" placeholder="6 dígitos" required></label>' : ''}
      ${['login','registro','restablecer'].includes(mode) ? `<label>${mode === 'restablecer' ? 'Nueva contraseña' : 'Contraseña'}<div class="password-field"><input type="password" name="${mode === 'restablecer' ? 'nuevaContrasena' : 'contrasena'}" autocomplete="${mode === 'login' ? 'current-password' : 'new-password'}" ${mode !== 'login' ? 'minlength="8" maxlength="72"' : ''} required><button class="text-button" type="button" id="toggle-password" aria-label="Mostrar contraseña">Mostrar</button></div></label>` : ''}
      ${mode === 'registro' ? '<p class="field-hint">Estudiante: U + 8 dígitos. Docente: C + 5 dígitos. También puedes usar un correo personal para práctica autónoma.</p>' : ''}
      <div class="form-error" role="alert"></div><button type="submit" class="button dark">${({login:'Iniciar sesión',registro:'Crear mi cuenta',verificar:'Verificar cuenta',recuperar:'Solicitar código',restablecer:'Guardar contraseña'})[mode]} ${icon('arrow')}</button></form>
      ${data.codigo ? `<div class="notice info"><div>Código recibido de la API para esta versión de desarrollo: <strong>${esc(data.codigo)}</strong></div></div>` : ''}
      <div class="auth-links">${mode === 'login' ? '<button class="text-button" data-action="auth-register">Crear una cuenta</button><button class="text-button" data-action="auth-recover">Olvidé mi contraseña</button><button class="text-button" data-action="auth-verify">Verificar mi cuenta</button>' : '<button class="text-button" data-action="auth-login">Ya tengo una cuenta</button>'}${mode === 'verificar' ? '<button class="text-button" data-action="resend">Reenviar código</button>' : ''}</div>
      ${demoMode ? '<div class="demo-choices"><p>Explora con una cuenta de demostración</p><button class="button secondary" data-action="demo-student">Vista estudiante</button><button class="button secondary" data-action="demo-teacher">Vista docente</button></div>' : '<p class="demo-link">¿Quieres conocer la plataforma? <a href="/?demo=1#/acceso">Explorar demo</a></p>'}</section></div>`);
  const show = (next, values = {}) => renderAuth(view, next, values);
  ctx.on('auth-register', () => show('registro')); ctx.on('auth-recover', () => show('recuperar')); ctx.on('auth-verify', () => show('verificar')); ctx.on('auth-login', () => show('login'));
  ctx.on('resend', async () => {
    const correo = document.querySelector('[name="correo"]').value;
    if (!correo || !document.querySelector('[name="correo"]').reportValidity()) return;
    const result = await ctx.api('/auth/reenviar-codigo', { method: 'POST', body: { correo } });
    show('verificar', { correo, codigo: result.codigo }); toast(result.mensaje);
  });
  for (const role of ['student','teacher']) ctx.on(`demo-${role}`, async () => { await signIn({ correo: role === 'teacher' ? 'c12345@utp.edu.pe' : 'u12345678@utp.edu.pe', contrasena: 'Demostracion123' }); });
  ctx.bind('#toggle-password', 'click', e => {
    const input = e.target.parentElement.querySelector('input'); input.type = input.type === 'password' ? 'text' : 'password'; e.target.textContent = input.type === 'password' ? 'Mostrar' : 'Ocultar';
  });
  ctx.bind('#auth-form', 'submit', async e => {
    e.preventDefault(); const button = e.submitter; button.disabled = true;
    const body = Object.fromEntries(new FormData(e.target));
    try {
      if (mode === 'login') return await signIn(body);
      const route = { registro: 'registro', verificar: 'verificar-cuenta', recuperar: 'recuperar-contrasena', restablecer: 'restablecer-contrasena' }[mode];
      const result = await ctx.api(`/auth/${route}`, { method: 'POST', body });
      if (mode === 'registro') show('verificar', { correo: body.correo, codigo: result.codigoVerificacion });
      else if (mode === 'recuperar') { toast(result.mensaje); show('restablecer', { correo: body.correo, codigo: result.codigo }); }
      else { toast(result.mensaje); show('login', { correo: body.correo }); }
    } catch (error) { e.target.querySelector('[role="alert"]').textContent = error.message; }
    finally { if (button.isConnected) button.disabled = false; }
  });
}
async function signIn(body) {
  const result = await ctx.api('/auth/login', { method: 'POST', body });
  ctx.session = { ...result, expiresAt: Date.now() + result.expiraEnMs };
  sessionStorage.setItem(sessionKey, JSON.stringify(ctx.session));
  ctx.progress = []; ctx.badges = []; personalLoaded = false; await loadPersonal();
  if (!ctx.session.genero || !ctx.session.fechaNacimiento) return ctx.go('/completar-perfil');
  ctx.go('/');
}
function renderCompleteProfile(view) {
  if (!ctx.session) return view(authPrompt('Completa tu perfil para continuar.', 'Inicia sesión para registrar tu género y fecha de nacimiento.'));
  if (ctx.session.genero && ctx.session.fechaNacimiento) { ctx.go('/'); return; }
  view(`<div class="page-heading"><div><div class="eyebrow">UN ÚLTIMO PASO</div><h1>Completa tu perfil.</h1><p>Solo nos faltan dos datos: tu género y tu fecha de nacimiento.</p></div></div>
    <section class="panel"><form id="complete-profile" class="stack-form"><div class="form-grid">
      <label>Género<select name="genero" required><option value="" disabled selected>Selecciona</option><option value="MASCULINO">Masculino</option><option value="FEMENINO">Femenino</option></select></label>
      <label>Fecha de nacimiento<input type="date" name="fechaNacimiento" max="${new Date().toISOString().slice(0, 10)}" required></label>
    </div><div class="form-error" role="alert"></div><button type="submit" class="button dark">Guardar y continuar ${icon('arrow')}</button></form></section>`);
  ctx.bind('#complete-profile', 'submit', async e => {
    e.preventDefault();
    const body = Object.fromEntries(new FormData(e.target));
    const result = await ctx.api(ctx.userPath('perfil'), { method: 'PUT', body });
    ctx.session = { ...ctx.session, genero: result.genero, fechaNacimiento: result.fechaNacimiento };
    sessionStorage.setItem(sessionKey, JSON.stringify(ctx.session));
    toast('Perfil completado. ¡Que disfrutes!');
    ctx.go('/');
  });
}
function renderPreferences(view) {
  const p = ctx.preferences;
  view(`<div class="page-heading"><div><div class="eyebrow">UN ESPACIO QUE SE ADAPTA A TI</div><h1>Aprende a tu manera.</h1><p>Configura la experiencia para sentirte más cómodo.</p></div></div><section class="panel settings-panel"><form id="preferences" class="stack-form"><div class="form-grid"><label>Tema visual<select name="tema"><option value="NORMAL" ${p.tema === 'NORMAL' ? 'selected' : ''}>Normal</option><option value="ALTO_CONTRASTE" ${p.tema === 'ALTO_CONTRASTE' ? 'selected' : ''}>Alto contraste</option></select></label><label>Tamaño del texto<select name="tamanoTexto">${['NORMAL','GRANDE','MUY_GRANDE'].map((v,i) => `<option value="${v}" ${p.tamanoTexto === v ? 'selected' : ''}>${['Normal','Grande','Muy grande'][i]}</option>`).join('')}</select></label></div>${[['reduccionMovimiento','Reducir movimiento','Evita animaciones y transiciones.'],['sonidoActivado','Permitir sonido','Activa la señal breve al completar un reto.'],['modoDaltonismo','Diferenciación de colores','Usa una paleta azul y naranja, con etiquetas e iconos.']].map(([key,title,desc]) => `<label class="switch-row"><span><strong>${title}</strong><small>${desc}</small></span><input type="checkbox" name="${key}" ${p[key] ? 'checked' : ''} role="switch"></label>`).join('')}<p class="muted">${ctx.session ? 'Las preferencias se guardarán en tu cuenta y en este navegador.' : 'Las preferencias se guardarán en este navegador. Puedes configurarlas antes de iniciar sesión.'}</p><div class="form-error" role="alert"></div><button type="submit" class="button dark">Guardar preferencias ${icon('check')}</button></form></section>`);
  ctx.bind('#preferences', 'submit', async e => {
    e.preventDefault(); const f = new FormData(e.target), p = { tema: f.get('tema'), tamanoTexto: f.get('tamanoTexto'), reduccionMovimiento: f.has('reduccionMovimiento'), sonidoActivado: f.has('sonidoActivado'), modoDaltonismo: f.has('modoDaltonismo') };
    if (ctx.session) await ctx.api(ctx.userPath('preferencias'), { method: 'PUT', body: p });
    ctx.preferences = p; applyPreferences(); toast('Tu espacio ya está configurado.');
  });
}
async function renderStudentRoom(view, params) {
  if (!ctx.session) return view(authPrompt('El aprendizaje también se comparte.', 'Inicia sesión con tu cuenta de estudiante UTP para unirte a una sala.'));
  if (ctx.session.rol !== 'ESTUDIANTE_UTP') return view(empty('Salas para estudiantes UTP', 'Las cuentas externas tienen acceso a toda la práctica autónoma.', '<a class="button dark" href="#/retos">Explorar micro-retos</a>'));
  const pin = params.get('pin') || '';
  let sala, participants = [], ranking = [], participation;
  if (pin) {
    sala = await ctx.api(`/salas/por-pin?pin=${encodeURIComponent(pin)}`);
    [participants, ranking] = await Promise.all([ctx.api(`/salas/${sala.id}/participaciones`), ctx.api(`/salas/${sala.id}/ranking`)]);
    participation = participants.find(p => p.estudianteId === ctx.session.usuarioId);
  }
  view(`<div class="page-heading"><div><div class="eyebrow">COMPARTIR EL RETO, MULTIPLICAR LAS IDEAS</div><h1>Salas en vivo</h1><p>Tu clase, un PIN y muchas ganas de aprender.</p></div></div><section class="room-join panel"><span class="stat-icon mint">${icon('users')}</span><h2>Encuentra a tu clase.</h2><p>Ingresa los seis dígitos que te comparte tu docente.</p><form id="room-pin" class="pin-form"><label class="sr-only" for="pin">PIN de sala</label><input id="pin" name="pin" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" value="${esc(pin)}" required placeholder="000000"><button class="button dark" type="submit">Buscar sala ${icon('arrow')}</button></form></section>
    ${sala ? `<section class="panel room-detail"><div class="row"><div><span class="eyebrow">SALA ENCONTRADA</span><h2>PIN <span class="pin-code">${esc(sala.pin)}</span></h2></div><span class="pill">${labels[sala.estado]}</span></div><p>${participants.filter(p => !p.expulsado).length} participantes · ${sala.tiempoPorPregunta} segundos por pregunta</p>${participation?.expulsado ? '<div class="notice warning">Tu participación está suspendida. Consulta con tu docente.</div>' : participation ? `<div class="notice info">${icon('check')} ${sala.estado === 'SALA_ESPERA' ? 'Ya estás en la sala. Espera las indicaciones de tu docente.' : sala.estado === 'FINALIZADA' ? 'La sala terminó. Consulta la clasificación.' : 'Tu participación está registrada. Sigue las indicaciones de tu docente.'}</div>` : sala.estado === 'SALA_ESPERA' ? '<button class="button dark" data-action="join-room">Unirme a esta sala</button>' : '<div class="notice warning">El ingreso está disponible únicamente durante la sala de espera.</div>'}<button class="button secondary" data-action="refresh-room">${icon('refresh')} Actualizar sala</button><div class="section-heading"><h3>Clasificación de la sala</h3><span>Los aciertos cuentan. El tiempo desempata.</span></div>${rankingTable(ranking)}</section>` : ''}`);
  ctx.bind('#room-pin', 'submit', e => { e.preventDefault(); ctx.go(`/salas?pin=${new FormData(e.target).get('pin')}`); });
  ctx.on('join-room', async () => { await ctx.api(`/salas/${sala.id}/participaciones`, { method: 'POST', body: { estudianteId: ctx.session.usuarioId } }); ctx.go(`/salas?pin=${sala.pin}`); });
  ctx.on('refresh-room', () => ctx.go(`/salas?pin=${sala.pin}`));
}
export function rankingTable(ranking) {
  return ranking.length ? `<div class="table-scroll"><table><thead><tr><th>Posición</th><th>Estudiante</th><th>Puntos</th><th>Tiempo</th></tr></thead><tbody>${ranking.map(r => `<tr><td><span class="rank-number">${r.posicion}</span></td><td>${esc(r.nombre)} ${esc(r.apellidoInicial)}</td><td>${r.puntos}</td><td>${((r.tiempoMs || 0) / 1000).toLocaleString('es-PE')} s</td></tr>`).join('')}</tbody></table></div>` : empty('La clasificación está por empezar', 'Los resultados aparecerán después de las respuestas.');
}

async function render() {
  const current = ++generation;
  actions = {};
  const { path, params } = parseRoute(location.hash);
  if (ctx.session && !validSession(ctx.session)) { ctx.session = null; sessionStorage.removeItem(sessionKey); personalLoaded = false; ctx.progress = []; ctx.badges = []; }
  document.querySelector('#app').innerHTML = shell(path);
  document.title = `AdaptCode UTP · ${path === '/' ? 'Mi inicio' : 'Tu espacio de aprendizaje'}`;
  const view = html => { if (current === generation) { document.querySelector('#main').innerHTML = html; } };
  ctx.on('login', () => ctx.go('/acceso'));
  ctx.on('menu', () => {
    const open = document.querySelector('#sidebar').classList.toggle('open');
    document.querySelector('.menu-toggle').setAttribute('aria-expanded', String(open));
    document.querySelector('.page').inert = open;
    if (open) document.querySelector('.sidebar-dismiss').focus();
  });
  ctx.on('close-menu', closeMenu);
  ctx.on('profile', () => modal('Tu espacio', `<p><strong>${esc(ctx.session.nombres)} ${esc(ctx.session.apellidos)}</strong><br>${esc(ctx.session.correo)}</p><p>${labels[ctx.session.rol]}</p><a class="button secondary" href="#/ajustes" id="profile-settings">Preferencias de accesibilidad</a><button class="button dark" id="logout">${icon('logout')} Cerrar sesión</button>`).querySelector('#logout').addEventListener('click', () => {
    ctx.session = null; sessionStorage.removeItem(sessionKey); ctx.progress = []; ctx.badges = []; personalLoaded = false; document.querySelector('#modal').close(); ctx.go('/');
  }));
  ctx.on('help', () => modal('Cada intento cuenta', '<div class="help-content"><h3>¿Por dónde empiezo?</h3><p>Explora un curso, filtra los micro-retos y elige uno. Inicia sesión para enviar respuestas y guardar tu progreso.</p><h3>¿Y si me equivoco?</h3><p>Lee la explicación y vuelve a intentarlo. Los reintentos son ilimitados. Desde el tercer intento, la API permite consultar la solución; usarla completa el reto con ayuda.</p><h3>¿Cómo entro a mi clase?</h3><p>Usa tu cuenta de estudiante UTP e ingresa el PIN durante la sala de espera.</p><h3>Hazlo a tu manera</h3><p>En Accesibilidad puedes ajustar el contraste, texto, movimiento y sonido.</p></div>'));
  ctx.on('retry', async () => { catalogLoaded = false; await render(); });
  try {
    if (path === '/acceso') { renderAuth(view); return; }
    if (path === '/ajustes') { await loadPersonal(); renderPreferences(view); return; }
    if (path === '/completar-perfil') { renderCompleteProfile(view); return; }
    await Promise.all([loadCatalog().catch(e => { catalogError = e.message; }), loadPersonal()]);
    if (current !== generation) return;
    if (path === '/') renderHome(view);
    else if (path === '/cursos') renderCatalog(view, params, true);
    else if (path === '/retos') renderCatalog(view, params);
    else if (/^\/reto\/\d+$/.test(path)) await renderPractice({ ...ctx, view, isCurrent: () => current === generation }, Number(path.split('/')[2]));
    else if (path === '/progreso') renderProgress(view);
    else if (path === '/insignias') renderBadges(view);
    else if (path === '/salas') await renderStudentRoom(view, params);
    else if (path.startsWith('/docente/')) {
      if (!ctx.requireSession()) return;
      if (!ctx.isTeacher()) return view(empty('Este espacio es para docentes UTP', 'Puedes continuar aprendiendo en el catálogo de micro-retos.'));
      await renderTeacher({ ...ctx, view, isCurrent: () => current === generation }, path, params);
    } else view(empty('Este camino todavía no existe', 'Regresa al inicio y sigue explorando.', '<a class="button dark" href="#/">Volver al inicio</a>'));
  } catch (error) { if (current === generation) view(`<div class="notice warning" role="alert">${icon('alert')}<div><h2>No pudimos cargar este espacio</h2><p>${esc(error.message)}</p><button class="button secondary" data-action="retry">${icon('refresh')} Volver a intentar</button></div></div>`); }
}
document.addEventListener('click', e => {
  if (e.target.closest('.skip-link')) { e.preventDefault(); document.querySelector('#main')?.focus(); return; }
  const action = e.target.closest('[data-action]');
  if (action) run(() => actions[action.dataset.action]?.(action), action);
  if (e.target.closest('a[href^="#/"]')) { const dialog = document.querySelector('#modal'); if (dialog.open) dialog.close(); }
});
function closeMenu() {
  document.querySelector('#sidebar')?.classList.remove('open');
  if (document.querySelector('.page')) document.querySelector('.page').inert = false;
  const button = document.querySelector('.menu-toggle'); button?.setAttribute('aria-expanded', 'false'); button?.focus();
}
document.addEventListener('keydown', e => { if (e.key === 'Escape' && document.querySelector('#sidebar.open')) closeMenu(); });
matchMedia('(min-width: 761px)').addEventListener('change', e => {
  if (e.matches && document.querySelector('.page')?.inert) closeMenu();
});
window.addEventListener('hashchange', () => { render().then(() => { document.querySelector('#main')?.focus({ preventScroll: true }); window.scrollTo(0, 0); }); });
await render();
