package cl.nexogamer.eventos;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class ControladorPaginas {
    private final CatalogoEventos catalogo;

    private final ServicioComunidad comunidad;
    private final EnlacesEventos enlaces;

    ControladorPaginas(CatalogoEventos catalogo, ServicioComunidad comunidad, EnlacesEventos enlaces) {
        this.catalogo = catalogo;
        this.comunidad = comunidad;
        this.enlaces = enlaces;
    }

    @GetMapping("/")
    String raiz() {
        return "redirect:/eventos";
    }

    @GetMapping("/eventos")
    String inicio(Model modelo) {
        modelo.addAttribute("inicio", catalogo.inicio());
        return "inicio";
    }

    @GetMapping("/eventos/buscar")
    String buscar(@Valid @ModelAttribute("filtros") FiltrosEventos filtros, BindingResult validacion, Model modelo) {
        if (!validacion.hasErrors()) {
            modelo.addAttribute("resultados", catalogo.buscar(filtros));
        }
        return "busqueda";
    }

    @GetMapping("/eventos/acceso")
    String acceso(Principal usuario) {
        return usuario == null ? "acceso" : "redirect:/eventos/buscar";
    }

    @GetMapping("/eventos/{id}")
    String detalle(@PathVariable long id, Principal cuenta, Model modelo) {
        modelo.addAttribute("detalle", catalogo.detalle(id));
        modelo.addAttribute("participacion", comunidad.participacion(id, cuenta.getName()));
        boolean autor = comunidad.esAutor(id, cuenta.getName());
        modelo.addAttribute("autorEvento", autor);
        if (autor) {
            modelo.addAttribute("difusion", comunidad.informacionDifusion(id));
        }
        return "detalle";
    }

    @GetMapping("/eventos/{id}/canal")
    ResponseEntity<Void> canal(@PathVariable long id) {
        return ResponseEntity.status(303).location(enlaces.exigirCanal(catalogo.detalle(id).enlaceAcceso())).build();
    }

    @ResponseBody
    @GetMapping(path = "/eventos/{id}/foto", produces = MediaType.IMAGE_JPEG_VALUE)
    Resource foto(@PathVariable long id) {
        return catalogo.foto(id);
    }
}
