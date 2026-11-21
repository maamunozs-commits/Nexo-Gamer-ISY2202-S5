SET NAMES utf8mb4;
USE eventos_gamer;

ALTER TABLE cuentas ADD COLUMN alias_publico VARCHAR(60) NOT NULL DEFAULT 'Participante';
UPDATE cuentas SET alias_publico = 'Jugador de la comunidad' WHERE correo = 'jugador@nexogamer.test';
UPDATE cuentas SET alias_publico = 'Invitado de la comunidad' WHERE correo = 'invitado@nexogamer.test';
UPDATE cuentas SET alias_publico = 'Organización Nexo Gamer' WHERE correo = 'organizador@nexogamer.test';

ALTER TABLE eventos
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT,
    ADD COLUMN autor_correo VARCHAR(160),
    ADD CONSTRAINT fk_evento_autor FOREIGN KEY (autor_correo) REFERENCES cuentas(correo);

UPDATE eventos SET autor_correo = 'organizador@nexogamer.test' WHERE autor_correo IS NULL;

CREATE TABLE medios_evento (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    tipo VARCHAR(28) NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    datos MEDIUMBLOB NOT NULL,
    fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_medio_evento FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    CONSTRAINT ck_medio_tipo CHECK (tipo IN ('image/jpeg', 'image/png', 'video/mp4')),
    INDEX ix_medio_evento (evento_id, id)
);

CREATE TABLE comentarios_evento (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    autor_correo VARCHAR(160) NOT NULL,
    contenido VARCHAR(600) NOT NULL,
    fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_comentario_evento FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    CONSTRAINT fk_comentario_cuenta FOREIGN KEY (autor_correo) REFERENCES cuentas(correo),
    INDEX ix_comentario_evento (evento_id, id)
);

CREATE TABLE valoraciones_evento (
    evento_id BIGINT NOT NULL,
    autor_correo VARCHAR(160) NOT NULL,
    puntuacion TINYINT NOT NULL,
    PRIMARY KEY (evento_id, autor_correo),
    CONSTRAINT fk_valoracion_evento FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    CONSTRAINT fk_valoracion_cuenta FOREIGN KEY (autor_correo) REFERENCES cuentas(correo),
    CONSTRAINT ck_valoracion_rango CHECK (puntuacion BETWEEN 1 AND 5)
);

CREATE TABLE difusion_evento (
    evento_id BIGINT NOT NULL PRIMARY KEY,
    autor_correo VARCHAR(160) NOT NULL,
    fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_difusion_evento FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    CONSTRAINT fk_difusion_cuenta FOREIGN KEY (autor_correo) REFERENCES cuentas(correo)
);
