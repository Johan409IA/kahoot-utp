import test from 'node:test';
import assert from 'node:assert/strict';
import { createApi } from '../src/main/resources/static/assets/api.js';
import { esc, percent, evaluationPayload, validSession, readStored, parseRoute } from '../src/main/resources/static/assets/core.js';

test('la evaluación transmite la selección al servidor con JWT y sin calificar en el cliente', async () => {
  let request;
  const api = createApi({getSession:() => ({token:'a-test-token'}),fetcher:async (url, options) => { request = {url,...options}; return new Response(JSON.stringify({acierto:false}),{status:201}); }});
  const payload = evaluationPayload({id:4,tipo:'SELECCIONAR_FRAGMENTO'},7,Date.now() - 100);
  const response = await api('/usuarios/1/intentos/evaluar',{method:'POST',body:payload});
  assert.equal(request.url,'/api/usuarios/1/intentos/evaluar');
  assert.equal(request.headers.Authorization,'Bearer a-test-token');
  assert.equal(JSON.parse(request.body).alternativaId,7);
  assert.equal('acierto' in JSON.parse(request.body),false);
  assert.equal(response.acierto,false);
});
test('ordenamiento transmite todos los bloques en el orden elegido', () => {
  const payload = evaluationPayload({id:2,tipo:'ORDENAR_BLOQUES'},[3,1,2],Date.now());
  assert.equal(payload.alternativaId,null); assert.deepEqual(payload.ordenBloques,[3,1,2]); assert.ok(payload.tiempoMs >= 0);
});
test('una sesión expirada se descarta', () => {
  assert.equal(validSession({token:'x',usuarioId:1,expiresAt:10},11),null);
  assert.equal(validSession({token:'x',usuarioId:1,expiresAt:20},11).usuarioId,1);
});
test('entrar directamente sin hash muestra el inicio y los filtros conservan sus parámetros', () => {
  assert.equal(parseRoute('').path,'/');
  assert.equal(parseRoute('#/').path,'/');
  const route = parseRoute('#/retos?curso=1&nivel=BASICO');
  assert.equal(route.path,'/retos'); assert.equal(route.params.get('curso'),'1');
  assert.equal(route.params.get('nivel'),'BASICO');
});
test('401 limpia la sesión y conserva el error de la API', async () => {
  let called = false;
  const api = createApi({getSession:() => ({token:'expired'}),onUnauthorized:() => called=true,fetcher:async () => new Response('{"error":"Sesión expirada"}',{status:401})});
  await assert.rejects(api('/usuarios/1/progreso'),/Sesión expirada/); assert.equal(called,true);
});
test('validaciones por campo y caídas de red producen mensajes útiles', async () => {
  const invalid = createApi({fetcher:async () => new Response('{"correo":"Correo inválido","contrasena":"Contraseña corta"}',{status:400})});
  await assert.rejects(invalid('/auth/registro'),/Correo inválido Contraseña corta/);
  const offline = createApi({fetcher:async () => { throw new TypeError('network'); }});
  await assert.rejects(offline('/cursos'),e => e.status === 0 && e.message.includes('conectar'));
});
test('respuestas de texto y listas vacías se conservan', async () => {
  const api = createApi({fetcher:async () => new Response('Pregunta eliminada.')});
  assert.equal(await api('/docentes/2/preguntas/1',{method:'DELETE'}),'Pregunta eliminada.');
  assert.equal(percent(0,0),0); assert.equal(percent(1,4),25);
});
test('contenido externo se escapa antes de insertarse en HTML', () => {
  assert.equal(esc('<script>"&\'</script>'),'&lt;script&gt;&quot;&amp;&#39;&lt;/script&gt;');
  assert.equal(readStored({getItem:() => '{broken'},'key','fallback'),'fallback');
});
