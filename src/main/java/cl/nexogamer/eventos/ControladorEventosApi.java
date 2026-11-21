package cl.nexogamer.eventos;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos")
class ControladorEventosApi {
    private final CatalogoEventos catalogo;

    ControladorEventosApi(CatalogoEventos catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/inicio")
    CatalogoEventos.Inicio inicio() {
        return catalogo.inicio();
    }

    @GetMapping
    CatalogoEventos.Resultados buscar(@Valid @ModelAttribute FiltrosEventos filtros) {
        return catalogo.buscar(filtros);
    }

    @GetMapping("/{id}")
    CatalogoEventos.Detalle detalle(@PathVariable long id) {
        return catalogo.detalle(id);
    }

    @GetMapping(path = "/{id}/foto", produces = MediaType.IMAGE_JPEG_VALUE)
    Resource foto(@PathVariable long id) {
        return catalogo.foto(id);
    }
}
