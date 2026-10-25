import { esc, labels, evaluationPayload, formatTime } from './core.js';
import { icon } from './icons.js';

export async function renderPractice(ctx, id) {
  const reto = await ctx.api(`/micro-retos/${id}`);
  if (!ctx.isCurrent()) return;
  let blocks = [...(reto.contenido.bloques || [])], result, usedHelp = false, started = Date.now();
  const course = ctx.courses.find(c => c.id === ctx.retos.find(r => r.id === id)?.cursoId);
  const tema = ctx.temas.find(t => t.id === reto.temaId);
  ctx.view(`<a class="back-link" href="#/retos">← Volver a los micro-retos</a><div class="page-heading"><div><div class="eyebrow">${esc(course?.nombre || 'PRÁCTICA AUTÓNOMA')} · ${esc(tema?.nombre)}</div><h1>Un intento, una nueva idea.</h1><p>Lee con calma. Aquí tienes espacio para comprender.</p></div><span class="pill">${labels[reto.nivel]}</span></div>
    <div class="practice-layout"><section class="panel practice-panel"><div class="row"><span class="tag">${icon('code')} ${labels[reto.tipo]}</span><span class="pill" id="attempt-count">Sin límite de intentos</span></div><h2>${esc(reto.enunciado)}</h2>
    ${reto.contenido.codigo ? `<div class="code-block"><div>${esc(reto.contenido.codigo.lenguaje || 'Código')}</div><pre><code>${esc(reto.contenido.codigo.codigo)}</code></pre></div>` : ''}
    <form id="answer-form"><fieldset><legend>${reto.tipo === 'ORDENAR_BLOQUES' ? 'Ordena los bloques con los botones de subir y bajar.' : 'Selecciona una respuesta.'}</legend><div id="answer-options">${reto.tipo === 'ORDENAR_BLOQUES' ? '' : (reto.contenido.opciones || []).map((op,i) => `<label class="answer-option"><input type="radio" name="answer" value="${op.id}" required><span class="option-letter">${String.fromCharCode(65 + i)}</span>${op.codigo ? `<pre><code>${esc(op.codigo)}</code></pre>` : `<span>${esc(op.texto)}</span>`}</label>`).join('')}</div></fieldset><div id="feedback" aria-live="polite"></div><div class="practice-actions"><button type="submit" class="button dark" id="evaluate">Comprobar respuesta ${icon('arrow')}</button><button type="button" class="text-button" data-action="history">${icon('clock')} Ver mis intentos</button></div></form></section>
    <aside><section class="panel practice-guide"><span class="stat-icon mint">${icon('bulb')}</span><h3>Comprender es la meta.</h3><p>Si te equivocas, revisa la explicación y vuelve a probar. Cada intento te acerca un poco más.</p><ul><li>Los intentos son ilimitados.</li><li>El tiempo no cambia tus puntos.</li><li>La solución asistida no otorga punto de dominio.</li></ul><label>Tu modo de práctica<select id="practice-mode"><option value="0">Guiado · Sin tiempo</option>${[20,30,60,90,120].map(t => `<option value="${t}">Desafío personal · ${t} s</option>`).join('')}</select></label><p id="practice-timer" class="timer" role="status">A tu propio ritmo</p></section>${!ctx.session ? '<div class="notice info"><div><strong>Guarda cada paso.</strong><p>Inicia sesión para enviar tu respuesta y conservar tus logros.</p><a class="text-link" href="#/acceso">Iniciar sesión →</a></div></div>' : ''}</aside></div>`);
  const renderBlocks = () => {
    if (!ctx.isCurrent()) return;
    document.querySelector('#answer-options').innerHTML = blocks.map((block,i) => `<div class="block-option"><span class="block-index">${i + 1}</span><code>${esc(block.texto)}</code><div><button type="button" class="icon-button" data-action="move-block" data-index="${i}" data-direction="-1" aria-label="Subir bloque ${i + 1}" ${i === 0 ? 'disabled' : ''}>↑</button><button type="button" class="icon-button" data-action="move-block" data-index="${i}" data-direction="1" aria-label="Bajar bloque ${i + 1}" ${i === blocks.length - 1 ? 'disabled' : ''}>↓</button></div></div>`).join('');
  };
  if (reto.tipo === 'ORDENAR_BLOQUES') renderBlocks();
  ctx.on('move-block', button => {
    const index = Number(button.dataset.index), next = index + Number(button.dataset.direction);
    [blocks[index],blocks[next]] = [blocks[next],blocks[index]]; renderBlocks();
    document.querySelector(`.block-option:nth-child(${next + 1}) button:not(:disabled)`)?.focus();
  });
  let timer;
  ctx.bind('#practice-mode', 'change', e => {
    clearInterval(timer); const seconds = Number(e.target.value), until = Date.now() + seconds * 1000;
    const output = document.querySelector('#practice-timer');
    if (!seconds) { output.textContent = 'A tu propio ritmo'; return; }
    const tick = () => {
      if (!ctx.isCurrent()) { clearInterval(timer); return; }
      const remaining = Math.max(0, Math.ceil((until - Date.now()) / 1000));
      output.textContent = remaining ? `${remaining} s para tu desafío personal` : 'Tiempo cumplido. Puedes seguir a tu ritmo.';
      if (!remaining) clearInterval(timer);
    };
    tick(); timer = setInterval(tick, 1000);
  });
  ctx.bind('#answer-form', 'submit', async e => {
    e.preventDefault(); if (!ctx.requireSession()) return;
    const answer = reto.tipo === 'ORDENAR_BLOQUES' ? blocks.map(b => b.id) : new FormData(e.target).get('answer');
    // The server owns grading; no correct alternatives are present in catalog data.
    result = await ctx.api(ctx.userPath('intentos/evaluar'), { method: 'POST', body: evaluationPayload(reto, answer, started, usedHelp) });
    if (!ctx.isCurrent()) return;
    document.querySelector('#attempt-count').textContent = `Intento ${result.nroIntento}`;
    document.querySelector('#feedback').innerHTML = `<div class="feedback ${result.acierto ? 'correct' : 'retry-feedback'}"><h3>${icon(result.acierto ? 'check' : 'bulb')} ${result.acierto ? '¡Lo comprendiste! Buen trabajo.' : 'Vamos a mirarlo de otra manera.'}</h3>${result.acierto ? '<p>Tu respuesta es correcta. Puedes volver a practicar o explorar otro reto.</p>' : result.retroalimentacion ? `<dl><dt>¿Qué pasó?</dt><dd>${esc(result.retroalimentacion.causa)}</dd><dt>La idea clave</dt><dd>${esc(result.retroalimentacion.explicacion)}</dd><dt>Paso a paso</dt><dd>${esc(result.retroalimentacion.secuenciaTextual)}</dd><dt>Para tu siguiente intento</dt><dd>${esc(result.retroalimentacion.sugerencia)}</dd></dl>` : '<p>Revisa el orden y la lógica de los pasos. Puedes volver a intentarlo.</p>'}${!result.acierto && result.verSolucionDisponible && result.solucion ? '<button type="button" class="button secondary" data-action="solution">Ver solución y completar con ayuda</button>' : ''}</div>`;
    document.querySelector('#evaluate').innerHTML = `${result.acierto ? 'Volver a comprobar' : 'Intentar de nuevo'} ${icon('arrow')}`;
    if (result.acierto && ctx.preferences.sonidoActivado) playSuccess();
    started = Date.now();
    await ctx.reloadPersonal();
  });
  ctx.on('solution', async () => {
    if (!result?.solucion || !ctx.requireSession()) return;
    await ctx.api(ctx.userPath('intentos'), { method: 'POST', body: { microRetoId: id, acierto: true, usoAyuda: true, tiempoMs: Math.max(0, Date.now() - started) } });
    usedHelp = true;
    if (ctx.isCurrent()) document.querySelector('#feedback').innerHTML = `<div class="feedback"><h3>${icon('bulb')} Solución explicada</h3><pre><code>${esc(result.solucion.respuestaCorrecta)}</code></pre><p>${esc(result.solucion.detalle)}</p><p><strong>Reto completado con ayuda.</strong> Este avance cuenta para tu progreso.</p></div>`;
    await ctx.reloadPersonal();
  });
  ctx.on('history', async () => {
    if (!ctx.requireSession()) return;
    const history = await ctx.api(ctx.userPath(`intentos?microRetoId=${id}`));
    if (!ctx.isCurrent()) return;
    ctx.modal('Cada intento es parte del camino', history.length ? `<div class="table-scroll"><table><thead><tr><th>Intento</th><th>Resultado</th><th>Ayuda</th><th>Tiempo</th></tr></thead><tbody>${history.map(h => `<tr><td>${h.nroIntento}</td><td>${h.acierto ? '✓ Correcto' : 'Por revisar'}</td><td>${h.usoAyuda ? 'Con solución' : 'Sin solución'}</td><td>${formatTime(h.tiempoMs)}</td></tr>`).join('')}</tbody></table></div>` : ctx.empty('Aquí empieza tu recorrido', 'Todavía no has enviado respuestas a este reto.'));
  });
}
function playSuccess() {
  try {
    const Audio = window.AudioContext || window.webkitAudioContext;
    if (!Audio) return;
    const audio = new Audio(), oscillator = audio.createOscillator(), gain = audio.createGain();
    oscillator.connect(gain); gain.connect(audio.destination); gain.gain.value = 0.035;
    oscillator.frequency.setValueAtTime(523.25, audio.currentTime); oscillator.frequency.setValueAtTime(659.25, audio.currentTime + 0.12);
    gain.gain.exponentialRampToValueAtTime(0.001, audio.currentTime + 0.3);
    oscillator.start(); oscillator.stop(audio.currentTime + 0.3); oscillator.onended = () => audio.close();
  } catch { /* Audio is optional; visual feedback always remains available. */ }
}
