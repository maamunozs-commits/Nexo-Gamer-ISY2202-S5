package cl.nexogamer.eventos;

import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {ControladorPaginas.class, ControladorComunidadWeb.class})
class ModeloNavegacion {
    @ModelAttribute("autenticado")
    boolean autenticado(Principal usuario) {
        return usuario != null;
    }

    @ModelAttribute("organizador")
    boolean organizador(Principal usuario) {
        return usuario instanceof Authentication cuenta && cuenta.getAuthorities().stream()
                .anyMatch(rol -> "ROLE_ORGANIZADOR".equals(rol.getAuthority()));
    }
}
