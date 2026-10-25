import { ApiError } from './api.js';
import { readStored } from './core.js';

// This adapter is loaded only with ?demo=1. Production requests never fall back to it.
export function createDemo(storage = localStorage) {
  const initial = () => ({
    courses: [{id:1,nombre:'Algoritmos'},{id:2,nombre:'Python'},{id:3,nombre:'Bases de Datos'}],
    temas: [{id:1,cursoId:1,nombre:'Condicionales'},{id:2,cursoId:1,nombre:'Secuencias'},{id:3,cursoId:2,nombre:'Expresiones'},{id:4,cursoId:3,nombre:'Consultas SELECT'}],
    retos: [
      { id:1,temaId:1,tipo:'SELECCION_MULTIPLE',nivel:'BASICO',enunciado:'Con edad = 18, ¿la condición edad >= 18 es verdadera?',contenido:{opciones:[{id:1,texto:'Verdadero'},{id:2,texto:'Falso'}]} },
      { id:2,temaId:2,tipo:'ORDENAR_BLOQUES',nivel:'BASICO',enunciado:'Ordena los pasos para mostrar si un número es positivo.',contenido:{bloques:[{id:1,texto:'Mostrar positivo'},{id:2,texto:'Leer n'},{id:3,texto:'Evaluar si n > 0'}]} },
      { id:3,temaId:3,tipo:'PREDECIR_SALIDA',nivel:'BASICO',enunciado:'¿Qué valor imprime este fragmento de Python?',contenido:{codigo:{lenguaje:'python',codigo:'print(2 + 3 * 2)'},opciones:[{id:3,texto:'8'},{id:4,texto:'10'},{id:5,texto:'7'}]} },
      { id:4,temaId:4,tipo:'SELECCIONAR_FRAGMENTO',nivel:'BASICO',enunciado:'¿Qué consulta obtiene todas las filas de la tabla estudiantes?',contenido:{opciones:[{id:6,codigo:'SELECT * FROM estudiantes;',lenguaje:'sql'},{id:7,codigo:'SELECT estudiantes FROM *;',lenguaje:'sql'},{id:8,codigo:'DELETE FROM estudiantes;',lenguaje:'sql'}]} },
    ],
    questions:[{id:101,docenteId:2,origen:'PROPIA',temaId:4,tipo:'SELECCIONAR_FRAGMENTO',nivel:'BASICO',enunciado:'¿Qué consulta lista todas las filas de estudiantes?',activa:true}],
    alternatives:[{id:111,preguntaId:101,texto:null,codigo:'SELECT * FROM estudiantes;',lenguaje:'sql',ordenVisual:1,correcta:true},{id:112,preguntaId:101,texto:null,codigo:'SELECT estudiantes FROM *;',lenguaje:'sql',ordenVisual:2,correcta:false}],
    feedback:[{id:121,preguntaId:101,alternativaId:112,causa:'La tabla se colocó en la lista de columnas.',explicacion:'Las columnas van después de SELECT y la tabla después de FROM.',secuenciaTextual:'Indica las columnas; escribe FROM; indica la tabla.',sugerencia:'Distingue las columnas del origen de los datos.'}],
    quizzes:[{id:201,docenteId:2,titulo:'Consultas SQL · Aprender a consultar',estado:'LISTO',items:[{id:211,preguntaId:101,enunciado:'¿Qué consulta lista todas las filas de estudiantes?',orden:1}]}],
    rooms:[{id:301,docenteId:2,quizId:201,pin:'482913',estado:'SALA_ESPERA',tiempoPorPregunta:60,ampliacion:'ESTANDAR'}],
    participants:[], progress:[], badges:[], attempts:[], preferences:{}, annulments:[], accounts:[], nextId:1000,
  });
  let state = readStored(storage, 'adaptcode.demo.data.v1') || initial();
  const persist = () => storage.setItem('adaptcode.demo.data.v1', JSON.stringify(state));
  const fail = (message, status = 400) => { throw new ApiError(message, status); };
  const find = (list, id) => list.find(item => item.id === Number(id)) || fail('El recurso no existe en esta demostración.',404);
  const save = (list, body, id) => {
    if (id) { Object.assign(find(list,id), body); return find(list,id); }
    const item = { ...body, id: state.nextId++ }; list.push(item); return item;
  };
  function recordAttempt(userId, body) {
    const attempt = { ...body, id: state.nextId++,usuarioId:userId,nroIntento:state.attempts.filter(a => a.usuarioId === userId && a.microRetoId === body.microRetoId).length + 1 };
    state.attempts.push(attempt);
    if (body.acierto) {
      if (!state.progress.some(p => p.usuarioId === userId && p.microRetoId === body.microRetoId)) state.progress.push({ id:state.nextId++,usuarioId:userId,microRetoId:body.microRetoId,completado:true,resueltoConAyuda:body.usoAyuda,puntoDominio:!body.usoAyuda });
      const award = tipo => { if (!state.badges.some(b => b.usuarioId === userId && b.tipo === tipo)) state.badges.push({id:state.nextId++,usuarioId:userId,tipo}); };
      award('PRIMER_PASO'); if (attempt.nroIntento >= 3) award('PERSEVERANTE');
    }
    return attempt;
  }
  function handle(path, { method, body }) {
    const url = new URL(`https://demo.invalid${path}`), parts = url.pathname.split('/').filter(Boolean), query = url.searchParams;
    if (parts[0] === 'auth') {
      if (parts[1] === 'login') {
        const account = state.accounts.find(a => a.correo === body.correo);
        const teacher = /^c\d{5}@utp\.edu\.pe$/i.test(body.correo);
        return {token:'demo-session',tipoToken:'Bearer',expiraEnMs:3600000,usuarioId:teacher ? 2 : 1,correo:body.correo,nombres:account?.nombres || (teacher ? 'Docente Demo' : 'Estudiante Demo'),apellidos:account?.apellidos || 'AdaptCode',rol:teacher ? 'DOCENTE_UTP' : /^u\d{8}@utp\.edu\.pe$/i.test(body.correo) ? 'ESTUDIANTE_UTP' : 'USUARIO_EXTERNO',genero:account?.genero || 'MASCULINO',fechaNacimiento:account?.fechaNacimiento || '2000-01-01'};
      }
      if (parts[1] === 'registro') {
        if (state.accounts.some(a => a.correo === body.correo)) fail('El correo ya está registrado en la demo.');
        state.accounts.push({correo:body.correo,nombres:body.nombres,apellidos:body.apellidos,genero:body.genero,fechaNacimiento:body.fechaNacimiento});
        return {usuarioId:1,correo:body.correo,requiereVerificacion:true,codigoVerificacion:'123456'};
      }
      if (['reenviar-codigo','recuperar-contrasena'].includes(parts[1])) return {mensaje:'Si el correo está registrado, recibirás un código.',codigo:'123456'};
      if (body.codigo !== '123456') fail('El código de demostración es 123456.');
      return {mensaje:parts[1] === 'verificar-cuenta' ? 'Cuenta verificada. Ya puedes iniciar sesión.' : 'Contraseña actualizada.'};
    }
    if (parts[0] === 'cursos') return parts[2] === 'temas' ? state.temas.filter(t => t.cursoId === Number(parts[1])) : state.courses;
    if (parts[0] === 'micro-retos') {
      if (parts[1]) return find(state.retos,parts[1]);
      return state.retos.filter(r => state.temas.find(t => t.id === r.temaId).cursoId === Number(query.get('cursoId'))).map(({contenido,...r}) => r);
    }
    if (parts[0] === 'usuarios') {
      const userId = Number(parts[1]);
      if (parts[2] === 'perfil') {
        state.profile = state.profile || {};
        if (method === 'PUT') { state.profile[userId] = { usuarioId: userId, genero: body.genero, fechaNacimiento: body.fechaNacimiento }; return state.profile[userId]; }
        return state.profile[userId] || { usuarioId: userId, genero: null, fechaNacimiento: null };
      }
      if (parts[2] === 'preferencias') {
        if (method === 'PUT') state.preferences[userId] = body;
        return state.preferences[userId] || fail('No hay preferencias registradas.',404);
      }
      if (parts[2] === 'progreso') return state.progress.filter(p => p.usuarioId === userId);
      if (parts[2] === 'insignias') return state.badges.filter(b => b.usuarioId === userId);
      if (parts[2] === 'intentos') {
        if (method === 'GET') return state.attempts.filter(a => a.usuarioId === userId && a.microRetoId === Number(query.get('microRetoId')));
        if (parts[3] !== 'evaluar') return recordAttempt(userId,body);
        const reto = find(state.retos,body.microRetoId);
        const acierto = reto.tipo === 'ORDENAR_BLOQUES' ? JSON.stringify(body.ordenBloques) === '[2,3,1]' : body.alternativaId === ({1:1,3:3,4:6})[reto.id];
        const registered = recordAttempt(userId,{...body,acierto});
        const feedback = {
          1:{causa:'Revisa el caso límite de la comparación.',explicacion:'El operador >= también incluye valores iguales.',secuenciaTextual:'Identifica el valor de edad; compáralo con el límite; revisa el operador.',sugerencia:'¿La igualdad está incluida?'},
          3:{causa:'La precedencia de operadores cambia el resultado.',explicacion:'Python evalúa la multiplicación antes que la suma.',secuenciaTextual:'Primero calcula la multiplicación. Después realiza la suma.',sugerencia:'Revisa qué operación debe ejecutarse primero.'},
          4:{causa:'La estructura de la consulta no corresponde a una lectura válida.',explicacion:'SELECT indica columnas y FROM indica la tabla. DELETE elimina datos.',secuenciaTextual:'Identifica la operación; define columnas; indica el origen.',sugerencia:'Busca la instrucción que consulta sin modificar.'},
        };
        return { intentoId:registered.id,nroIntento:registered.nroIntento,acierto,retroalimentacion:acierto ? null : feedback[reto.id] || null,verSolucionDisponible:registered.nroIntento >= 3,solucion:registered.nroIntento >= 3 ? {respuestaCorrecta:({1:'Verdadero',2:'Leer n → Evaluar si n > 0 → Mostrar positivo',3:'8',4:'SELECT * FROM estudiantes;'})[reto.id],detalle:'Compara cada paso con tu respuesta. Este reto se completa con ayuda y no genera punto de dominio.'} : null };
      }
    }
    if (parts[0] === 'docentes') {
      const id = Number(parts[3]), resource = parts[2];
      if (resource === 'preguntas') {
        if (parts[4] === 'clonar') {
          const reto = find(state.retos,id);
          return save(state.questions,{docenteId:2,origen:'COPIA',microRetoOrigenId:id,temaId:reto.temaId,tipo:reto.tipo,nivel:reto.nivel,enunciado:reto.enunciado,activa:true});
        }
        if (parts[4]) {
          const list = parts[4] === 'alternativas' ? state.alternatives : state.feedback, subId = Number(parts[5]);
          if (method === 'DELETE') { const index = list.findIndex(a => a.id === subId); list.splice(index,1); return 'Eliminado.'; }
          if (method === 'POST' || method === 'PUT') return save(list,{...body,preguntaId:id},subId);
          return list.filter(a => a.preguntaId === id);
        }
        if (method === 'DELETE') { find(state.questions,id).activa = false; return 'Pregunta eliminada.'; }
        if (method === 'POST' || method === 'PUT') return save(state.questions,{...body,docenteId:2,origen:id ? find(state.questions,id).origen : 'PROPIA',activa:true},id);
        return id ? find(state.questions,id) : state.questions.filter(q => q.activa);
      }
      if (resource === 'quizzes') {
        if (parts[4] === 'listo') {
          const quiz = find(state.quizzes,id); if (!quiz.items.length) fail('El quiz necesita al menos una pregunta.');
          for (const item of quiz.items) {
            const alts = state.alternatives.filter(a => a.preguntaId === item.preguntaId);
            if (!alts.some(a => a.correcta)) fail(`La pregunta ${item.preguntaId} necesita una respuesta correcta.`);
            if (alts.some(a => !a.correcta && !state.feedback.some(f => f.alternativaId === a.id))) fail(`La pregunta ${item.preguntaId} no tiene retroalimentación completa (Q19).`);
          }
          quiz.estado = 'LISTO'; return quiz;
        }
        if (method === 'DELETE') { state.quizzes = state.quizzes.filter(q => q.id !== id); return 'Quiz eliminado.'; }
        if (method === 'POST' || method === 'PUT') return save(state.quizzes,{...body,docenteId:2,estado:'BORRADOR',items:body.items.map(i => ({...i,id:state.nextId++,enunciado:find(state.questions,i.preguntaId).enunciado}))},id);
        return id ? find(state.quizzes,id) : state.quizzes;
      }
      if (resource === 'salas') {
        if (parts[4] === 'anulaciones') {
          if (method === 'POST') return save(state.annulments,{...body,salaId:id,docenteId:2});
          return state.annulments.filter(a => a.salaId === id);
        }
        if (parts[4]) { const sala = find(state.rooms,id); sala.estado = ({'abrir-espera':'SALA_ESPERA',iniciar:'EN_CURSO',pausar:'PAUSADA',reanudar:'EN_CURSO',finalizar:'FINALIZADA'})[parts[4]]; return sala; }
        if (method === 'POST') return save(state.rooms,{...body,docenteId:2,pin:String(100000 + state.nextId),estado:'BORRADOR'});
        return id ? find(state.rooms,id) : state.rooms;
      }
    }
    if (parts[0] === 'salas') {
      if (parts[1] === 'por-pin') return state.rooms.find(s => s.pin === query.get('pin')) || fail('No encontramos una sala con ese PIN. Prueba 482913 en la demo.',404);
      const salaId = Number(parts[1]);
      if (parts[2] === 'ranking') return state.participants.filter(p => p.salaId === salaId).map((p,i) => ({posicion:i + 1,participacionId:p.id,estudianteId:p.estudianteId,nombre:'Estudiante Demo',apellidoInicial:'A.',puntos:0,tiempoMs:0}));
      if (parts[2] === 'participaciones') {
        if (parts[4]) { const p = find(state.participants,parts[3]); p.expulsado = parts[4] === 'expulsar'; return p; }
        if (method === 'POST') {
          if (find(state.rooms,salaId).estado !== 'SALA_ESPERA') fail('Solo se puede ingresar en la sala de espera.');
          if (state.participants.some(p => p.salaId === salaId && p.estudianteId === body.estudianteId)) fail('Ya tienes una participación en esta sala.');
          return save(state.participants,{salaId,estudianteId:body.estudianteId,expulsado:false});
        }
        return state.participants.filter(p => p.salaId === salaId);
      }
    }
    fail('Esta operación no forma parte de la demostración.',404);
  }
  return async (path, options) => {
    const result = handle(path,options); persist(); return structuredClone(result);
  };
}
