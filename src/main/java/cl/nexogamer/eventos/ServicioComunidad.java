package cl.nexogamer.eventos;

import cl.nexogamer.eventos.DatosComunidad.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
class ServicioComunidad {
    private final RepositorioEventos eventos;
    private final JdbcClient base;
    private final EnlacesEventos enlaces;
    private final ValidacionMultimedia multimedia;

    ServicioComunidad(RepositorioEventos eventos, JdbcClient base, EnlacesEventos enlaces,
                      ValidacionMultimedia multimedia) {
        this.eventos = eventos;
        this.base = base;
        this.enlaces = enlaces;
        this.multimedia = multimedia;
    }

    @Transactional
    long publicar(String cuenta, Publicacion datos) {
        if ("Presencial".equals(datos.modalidad()) && (datos.ubicacion() == null || datos.ubicacion().isBlank())) {
            throw error(HttpStatus.BAD_REQUEST, "Indica la ubicación del evento presencial.");
        }
        var canal = EnlacesEventos.canalSeguro(datos.enlace());
        if ("En línea".equals(datos.modalidad()) && canal == null) {
            throw error(HttpStatus.BAD_REQUEST, "Indica un canal HTTPS directo de Twitch o YouTube.");
        }
        return eventos.save(new Evento(cuenta, datos, canal)).identificador();
    }

    @Transactional(readOnly = true)
    Participacion participacion(long id, String cuenta) {
        evento(id);
        var medios = base.sql("""
                SELECT id, tipo, titulo FROM medios_evento WHERE evento_id = :evento ORDER BY id DESC
                """).param("evento", id).query(Medio.class).list();
        var comentarios = base.sql("""
                SELECT c.alias_publico AS autor, m.contenido, m.fecha
                FROM comentarios_evento m JOIN cuentas c ON c.correo = m.autor_correo
                WHERE m.evento_id = :evento ORDER BY m.id DESC LIMIT 40
                """).param("evento", id).query(Comentario.class).list();
        var valoracion = base.sql("""
                SELECT COALESCE(ROUND(AVG(puntuacion), 1), 0) AS promedio, COUNT(*) AS cantidad,
                       MAX(CASE WHEN autor_correo = :autor THEN puntuacion END) AS propia
                FROM valoraciones_evento WHERE evento_id = :evento
                """).param("evento", id).param("autor", cuenta)
                .query((fila, indice) -> new Valoracion(fila.getDouble("promedio"), fila.getInt("cantidad"),
                        fila.getObject("propia", Integer.class))).single();
        return new Participacion(medios, comentarios, valoracion);
    }

    @Transactional
    long agregarMedio(long id, String cuenta, String titulo, MultipartFile archivo) {
        if (titulo == null || titulo.isBlank() || titulo.length() > 100) {
            throw error(HttpStatus.BAD_REQUEST, "Indica un título de hasta 100 caracteres.");
        }
        var bloqueado = eventos.bloquear(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "El evento no existe."));
        exigirAutor(bloqueado, cuenta);
        int cantidad = base.sql("SELECT COUNT(*) FROM medios_evento WHERE evento_id = :evento")
                .param("evento", id).query(Integer.class).single();
        if (cantidad >= 8) {
            throw error(HttpStatus.CONFLICT, "La galería admite hasta ocho archivos por evento.");
        }
        var medio = multimedia.validar(archivo);
        var clave = new org.springframework.jdbc.support.GeneratedKeyHolder();
        base.sql("""
                INSERT INTO medios_evento(evento_id, tipo, titulo, datos)
                VALUES(:evento, :tipo, :titulo, :datos)
                """).param("evento", id).param("tipo", medio.tipo()).param("titulo", titulo.strip())
                .param("datos", medio.datos(), java.sql.Types.LONGVARBINARY).update(clave);
        return clave.getKey().longValue();
    }

    @Transactional(readOnly = true)
    Archivo obtenerMedio(long id, long medio) {
        return base.sql("SELECT tipo, datos FROM medios_evento WHERE evento_id = :evento AND id = :medio")
                .param("evento", id).param("medio", medio)
                .query((fila, indice) -> new Archivo(fila.getString("tipo"), fila.getBytes("datos")))
                .optional().orElseThrow(() -> error(HttpStatus.NOT_FOUND, "El archivo no existe."));
    }

    @Transactional
    void comentar(long id, String cuenta, NuevoComentario datos) {
        evento(id);
        base.sql("""
                INSERT INTO comentarios_evento(evento_id, autor_correo, contenido)
                VALUES(:evento, :autor, :contenido)
                """).param("evento", id).param("autor", cuenta).param("contenido", datos.contenido().strip()).update();
    }

    @Transactional
    void valorar(long id, String cuenta, NuevaValoracion datos) {
        evento(id);
        base.sql("""
                INSERT INTO valoraciones_evento(evento_id, autor_correo, puntuacion)
                VALUES(:evento, :autor, :puntuacion) AS nueva
                ON DUPLICATE KEY UPDATE puntuacion = nueva.puntuacion
                """).param("evento", id).param("autor", cuenta).param("puntuacion", datos.puntuacion()).update();
    }

    @Transactional
    Difusion compartir(long id, String cuenta) {
        exigirAutor(evento(id), cuenta);
        base.sql("""
                INSERT INTO difusion_evento(evento_id, autor_correo)
                VALUES(:evento, :autor) AS nueva
                ON DUPLICATE KEY UPDATE autor_correo = nueva.autor_correo
                """).param("evento", id).param("autor", cuenta).update();
        return informacionDifusion(id);
    }

    @Transactional(readOnly = true)
    Difusion informacionDifusion(long id) {
        var fecha = base.sql("SELECT fecha FROM difusion_evento WHERE evento_id = :evento")
                .param("evento", id).query(LocalDateTime.class).optional();
        return new Difusion(enlaces.evento(id).toASCIIString(), fecha.isPresent(),
                fecha.map(LocalDateTime::toString).orElse(null));
    }

    @Transactional(readOnly = true)
    List<CatalogoEventos.Resumen> compartidos() {
        return eventos.compartidos().stream().map(Evento::resumen).toList();
    }

    @Transactional(readOnly = true)
    boolean esAutor(long id, String cuenta) {
        return evento(id).esAutor(cuenta);
    }

    private Evento evento(long id) {
        return eventos.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "El evento no existe."));
    }

    private void exigirAutor(Evento evento, String cuenta) {
        if (!evento.esAutor(cuenta)) {
            throw error(HttpStatus.FORBIDDEN, "Solo la cuenta autora puede realizar esta acción.");
        }
    }

    private ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
