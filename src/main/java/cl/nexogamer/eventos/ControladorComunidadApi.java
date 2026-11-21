package cl.nexogamer.eventos;

import cl.nexogamer.eventos.DatosComunidad.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/eventos")
class ControladorComunidadApi {
    private final ServicioComunidad comunidad;

    ControladorComunidadApi(ServicioComunidad comunidad) {
        this.comunidad = comunidad;
    }

    @PostMapping
    ResponseEntity<Identificador> publicar(Principal cuenta, @Valid @RequestBody Publicacion datos) {
        long id = comunidad.publicar(cuenta.getName(), datos);
        return ResponseEntity.created(URI.create("/api/eventos/" + id)).body(new Identificador(id));
    }

    @GetMapping("/{id}/participacion")
    Participacion participacion(@PathVariable long id, Principal cuenta) {
        return comunidad.participacion(id, cuenta.getName());
    }

    @PostMapping("/{id}/medios")
    ResponseEntity<Identificador> archivo(@PathVariable long id, Principal cuenta,
                                          @RequestParam String titulo, @RequestParam MultipartFile archivo) {
        long medio = comunidad.agregarMedio(id, cuenta.getName(), titulo, archivo);
        return ResponseEntity.created(URI.create("/api/eventos/" + id + "/medios/" + medio))
                .body(new Identificador(medio));
    }

    @GetMapping("/{id}/medios/{medio}")
    ResponseEntity<Resource> medio(@PathVariable long id, @PathVariable long medio) {
        return RespuestaMultimedia.de(comunidad.obtenerMedio(id, medio));
    }

    @PostMapping("/{id}/comentarios")
    ResponseEntity<Void> comentar(@PathVariable long id, Principal cuenta, @Valid @RequestBody NuevoComentario datos) {
        comunidad.comentar(id, cuenta.getName(), datos);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}/valoracion")
    Valoracion valorar(@PathVariable long id, Principal cuenta, @Valid @RequestBody NuevaValoracion datos) {
        comunidad.valorar(id, cuenta.getName(), datos);
        return comunidad.participacion(id, cuenta.getName()).valoracion();
    }

    @PostMapping("/{id}/compartir")
    Difusion compartir(@PathVariable long id, Principal cuenta) {
        return comunidad.compartir(id, cuenta.getName());
    }

    record Identificador(long id) {
    }
}
