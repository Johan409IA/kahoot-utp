import assert from 'node:assert/strict';
import { randomBytes } from 'node:crypto';
import { mkdir, writeFile } from 'node:fs/promises';

const origin = new URL(process.env.ADAPTCODE_TEST_ORIGIN || 'http://127.0.0.1:8080');
if (!['127.0.0.1','localhost','[::1]'].includes(origin.hostname)) throw new Error('La verificación escribe datos de prueba y solo admite una API local.');
const checks = [];
async function request(path, { method='GET', body, token, expected=200 } = {}) {
  const response = await fetch(new URL(path,origin), {method,headers:{Accept:path.startsWith('/api/') ? 'application/json' : '*/*',...(body ? {'Content-Type':'application/json'} : {}),...(token ? {Authorization:`Bearer ${token}`} : {})},...(body ? {body:JSON.stringify(body)} : {}),signal:AbortSignal.timeout(15000)});
  const text = await response.text();
  let data; try { data = JSON.parse(text); } catch { data = text; }
  assert.equal(response.status,expected,`${method} ${path}: ${response.status}, ${response.status >= 400 ? JSON.stringify(data) : 'estado inesperado'}`);
  return data;
}
const post = (path,body,token,expected=201) => request(path,{method:'POST',body,token,expected});
const check = (name) => { checks.push(name); console.log(`OK: ${name}`); };
async function account(correo,nombres) {
  const contrasena = randomBytes(24).toString('base64url');
  const registered = await post('/api/auth/registro',{correo,nombres,apellidos:'Verificación local',contrasena});
  await post('/api/auth/verificar-cuenta',{correo,codigo:registered.codigoVerificacion},null,200);
  return post('/api/auth/login',{correo,contrasena},null,200);
}
const runId = Date.now(), suffix = String(runId % 100000000).padStart(8,'0');
const home = await request('/'); assert.match(home, /AdaptCode/);
const css = await request('/assets/styles.css'); assert.match(css, /font-size:16px/);
await request('/api/usuarios/1/progreso',{expected:401});
check('Frontend y recursos servidos por Spring Boot; rutas protegidas sin sesión devuelven 401');
const courses = await request('/api/cursos'); assert.equal(courses.length,3);
const groups = await Promise.all(courses.map(async c => ({course:c,temas:await request(`/api/cursos/${c.id}/temas`),retos:await request(`/api/micro-retos?cursoId=${c.id}`)})));
const retos = groups.flatMap(g => g.retos); assert.equal(new Set(retos.map(r => r.tipo)).size,4);
const details = await Promise.all(retos.map(r => request(`/api/micro-retos/${r.id}`)));
assert.ok(details.every(r => !JSON.stringify(r.contenido).includes('correcta')));
check('Catálogo real de PostgreSQL: 3 cursos y los 4 tipos de micro-reto, sin respuestas correctas expuestas');
const student = await account(`u${suffix}@utp.edu.pe`,'Estudiante API');
const teacher = await account(`c${suffix.slice(-5)}@utp.edu.pe`,'Docente API');
const external = await account(`externo-${runId}@adaptcode.test`,'Autodidacta API');
assert.equal(student.rol,'ESTUDIANTE_UTP'); assert.equal(teacher.rol,'DOCENTE_UTP'); assert.equal(external.rol,'USUARIO_EXTERNO');
check('Registro, verificación e inicio de sesión con JWT de los tres roles');
const studentPath = `/api/usuarios/${student.usuarioId}`;
const multiple = details.find(r => r.tipo === 'SELECCION_MULTIPLE');
const incorrect = multiple.contenido.opciones.find(o => o.texto === 'Falso');
for (let i=1;i<=3;i++) {
  const result = await post(`${studentPath}/intentos/evaluar`,{microRetoId:multiple.id,alternativaId:incorrect.id,usoAyuda:false,tiempoMs:2000},student.token);
  assert.equal(result.acierto,false); assert.equal(result.verSolucionDisponible,i === 3); assert.equal(Boolean(result.solucion),i === 3);
}
await post(`${studentPath}/intentos`,{microRetoId:multiple.id,acierto:true,usoAyuda:true,tiempoMs:500},student.token);
for (const reto of details.filter(r => r.id !== multiple.id)) {
  let answer;
  if (reto.tipo === 'ORDENAR_BLOQUES') {
    const blocks = reto.contenido.bloques;
    answer = {ordenBloques:['Leer n','Evaluar si n > 0','Mostrar positivo'].map(text => blocks.find(b => b.texto === text).id)};
  } else {
    const option = reto.contenido.opciones.find(o => reto.tipo === 'PREDECIR_SALIDA' ? o.texto === '8' : o.codigo === 'SELECT * FROM estudiantes;');
    answer = {alternativaId:option.id};
  }
  const result = await post(`${studentPath}/intentos/evaluar`,{microRetoId:reto.id,...answer,usoAyuda:false,tiempoMs:3000},student.token);
  assert.equal(result.acierto,true);
}
const progress = await request(`${studentPath}/progreso`,{token:student.token});
assert.equal(progress.filter(p => p.completado).length,4); assert.equal(progress.filter(p => p.puntoDominio).length,3);
assert.equal(progress.find(p => p.microRetoId === multiple.id).resueltoConAyuda,true);
const history = await request(`${studentPath}/intentos?microRetoId=${multiple.id}`,{token:student.token}); assert.equal(history.length,4);
const badges = await request(`${studentPath}/insignias`,{token:student.token}); assert.ok(badges.some(b => b.tipo === 'PRIMER_PASO'));
check('Evaluación y persistencia: errores, solución desde el tercer intento, 4 completados, 3 puntos e historial');
const preferences = {tema:'NORMAL',tamanoTexto:'NORMAL',reduccionMovimiento:true,sonidoActivado:false,modoDaltonismo:false};
await request(`${studentPath}/preferencias`,{method:'PUT',body:preferences,token:student.token});
assert.equal((await request(`${studentPath}/preferencias`,{token:student.token})).reduccionMovimiento,true);
check('Preferencias de accesibilidad guardadas y recuperadas desde la base');
const teacherPath = `/api/docentes/${teacher.usuarioId}`;
const question = await post(`${teacherPath}/preguntas`,{temaId:multiple.temaId,tipo:'SELECCION_MULTIPLE',nivel:'BASICO',enunciado:`Verificación ${runId}: ¿5 > 3?`},teacher.token);
await post(`${teacherPath}/preguntas/${question.id}/alternativas`,{texto:'Verdadero',ordenVisual:1,correcta:true},teacher.token);
const wrong = await post(`${teacherPath}/preguntas/${question.id}/alternativas`,{texto:'Falso',ordenVisual:2,correcta:false},teacher.token);
const quiz = await post(`${teacherPath}/quizzes`,{titulo:`Verificación local ${runId}`,items:[{preguntaId:question.id,orden:1}]},teacher.token);
await post(`${teacherPath}/quizzes/${quiz.id}/listo`,undefined,teacher.token,400);
await post(`${teacherPath}/preguntas/${question.id}/retroalimentaciones`,{alternativaId:wrong.id,causa:'Se invirtió la comparación.',explicacion:'5 es mayor que 3.',secuenciaTextual:'Identifica 5; compara con 3; evalúa el operador >.',sugerencia:'Compara los valores antes de decidir.'},teacher.token);
assert.equal((await post(`${teacherPath}/quizzes/${quiz.id}/listo`,undefined,teacher.token,200)).estado,'LISTO');
check('Gestión docente real: pregunta, alternativas, retroalimentación y validación obligatoria de quiz listo');
const room = await post(`${teacherPath}/salas`,{quizId:quiz.id,tiempoPorPregunta:60,ampliacion:'ESTANDAR'},teacher.token);
await post(`${teacherPath}/salas/${room.id}/abrir-espera`,undefined,teacher.token,200);
assert.equal((await request(`/api/salas/por-pin?pin=${room.pin}`,{token:student.token})).id,room.id);
const participation = await post(`/api/salas/${room.id}/participaciones`,{estudianteId:student.usuarioId},student.token);
await post(`/api/salas/${room.id}/participaciones`,{estudianteId:student.usuarioId},student.token,400);
await request(`/api/salas/por-pin?pin=${room.pin}`,{token:external.token,expected:403});
for (const action of ['iniciar','pausar','reanudar']) await post(`${teacherPath}/salas/${room.id}/${action}`,undefined,teacher.token,200);
await post(`/api/salas/${room.id}/respuestas`,{participacionId:participation.id,preguntaId:question.id,acierto:true,omitida:false,tiempoMs:4000},student.token);
const ranking = await request(`/api/salas/${room.id}/ranking`,{token:student.token}); assert.equal(ranking[0].puntos,1);
await post(`${teacherPath}/salas/${room.id}/finalizar`,undefined,teacher.token,200);
check('Sala real: PIN, ingreso único, exclusión de externos, estados y ranking persistido');
const report = {verifiedAt:new Date().toISOString(),origin:origin.origin,checks,courses:courses.length,retos:retos.length,usuarios:{student:student.usuarioId,teacher:teacher.usuarioId,external:external.usuarioId},roomId:room.id,progress:{completados:4,puntos:3}};
await mkdir(new URL('../.local/',import.meta.url),{recursive:true});
await writeFile(new URL('../.local/live-verification.json',import.meta.url),JSON.stringify(report,null,2));
console.log(`Verificación integrada completa: ${checks.length} grupos de comprobaciones. No se han guardado tokens ni contraseñas.`);
