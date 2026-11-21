SET NAMES utf8mb4;
USE eventos_gamer;
START TRANSACTION;

UPDATE cuentas SET clave_hash = @hash_jugador WHERE correo = 'jugador@nexogamer.test';
UPDATE cuentas SET clave_hash = @hash_invitado WHERE correo = 'invitado@nexogamer.test';
UPDATE cuentas SET clave_hash = @hash_organizador WHERE correo = 'organizador@nexogamer.test';
COMMIT;
