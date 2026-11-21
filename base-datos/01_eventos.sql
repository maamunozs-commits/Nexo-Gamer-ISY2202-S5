SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS eventos_gamer CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE eventos_gamer;

CREATE TABLE cuentas (
    correo VARCHAR(160) NOT NULL PRIMARY KEY,
    clave_hash VARCHAR(60) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    CHECK (rol IN ('JUGADOR', 'ORGANIZADOR'))
);

CREATE TABLE eventos (
    id BIGINT NOT NULL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    videojuego VARCHAR(100) NOT NULL,
    categoria VARCHAR(60) NOT NULL,
    pais VARCHAR(60) NOT NULL,
    ciudad VARCHAR(80) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    zona_horaria VARCHAR(45) NOT NULL,
    modalidad VARCHAR(20) NOT NULL,
    resumen VARCHAR(255) NOT NULL,
    descripcion VARCHAR(1000) NOT NULL,
    ubicacion VARCHAR(255),
    enlace_acceso VARCHAR(255),
    organizador VARCHAR(120) NOT NULL,
    requisitos VARCHAR(400) NOT NULL,
    inscripcion VARCHAR(400) NOT NULL,
    imagen VARCHAR(80) NOT NULL,
    foto_detalle VARCHAR(80) NOT NULL,
    publicado_en DATE NOT NULL,
    popularidad INT NOT NULL,
    INDEX indice_fecha (fecha, hora),
    INDEX indice_publicacion (publicado_en),
    INDEX indice_popularidad (popularidad),
    CHECK (modalidad IN ('Presencial', 'En línea'))
);

CREATE TABLE avisos (
    id BIGINT NOT NULL PRIMARY KEY,
    marca VARCHAR(100) NOT NULL,
    mensaje VARCHAR(255) NOT NULL
);

INSERT INTO cuentas (correo, clave_hash, rol) VALUES
('jugador@nexogamer.test', '$2a$12$J87GD8rKfyphTJqwUup6uev.Wo9.cdrRI2HDuwMTuGY7EUZ590Nn2', 'JUGADOR'),
('invitado@nexogamer.test', '$2a$12$6bZ4y2ghCSWwEE648nwM0ep.xARw3qqf.QK9hx/vu/rCCykrtsNsy', 'JUGADOR'),
('organizador@nexogamer.test', '$2a$12$n/pYxnlOcqTUOjTHWYKhqODkx24XLbChE0dZLF7X1uKUIXR27KcuO', 'ORGANIZADOR');

INSERT INTO avisos (id, marca, mensaje) VALUES
(1, 'Pixel Lab', 'Prueba estaciones de juego y accesorios para PC en los espacios de exhibición.'),
(2, 'Comunidad Respawn', 'Encuentra grupos de práctica y actividades para quienes se inician en los torneos.');

INSERT INTO eventos (
    id, nombre, videojuego, categoria, pais, ciudad, fecha, hora, zona_horaria, modalidad, resumen,
    descripcion, ubicacion, enlace_acceso, organizador, requisitos, inscripcion, imagen, foto_detalle,
    publicado_en, popularidad
) VALUES
(1, 'Arena Valorant Santiago', 'Valorant', 'Torneo', 'Chile', 'Santiago', '2026-11-21', '16:00:00',
 'Hora de Santiago', 'Presencial', 'Clasificatoria por equipos con partidas al mejor de tres.',
 'Jornada de competición con fase de grupos y eliminatorias. El recinto dispone de estaciones de juego y un área de observación para acompañantes.',
 'Sala Arena, Centro de Eventos Pixel, Santiago Centro', NULL, 'Liga Pixel Chile',
 'Equipo de cinco integrantes, cuenta propia del juego y acreditación media hora antes.',
 'Cupos para 16 equipos. El capitán registra el equipo en la mesa de acreditación.',
 'arena.jpg', 'control-detalle.jpg', '2026-11-07', 98),
(2, 'Circuito Rocket League', 'Rocket League', 'Torneo', 'Chile', 'Valparaíso', '2026-11-28', '18:30:00',
 'Hora de Santiago', 'En línea', 'Competencia 3 contra 3 con llaves de eliminación directa.',
 'Cada equipo disputa una serie de tres partidas. La jornada contempla una pausa entre las rondas y una revisión de resultados por parte de la organización.',
 NULL, 'https://www.twitch.tv/rocketleague', 'Circuito Costa Esports',
 'Equipo de tres personas y conexión estable. La persona capitana comunica los resultados.',
 'Cupos para 24 equipos. La organización confirma el registro antes de publicar la llave.',
 'consola.jpg', 'pantalla-detalle.jpg', '2026-11-06', 84),
(3, 'Taller de estrategia LoL', 'League of Legends', 'Taller', 'Chile', 'Concepción', '2026-12-05', '11:00:00',
 'Hora de Santiago', 'Presencial', 'Práctica guiada de visión, rotaciones y control de objetivos.',
 'Se revisan repeticiones y se realizan ejercicios por equipos. La primera parte aborda decisiones de mapa y la segunda permite aplicarlas en una partida de práctica.',
 'Sala Taller, Espacio Comunidad Pixel, Concepción', NULL, 'Comunidad Respawn',
 'Conocer los controles básicos del juego y llevar audífonos.',
 'Inscripción individual en recepción. Se asignan 20 cupos según orden de llegada.',
 'estacion.jpg', 'control-detalle.jpg', '2026-11-05', 53),
(4, 'Encuentro Indie del Pacífico', 'Videojuegos independientes', 'Exhibición', 'Perú', 'Lima', '2026-12-12', '15:00:00',
 'Hora de Lima', 'Presencial', 'Muestra de prototipos con sesiones de prueba y conversación con estudios locales.',
 'Los estudios presentan prototipos jugables. El programa alterna pruebas breves, demostraciones y conversaciones sobre diseño y desarrollo de videojuegos.',
 'Sala Demo, Espacio Creativo Pacífico, Lima', NULL, 'Colectivo Bit Sur',
 'Ingreso para público general. Los equipos expositores llevan sus propios prototipos.',
 'La entrada se solicita en el punto de atención. Los turnos de prueba se asignan al llegar.',
 'exhibicion.jpg', 'pantalla-detalle.jpg', '2026-11-03', 76),
(5, 'Copa Bogotá de Combate', 'Street Fighter 6', 'Torneo', 'Colombia', 'Bogotá', '2026-12-19', '17:00:00',
 'Hora de Bogotá', 'Presencial', 'Competencia individual con doble eliminación y partidas de práctica.',
 'El programa incluye una fase de práctica libre y un torneo individual. El personal de sala verifica la configuración de cada estación antes de iniciar las rondas.',
 'Sala de Competición, Centro Arcade, Bogotá', NULL, 'Colectivo Arcade Bogotá',
 'Cuenta de jugador y control compatible con la estación asignada.',
 'Cupos para 32 participantes. El registro se confirma en recepción antes de la primera ronda.',
 'consola.jpg', 'pantalla-detalle.jpg', '2026-11-02', 68),
(6, 'Reto de Construcción Creativa', 'Minecraft', 'Encuentro', 'Argentina', 'Buenos Aires', '2027-01-09', '18:00:00',
 'Hora de Buenos Aires', 'En línea', 'Reto cooperativo de construcción con un tema común y revisión de los proyectos.',
 'Los equipos trabajan en un mundo compartido durante dos horas. El cierre reserva un bloque para mostrar las construcciones y explicar las decisiones de diseño.',
 NULL, 'https://www.twitch.tv/minecraft', 'Comunidad Bloque Abierto',
 'Cuenta del juego, micrófono y equipo de dos a cuatro participantes.',
 'El equipo registra un nombre y una persona de contacto con la organización.',
 'estacion.jpg', 'pantalla-detalle.jpg', '2026-11-01', 91),
(7, 'Encuentro de Simulación Austral', 'Assetto Corsa Competizione', 'Encuentro', 'Chile', 'Puerto Montt', '2027-01-16', '14:00:00',
 'Hora de Santiago', 'Presencial', 'Sesiones de simulación con práctica, clasificación y carrera corta.',
 'Cada participante dispone de una sesión para configurar su posición de conducción. Las series se forman a partir de los tiempos de clasificación.',
 'Sala Simulación, Espacio Austral, Puerto Montt', NULL, 'Club Simulación Austral',
 'Puntualidad para el turno de práctica. La organización facilita el volante.',
 'Los turnos se reservan en recepción hasta completar 24 plazas.',
 'exhibicion.jpg', 'control-detalle.jpg', '2026-10-29', 60),
(8, 'Clínica de puntería FPS', 'Counter-Strike 2', 'Taller', 'Chile', 'Santiago', '2027-01-23', '19:00:00',
 'Hora de Santiago', 'En línea', 'Ejercicios guiados para ajustar sensibilidad, posición y trabajo en equipo.',
 'La sesión comienza con ajustes de sensibilidad y seguimiento. Luego se practican posiciones de equipo y se revisan las grabaciones de las rondas.',
 NULL, 'https://www.twitch.tv/eslcs', 'Laboratorio FPS Chile',
 'Cuenta del juego, micrófono y cliente actualizado.',
 'Inscripción individual con 18 cupos por sesión de práctica.',
 'arena.jpg', 'control-detalle.jpg', '2026-10-27', 65);
