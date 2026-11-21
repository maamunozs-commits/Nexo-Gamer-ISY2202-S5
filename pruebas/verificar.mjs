import assert from 'node:assert/strict';

const origen = 'http://localhost:8090';
const credenciales = [
  ['jugador@nexogamer.test', 'NexoDemo!27A', 'JUGADOR'],
  ['invitado@nexogamer.test', 'NexoDemo!27B', 'JUGADOR'],
  ['organizador@nexogamer.test', 'NexoDemo!27C', 'ORGANIZADOR'],
];
const resultados = [];
function comprobar(nombre, esperado, obtenido) {
  assert.deepEqual(obtenido, esperado, nombre);
  resultados.push(nombre);
}

async function solicitar(ruta, { metodo = 'GET', token, datos, formulario } = {}) {
  const headers = token ? { Authorization: 'Bearer ' + token } : {};
  if (datos) headers['Content-Type'] = 'application/json';
  return fetch(origen + ruta, {
    method: metodo, headers, body: formulario ?? (datos ? JSON.stringify(datos) : undefined),
    redirect: 'manual',
  });
}

comprobar('Portada pública', 200, (await solicitar('/eventos')).status);
comprobar('Búsqueda pública', 200, (await solicitar('/api/eventos')).status);
comprobar('Detalle web privado', 302, (await solicitar('/eventos/1')).status);
comprobar('API privada sin JWT', 401, (await solicitar('/api/eventos/1')).status);

const sesiones = [];
for (const [correo, clave, rol] of credenciales) {
  const acceso = await solicitar('/api/login', { metodo: 'POST', datos: { correo, clave } });
  comprobar('Inicio de sesión ' + rol, 200, acceso.status);
  const sesion = await acceso.json();
  comprobar('Permiso firmado ' + rol, rol, sesion.roles[0]);
  comprobar('Vigencia del JWT', 900, sesion.expiraEn);
  sesiones.push(sesion.token);
}

const jugador = sesiones[0];
const organizador = sesiones[2];
const nuevo = {
  nombre: 'Control de integración ' + Date.now(), videojuego: 'Rocket League', categoria: 'Encuentro',
  pais: 'Chile', ciudad: 'Santiago', fecha: '2027-02-05', hora: '15:00', modalidad: 'Presencial',
  resumen: 'Prueba de publicación y participación.', descripcion: 'Evento temporal del entorno de verificación.',
  ubicacion: 'Sala de reuniones', enlace: null, organizador: 'Nexo Gamer', requisitos: 'Registro',
  inscripcion: 'Recepción',
};
const crear = await solicitar('/api/eventos', { metodo: 'POST', token: organizador, datos: nuevo });
comprobar('Publicación de organizador', 201, crear.status);
const { id } = await crear.json();
const ruta = '/api/eventos/' + id;
comprobar('Detalle autenticado', 200, (await solicitar(ruta, { token: jugador })).status);
comprobar('Difusión denegada al jugador', 403,
  (await solicitar(ruta + '/compartir', { metodo: 'POST', token: jugador })).status);
comprobar('Difusión aceptada al autor', true,
  (await (await solicitar(ruta + '/compartir', { metodo: 'POST', token: organizador })).json()).compartido);

const foto = new FormData();
const { readFile } = await import('node:fs/promises');
const archivo = await readFile(new URL('../src/main/resources/static/img/arena.jpg', import.meta.url));
foto.set('titulo', 'Fotografía de verificación');
foto.set('archivo', new Blob([archivo], { type: 'image/jpeg' }), 'fotografia.jpg');
const subida = await solicitar(ruta + '/medios', { metodo: 'POST', token: organizador, formulario: foto });
comprobar('Carga de fotografía', 201, subida.status);
const medio = await subida.json();
const lectura = await solicitar(ruta + '/medios/' + medio.id, { token: jugador });
comprobar('Recuperación privada de fotografía', 'image/jpeg', lectura.headers.get('content-type'));
comprobar('Lectura de medio sin sesión', 401, (await solicitar(ruta + '/medios/' + medio.id)).status);
const comentario = { contenido: 'Comentario de integración de Nexo Gamer.' };
comprobar('Comentario persistido', 201,
  (await solicitar(ruta + '/comentarios', { metodo: 'POST', token: jugador, datos: comentario })).status);
const puntuar = puntuacion => solicitar(ruta + '/valoracion', { metodo: 'PUT', token: jugador, datos: { puntuacion } });
comprobar('Valoración válida', 200, (await puntuar(4)).status);
comprobar('Valoración actualizada sin duplicado', 1, (await (await puntuar(5)).json()).cantidad);
comprobar('Valoración fuera del intervalo', 400, (await puntuar(6)).status);
const panel = await (await solicitar(ruta + '/participacion', { token: jugador })).json();
comprobar('Lectura de comentario', comentario.contenido, panel.comentarios[0].contenido);
comprobar('Lectura de la valoración actual', 5, panel.valoracion.promedio);
console.log(resultados.length + ' verificaciones conformes en Nexo Gamer.');
