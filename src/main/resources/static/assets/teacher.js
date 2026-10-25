import { esc, labels, formatTime } from './core.js';
import { icon } from './icons.js';

const errors = '<div class="form-error" role="alert"></div>';
const levelOptions = value => ['BASICO','INTERMEDIO','AVANZADO'].map(v => `<option value="${v}" ${v === value ? 'selected' : ''}>${labels[v]}</option>`).join('');
const typeOptions = value => ['SELECCION_MULTIPLE','ORDENAR_BLOQUES','PREDECIR_SALIDA','SELECCIONAR_FRAGMENTO'].map(v => `<option value="${v}" ${v === value ? 'selected' : ''}>${labels[v]}</option>`).join('');
const header = (title, description, action = '') => `<div class="page-heading"><div><div class="eyebrow">ESPACIO DOCENTE</div><h1>${title}</h1><p>${description}</p></div>${action}</div>`;
function confirmAction(ctx, title, description, action) {
  ctx.modal(title, `<form class="stack-form"><p>${esc(description)}</p>${errors}<button class="button dark" type="submit">Confirmar</button></form>`, action);
}
export async function renderTeacher(ctx, path) {
  const [, , section, rawId] = path.split('/'), id = rawId ? Number(rawId) : null;
  const refresh = () => ctx.go(path);
  const base = resource => ctx.teacherPath(resource);
  if (section === 'preguntas') {
    const [questions, quizzes] = await Promise.all([ctx.api(base('preguntas')), ctx.api(base('quizzes'))]);
    if (!ctx.isCurrent()) return;
    if (id) return questionDetail(ctx, id, quizzes, refresh);
    ctx.view(header('Ideas que se convierten en retos.', 'Crea preguntas y acompaña cada error con una explicación.', '<button class="button secondary" data-action="clone">Clonar del catálogo</button><button class="button dark" data-action="new-question">+ Crear pregunta</button>') +
      `<div class="challenge-grid">${questions.map(q => `<article class="challenge-card"><div class="row"><span class="pill">${labels[q.origen]}</span><span class="pill">${labels[q.nivel]}</span></div><span class="overline">${labels[q.tipo]}</span><h3>${esc(q.enunciado)}</h3><p>${esc(ctx.temas.find(t => t.id === q.temaId)?.nombre || 'Tema')}</p><a class="course-link" href="#/docente/preguntas/${q.id}">Configurar pregunta ${icon('arrow')}</a></article>`).join('') || ctx.empty('Tu banco de ideas está listo', 'Crea tu primera pregunta o clona una del catálogo institucional.')}</div>`);
    ctx.on('new-question', () => questionForm(ctx));
    ctx.on('clone', () => ctx.modal('Clonar un micro-reto', `<form class="stack-form"><p>Se creará una copia privada. Configura sus alternativas y retroalimentación antes de usarla en un quiz.</p><label>Micro-reto institucional<select name="id" required><option value="">Selecciona un reto</option>${ctx.retos.map(r => `<option value="${r.id}">${esc(r.enunciado)}</option>`).join('')}</select></label>${errors}<button class="button dark">Crear copia privada</button></form>`, async f => {
      const q = await ctx.api(base(`preguntas/${Number(f.get('id'))}/clonar`), { method: 'POST' }); ctx.go(`/docente/preguntas/${q.id}`);
    }));
    return;
  }
  if (section === 'quizzes') {
    const [quizzes, questions] = await Promise.all([ctx.api(base('quizzes')), ctx.api(base('preguntas'))]);
    if (!ctx.isCurrent()) return;
    ctx.view(header('Prepara tu próxima clase.', 'Reúne tus preguntas y dales un orden para aprender.', '<button class="button dark" data-action="new-quiz">+ Crear quiz</button>') +
      `<div class="challenge-grid">${quizzes.map(q => `<article class="challenge-card"><span class="pill ${q.estado === 'LISTO' ? 'completed' : ''}">${labels[q.estado]}</span><h3>${esc(q.titulo)}</h3><p>${q.items.length} ${q.items.length === 1 ? 'pregunta' : 'preguntas'}</p><ol class="quiz-preview">${q.items.slice(0,3).map(i => `<li>${esc(i.enunciado)}</li>`).join('')}</ol><div class="card-actions"><button class="button secondary" data-action="edit-quiz" data-id="${q.id}">${q.estado === 'LISTO' ? 'Ver quiz' : 'Editar'}</button>${q.estado === 'BORRADOR' ? `<button class="button dark" data-action="ready-quiz" data-id="${q.id}">Marcar listo</button><button class="text-button danger" data-action="delete-quiz" data-id="${q.id}">Eliminar</button>` : '<a class="text-link" href="#/docente/salas">Crear una sala →</a>'}</div></article>`).join('') || ctx.empty('Una nueva clase empieza con una idea', 'Crea un quiz con tus preguntas para preparar una sala.')}</div>`);
    ctx.on('new-quiz', () => quizForm(ctx, questions, null, refresh));
    ctx.on('edit-quiz', b => quizForm(ctx, questions, quizzes.find(q => q.id === Number(b.dataset.id)), refresh));
    ctx.on('ready-quiz', async b => { await ctx.api(base(`quizzes/${Number(b.dataset.id)}/listo`), { method: 'POST' }); ctx.toast('Quiz listo para usar.'); refresh(); });
    ctx.on('delete-quiz', b => confirmAction(ctx, 'Eliminar borrador', 'Este borrador se eliminará. Las preguntas de tu banco se conservan.', async () => { await ctx.api(base(`quizzes/${Number(b.dataset.id)}`), { method: 'DELETE' }); refresh(); }));
    return;
  }
  if (section === 'salas') {
    if (id) return roomDetail(ctx, id, refresh);
    const [rooms, quizzes] = await Promise.all([ctx.api(base('salas')), ctx.api(base('quizzes'))]);
    if (!ctx.isCurrent()) return;
    ctx.view(header('El reto se comparte.', 'Dirige tus sesiones y descubre cómo avanza tu clase.', '<button class="button dark" data-action="new-room">+ Crear sala</button>') +
      `<div class="challenge-grid">${rooms.map(s => `<article class="challenge-card"><div class="row"><span class="pill">${labels[s.estado]}</span>${icon('users')}</div><h3>${esc(quizzes.find(q => q.id === s.quizId)?.titulo || `Sala ${s.id}`)}</h3><div class="room-pin-label">PIN DE LA SALA<strong>${esc(s.pin)}</strong></div><p>${s.tiempoPorPregunta} s por pregunta · ${s.ampliacion === 'ESTANDAR' ? 'Tiempo estándar' : s.ampliacion === 'MAS_50' ? '+50 % de tiempo' : '+100 % de tiempo'}</p><a class="course-link" href="#/docente/salas/${s.id}">Gestionar sala ${icon('arrow')}</a></article>`).join('') || ctx.empty('Tu próxima clase te espera', 'Prepara un quiz listo para usar y crea tu primera sala.')}</div>`);
    ctx.on('new-room', () => ctx.modal('Prepara una sala', `<form class="stack-form"><label>Quiz listo para usar<select name="quizId" required><option value="">Selecciona un quiz</option>${quizzes.filter(q => q.estado === 'LISTO').map(q => `<option value="${q.id}">${esc(q.titulo)}</option>`).join('')}</select></label><div class="form-grid"><label>Tiempo por pregunta<select name="tiempoPorPregunta">${[20,30,60,90,120].map(t => `<option value="${t}" ${t === 60 ? 'selected' : ''}>${t} segundos</option>`).join('')}</select></label><label>Ampliación para toda la sala<select name="ampliacion"><option value="ESTANDAR">Estándar</option><option value="MAS_50">+50 %</option><option value="MAS_100">+100 %</option></select></label></div><p class="field-hint">Abre la sala de espera para que tus estudiantes puedan ingresar.</p>${errors}<button class="button dark">Crear sala y generar PIN</button></form>`, async f => {
      const s = await ctx.api(base('salas'), { method: 'POST', body: { quizId: Number(f.get('quizId')), tiempoPorPregunta: Number(f.get('tiempoPorPregunta')), ampliacion: f.get('ampliacion') } }); ctx.go(`/docente/salas/${s.id}`);
    }));
    return;
  }
  ctx.view(ctx.empty('Espacio no encontrado', 'Vuelve a tu banco de preguntas para continuar.'));
}
function questionForm(ctx, question) {
  ctx.modal(question ? 'Editar pregunta' : 'Una nueva idea', `<form class="stack-form"><label>Tema<select name="temaId" required><option value="">Selecciona un tema</option>${ctx.temas.map(t => `<option value="${t.id}" ${t.id === question?.temaId ? 'selected' : ''}>${esc(ctx.courses.find(c => c.id === t.cursoId)?.nombre)} · ${esc(t.nombre)}</option>`).join('')}</select></label><div class="form-grid"><label>Tipo<select name="tipo">${typeOptions(question?.tipo)}</select></label><label>Nivel<select name="nivel">${levelOptions(question?.nivel)}</select></label></div><label>Enunciado<textarea name="enunciado" maxlength="1000" rows="4" required>${esc(question?.enunciado)}</textarea></label>${errors}<button class="button dark">Guardar pregunta ${icon('check')}</button></form>`, async f => {
    const q = await ctx.api(ctx.teacherPath(`preguntas${question ? `/${question.id}` : ''}`), { method: question ? 'PUT' : 'POST', body: { temaId: Number(f.get('temaId')), tipo: f.get('tipo'), nivel: f.get('nivel'), enunciado: f.get('enunciado') } }); ctx.go(`/docente/preguntas/${q.id}`);
  });
}
async function questionDetail(ctx, id, quizzes, refresh) {
  const base = ctx.teacherPath(`preguntas/${id}`);
  const [question, alternatives, feedback] = await Promise.all([ctx.api(base), ctx.api(`${base}/alternativas`), ctx.api(`${base}/retroalimentaciones`)]);
  if (!ctx.isCurrent()) return;
  const locked = question.origen === 'INSTITUCIONAL' || quizzes.some(q => q.estado === 'LISTO' && q.items.some(i => i.preguntaId === id));
  ctx.view(`<a href="#/docente/preguntas" class="back-link">← Volver a mis preguntas</a>` + header('Dale forma a tu pregunta.', 'Cada alternativa es una oportunidad para explicar y comprender.', locked ? '<span class="pill">Solo lectura</span>' : '<button class="button secondary" data-action="edit-question">Editar enunciado</button><button class="text-button danger" data-action="delete-question">Eliminar pregunta</button>') +
    `<section class="panel"><div class="row"><span class="pill">${labels[question.tipo]}</span><span class="pill">${labels[question.nivel]}</span></div><h2>${esc(question.enunciado)}</h2>${locked ? '<p class="notice info">Esta pregunta está protegida porque es institucional o pertenece a un quiz listo.</p>' : ''}<div class="section-heading"><h3>Alternativas y retroalimentación</h3>${!locked ? '<button class="button dark" data-action="add-option">+ Añadir alternativa</button>' : ''}</div>${alternatives.map(a => {
      const f = feedback.find(f => f.alternativaId === a.id);
      return `<article class="alternative-editor"><div class="row"><strong>${a.ordenVisual}. ${a.correcta ? '✓ Respuesta correcta' : 'Alternativa incorrecta'}</strong>${!locked ? `<div><button class="text-button" data-action="edit-option" data-id="${a.id}">Editar</button><button class="text-button danger" data-action="delete-option" data-id="${a.id}">Eliminar</button></div>` : ''}</div>${a.codigo ? `<pre><code>${esc(a.codigo)}</code></pre>` : `<p>${esc(a.texto)}</p>`}${!a.correcta ? `<div class="feedback-summary"><strong>${f ? '✓ Explicación completa' : '⚠ Falta retroalimentación'}</strong>${f ? `<dl><dt>Causa</dt><dd>${esc(f.causa)}</dd><dt>Explicación</dt><dd>${esc(f.explicacion)}</dd><dt>Paso a paso</dt><dd>${esc(f.secuenciaTextual)}</dd><dt>Sugerencia</dt><dd>${esc(f.sugerencia)}</dd></dl>` : '<p>Completa la causa, explicación, secuencia y sugerencia para poder marcar el quiz listo.</p>'}${!locked ? `<button class="button secondary" data-action="feedback-option" data-id="${a.id}">${f ? 'Editar explicación' : 'Añadir explicación'}</button>` : ''}</div>` : ''}</article>`;
    }).join('') || ctx.empty('Añade tus alternativas', 'Incluye una correcta y explica cada opción incorrecta.')}</section>`);
  ctx.on('edit-question', () => questionForm(ctx, question));
  ctx.on('delete-question', () => confirmAction(ctx, 'Eliminar pregunta', 'La pregunta se retirará de tu banco si no está protegida por un quiz listo.', async () => { await ctx.api(base, { method: 'DELETE' }); ctx.go('/docente/preguntas'); }));
  function optionForm(option) {
    ctx.modal(option ? 'Editar alternativa' : 'Añadir alternativa', `<form class="stack-form"><label>Texto de la respuesta<textarea name="texto" maxlength="1000" rows="2">${esc(option?.texto)}</textarea></label><label>Código (opcional)<textarea class="code-input" name="codigo" maxlength="4000" rows="3">${esc(option?.codigo)}</textarea></label><div class="form-grid"><label>Lenguaje<input name="lenguaje" maxlength="32" value="${esc(option?.lenguaje)}" placeholder="python, sql…"></label><label>Orden visual<input type="number" min="1" name="ordenVisual" value="${option?.ordenVisual || Math.max(0,...alternatives.map(a => a.ordenVisual)) + 1}" required></label></div><label class="checkbox-label"><input type="checkbox" name="correcta" ${option?.correcta ? 'checked' : ''}> Esta es la respuesta correcta</label>${errors}<button class="button dark">Guardar alternativa</button></form>`, async f => {
      if (!String(f.get('texto')).trim() && !String(f.get('codigo')).trim()) throw new Error('Escribe un texto o fragmento de código.');
      await ctx.api(`${base}/alternativas${option ? `/${option.id}` : ''}`, { method: option ? 'PUT' : 'POST', body: { texto: f.get('texto') || null, codigo: f.get('codigo') || null, lenguaje: f.get('lenguaje') || null, ordenVisual: Number(f.get('ordenVisual')), correcta: f.has('correcta') } }); refresh();
    });
  }
  ctx.on('add-option', () => optionForm());
  ctx.on('edit-option', b => optionForm(alternatives.find(a => a.id === Number(b.dataset.id))));
  ctx.on('delete-option', b => confirmAction(ctx, 'Eliminar alternativa', 'Esta alternativa se eliminará de la pregunta.', async () => { await ctx.api(`${base}/alternativas/${Number(b.dataset.id)}`, { method: 'DELETE' }); refresh(); }));
  ctx.on('feedback-option', b => {
    const optionId = Number(b.dataset.id), f = feedback.find(f => f.alternativaId === optionId);
    ctx.modal('Convierte el error en aprendizaje', `<form class="stack-form">${[['causa','¿Qué causó el error?',500],['explicacion','Explicación conceptual',1500],['secuenciaTextual','Secuencia paso a paso',1500],['sugerencia','Sugerencia para reintentar',500]].map(([key,title,max]) => `<label>${title}<textarea name="${key}" maxlength="${max}" rows="3" required>${esc(f?.[key])}</textarea></label>`).join('')}${errors}<button class="button dark">Guardar explicación</button></form>`, async data => {
      await ctx.api(`${base}/retroalimentaciones${f ? `/${f.id}` : ''}`, { method: f ? 'PUT' : 'POST', body: { alternativaId: optionId, ...Object.fromEntries(data) } }); refresh();
    });
  });
}
function quizForm(ctx, questions, quiz, refresh) {
  const locked = quiz?.estado === 'LISTO';
  const sorted = [...questions].sort((a,b) => (quiz?.items.find(i => i.preguntaId === a.id)?.orden || 9999) - (quiz?.items.find(i => i.preguntaId === b.id)?.orden || 9999));
  ctx.modal(locked ? 'Tu quiz está listo' : quiz ? 'Editar quiz' : 'Un quiz para tu clase', `<form class="stack-form"><label>Título<input name="titulo" minlength="3" maxlength="120" value="${esc(quiz?.titulo)}" required ${locked ? 'disabled' : ''}></label><p>Selecciona tus preguntas y define un orden único para cada una.</p><fieldset ${locked ? 'disabled' : ''}><legend>Preguntas del quiz</legend><div class="quiz-question-list">${sorted.map((q,index) => {
    const item = quiz?.items.find(i => i.preguntaId === q.id);
    return `<div class="quiz-question"><label><input type="checkbox" name="pregunta" value="${q.id}" ${item ? 'checked' : ''}><span>${esc(q.enunciado)}</span></label><label class="quiz-order">Orden<input type="number" name="order-${q.id}" min="1" value="${item?.orden || index + 1}" aria-label="Orden de pregunta ${q.id}"></label></div>`;
  }).join('') || '<p>Aún no tienes preguntas. Crea una en tu banco de preguntas.</p>'}</div></fieldset>${locked ? '<p class="notice info">Los quizzes listos están protegidos contra cambios.</p>' : `${errors}<button class="button dark">Guardar borrador</button>`}</form>`, locked ? null : async f => {
    const items = f.getAll('pregunta').map(id => ({ preguntaId: Number(id), orden: Number(f.get(`order-${id}`)) })).sort((a,b) => a.orden - b.orden);
    if (items.some(i => !Number.isInteger(i.orden) || i.orden < 1)) throw new Error('Cada pregunta seleccionada necesita un orden positivo.');
    if (new Set(items.map(i => i.orden)).size !== items.length) throw new Error('El orden de las preguntas no puede repetirse.');
    await ctx.api(ctx.teacherPath(`quizzes${quiz ? `/${quiz.id}` : ''}`), { method: quiz ? 'PUT' : 'POST', body: { titulo: f.get('titulo'), items } }); refresh();
  });
}
async function roomDetail(ctx, id, refresh) {
  const base = ctx.teacherPath(`salas/${id}`);
  const [sala, participants, ranking, anulaciones] = await Promise.all([ctx.api(base), ctx.api(`/salas/${id}/participaciones`), ctx.api(`/salas/${id}/ranking`), ctx.api(`${base}/anulaciones`)]);
  const quiz = await ctx.api(ctx.teacherPath(`quizzes/${sala.quizId}`));
  if (!ctx.isCurrent()) return;
  const transitions = { BORRADOR: [['abrir-espera','Abrir sala de espera'],['iniciar','Iniciar sala']], SALA_ESPERA: [['iniciar','Iniciar sala']], EN_CURSO: [['pausar','Pausar sala'],['finalizar','Finalizar sala']], PAUSADA: [['reanudar','Reanudar sala'],['finalizar','Finalizar sala']], FINALIZADA: [] };
  ctx.view(`<a href="#/docente/salas" class="back-link">← Volver a mis salas</a>` + header(esc(quiz.titulo), 'Comparte el PIN y acompaña a tu clase.', '<button class="button secondary" data-action="refresh-room">↻ Actualizar sala</button>') +
    `<section class="panel room-control"><div><span class="eyebrow">PIN DE LA SALA</span><strong class="large-pin">${esc(sala.pin)}</strong><span class="pill">${labels[sala.estado]}</span></div><div class="card-actions">${(transitions[sala.estado] || []).map(([action,title]) => `<button class="button dark" data-action="transition" data-transition="${action}">${title}</button>`).join('')}</div><p>${sala.tiempoPorPregunta} segundos por pregunta · ${participants.filter(p => !p.expulsado).length} participantes activos</p></section>
    <div class="teacher-room-grid"><section class="panel"><h2>Participantes</h2>${participants.map(p => `<div class="participant-row"><span class="avatar">${p.estudianteId}</span><div><strong>${esc(ranking.find(r => r.participacionId === p.id)?.nombre || `Estudiante ${p.estudianteId}`)}</strong><small>${p.expulsado ? 'Participación suspendida' : 'En la sala'}</small></div>${['BORRADOR','SALA_ESPERA'].includes(sala.estado) ? `<button class="text-button" data-action="moderate" data-id="${p.id}" data-mode="${p.expulsado ? 'readmitir' : 'expulsar'}">${p.expulsado ? 'Readmitir' : 'Expulsar'}</button>` : ''}</div>`).join('') || ctx.empty('Aquí se reúne tu clase', 'Abre la espera y comparte el PIN.')}</section><section class="panel"><h2>Clasificación</h2>${ranking.length ? `<div class="table-scroll"><table><thead><tr><th>#</th><th>Estudiante</th><th>Puntos</th><th>Tiempo</th></tr></thead><tbody>${ranking.map(r => `<tr><td>${r.posicion}</td><td>${esc(r.nombre)} ${esc(r.apellidoInicial)}</td><td>${r.puntos}</td><td>${formatTime(r.tiempoMs)}</td></tr>`).join('')}</tbody></table></div>` : ctx.empty('Los resultados llegarán aquí', 'Los aciertos suman y el tiempo solo desempata.')}</section></div>
    <section class="panel"><div class="section-heading"><h2>Preguntas de la sesión</h2><span>${quiz.items.length} preguntas</span></div>${quiz.items.map(item => {
      const annulled = anulaciones.find(a => a.preguntaId === item.preguntaId);
      return `<div class="session-question"><span class="block-index">${item.orden}</span><div><strong>${esc(item.enunciado)}</strong>${annulled ? `<p>Anulada: ${esc(annulled.motivo)}</p>` : ''}</div>${sala.estado !== 'FINALIZADA' && !annulled ? `<button class="text-button danger" data-action="annul" data-id="${item.preguntaId}">Anular</button>` : ''}</div>`;
    }).join('')}</section>`);
  ctx.on('refresh-room', refresh);
  ctx.on('transition', b => {
    const action = b.dataset.transition;
    const change = async () => { await ctx.api(`${base}/${action}`, { method: 'POST' }); refresh(); };
    return action === 'finalizar' ? confirmAction(ctx, 'Finalizar la sala', 'La sesión pasará a finalizada y no se podrá reanudar.', change) : change();
  });
  ctx.on('moderate', async b => { await ctx.api(`/salas/${id}/participaciones/${Number(b.dataset.id)}/${b.dataset.mode}?docenteId=${ctx.session.usuarioId}`, { method: 'POST' }); refresh(); });
  ctx.on('annul', b => ctx.modal('Anular una pregunta', `<form class="stack-form"><p>La pregunta se excluirá de los puntos y de la clasificación de esta sala.</p><label>Motivo<textarea name="motivo" maxlength="500" required rows="3"></textarea></label>${errors}<button class="button dark">Anular pregunta</button></form>`, async f => {
    await ctx.api(`${base}/anulaciones`, { method: 'POST', body: { preguntaId: Number(b.dataset.id), motivo: f.get('motivo') } }); refresh();
  }));
}
