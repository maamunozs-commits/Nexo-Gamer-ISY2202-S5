package cl.nexogamer.eventos;

import cl.nexogamer.eventos.DatosComunidad.Archivo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

final class RespuestaMultimedia {
    private RespuestaMultimedia() {
    }

    static ResponseEntity<Resource> de(Archivo archivo) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(archivo.tipo()))
                .body(new ByteArrayResource(archivo.datos()));
    }
}
