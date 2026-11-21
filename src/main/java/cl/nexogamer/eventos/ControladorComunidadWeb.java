package cl.nexogamer.eventos;

import cl.nexogamer.eventos.DatosComunidad.*;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/eventos")
class ControladorComunidadWeb {
    private final ServicioComunidad comunidad;

    ControladorComunidadWeb(ServicioComunidad comunidad) {
        this.comunidad = comunidad;
    }

    @GetMapping("/publicar")
    String formulario(Model modelo) {
        modelo.addAttribute("datos", new Publicacion("", "", "", "Chile", "", null, null, "Presencial",
                "", "", "", "", "", "", ""));
        return "publicar";
    }

    @PostMapping("/publicar")
    String publicar(@Valid @ModelAttribute("datos") Publicacion datos, BindingResult errores,
                    Principal cuenta, Model modelo, RedirectAttributes mensajes) {
        if (errores.hasErrors()) {
            return "publicar";
        }
        try {
            long id = comunidad.publicar(cuenta.getName(), datos);
            mensajes.addFlashAttribute("confirmacion", "Evento publicado.");
            return detalle(id);
        } catch (ResponseStatusException fallo) {
            modelo.addAttribute("aviso", fallo.getReason());
            return "publicar";
        }
    }

    @GetMapping("/comunidad")
    String comunidad(Model modelo) {
        modelo.addAttribute("eventos", comunidad.compartidos());
        return "comunidad";
    }

    @GetMapping("/{id}/medios/{medio}")
    @ResponseBody
    ResponseEntity<Resource> medio(@PathVariable long id, @PathVariable long medio) {
        return RespuestaMultimedia.de(comunidad.obtenerMedio(id, medio));
    }

    @PostMapping("/{id}/medios")
    String cargar(@PathVariable long id, Principal cuenta, @RequestParam String titulo,
                  @RequestParam MultipartFile archivo, RedirectAttributes mensajes) {
        try {
            comunidad.agregarMedio(id, cuenta.getName(), titulo, archivo);
            mensajes.addFlashAttribute("confirmacion", "Archivo agregado a la galería.");
        } catch (ResponseStatusException fallo) {
            if (fallo.getStatusCode().value() == 403 || fallo.getStatusCode().value() == 404) {
                throw fallo;
            }
            mensajes.addFlashAttribute("aviso", fallo.getReason());
        }
        return detalle(id);
    }

    @PostMapping("/{id}/comentarios")
    String comentar(@PathVariable long id, Principal cuenta,
                    @Valid @ModelAttribute NuevoComentario datos, BindingResult errores,
                    RedirectAttributes mensajes) {
        if (errores.hasErrors()) {
            mensajes.addFlashAttribute("aviso", "Escribe un comentario de hasta 600 caracteres.");
        } else {
            comunidad.comentar(id, cuenta.getName(), datos);
            mensajes.addFlashAttribute("confirmacion", "Comentario publicado.");
        }
        return detalle(id);
    }

    @PostMapping("/{id}/valoracion")
    String valorar(@PathVariable long id, Principal cuenta,
                   @Valid @ModelAttribute NuevaValoracion datos, BindingResult errores,
                   RedirectAttributes mensajes) {
        if (errores.hasErrors()) {
            mensajes.addFlashAttribute("aviso", "Selecciona una valoración entre 1 y 5.");
        } else {
            comunidad.valorar(id, cuenta.getName(), datos);
            mensajes.addFlashAttribute("confirmacion", "Valoración guardada.");
        }
        return detalle(id);
    }

    @PostMapping("/{id}/compartir")
    String compartir(@PathVariable long id, Principal cuenta, RedirectAttributes mensajes) {
        comunidad.compartir(id, cuenta.getName());
        mensajes.addFlashAttribute("confirmacion", "Evento compartido en la comunidad.");
        return detalle(id);
    }

    private String detalle(long id) {
        return "redirect:/eventos/" + id;
    }
}
