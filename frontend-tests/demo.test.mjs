import test from 'node:test';
import assert from 'node:assert/strict';
import { createDemo } from '../src/main/resources/static/assets/demo.js';
const demo = () => { const map = new Map(); return createDemo({getItem:key=>map.get(key),setItem:(key,value)=>map.set(key,value)}); };
const get = (api,path) => api(path,{method:'GET'});
const post = (api,path,body) => api(path,{method:'POST',body});

test('demo: tres errores habilitan solución; el avance asistido no da punto', async () => {
  const api = demo();
  for (let attempt=1;attempt<=3;attempt++) {
    const result = await post(api,'/usuarios/1/intentos/evaluar',{microRetoId:1,alternativaId:2,usoAyuda:false,tiempoMs:100});
    assert.equal(result.acierto,false); assert.equal(result.verSolucionDisponible,attempt === 3);
    assert.equal(Boolean(result.solucion),attempt === 3);
  }
  assert.deepEqual(await get(api,'/usuarios/1/progreso'),[]);
  await post(api,'/usuarios/1/intentos',{microRetoId:1,acierto:true,usoAyuda:true,tiempoMs:100});
  const [progress] = await get(api,'/usuarios/1/progreso');
  assert.equal(progress.resueltoConAyuda,true); assert.equal(progress.puntoDominio,false);
});
test('demo: repetir aciertos conserva un único punto y distingue los usuarios', async () => {
  const api = demo();
  for (let i=0;i<2;i++) await post(api,'/usuarios/1/intentos/evaluar',{microRetoId:1,alternativaId:1,usoAyuda:false});
  assert.equal((await get(api,'/usuarios/1/progreso')).length,1);
  assert.equal((await get(api,'/usuarios/2/progreso')).length,0);
  assert.equal((await get(api,'/usuarios/1/insignias')).length,1);
});
test('demo: una pregunta sin alternativas correctas impide marcar un quiz listo', async () => {
  const api = demo();
  const q = await post(api,'/docentes/2/preguntas',{temaId:1,tipo:'SELECCION_MULTIPLE',nivel:'BASICO',enunciado:'Prueba'});
  const quiz = await post(api,'/docentes/2/quizzes',{titulo:'Quiz de prueba',items:[{preguntaId:q.id,orden:1}]});
  await assert.rejects(post(api,`/docentes/2/quizzes/${quiz.id}/listo`),/respuesta correcta/);
});
test('demo: el PIN encuentra la sala y cada estudiante ocupa un solo lugar', async () => {
  const api = demo(), sala = await get(api,'/salas/por-pin?pin=482913');
  const p = await post(api,`/salas/${sala.id}/participaciones`,{estudianteId:1});
  assert.equal(p.salaId,sala.id);
  await assert.rejects(post(api,`/salas/${sala.id}/participaciones`,{estudianteId:1}),/participación/);
});
