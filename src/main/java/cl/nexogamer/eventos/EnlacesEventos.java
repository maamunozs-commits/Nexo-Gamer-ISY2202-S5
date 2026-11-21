package cl.nexogamer.eventos;

import java.net.URI;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
final class EnlacesEventos {
    private final URI destinoPublico;

    EnlacesEventos(@Value("${nexo.enlace-publico}") String destino) {
        URI analizado = analizar(destino);
        if (analizado == null || analizado.getRawQuery() != null || analizado.getRawFragment() != null
                || (!analizado.getPath().isEmpty() && !"/".equals(analizado.getPath()))) {
            throw new IllegalArgumentException("ENLACE_PUBLICO debe indicar el origen del sitio.");
        }
        boolean https = "https".equalsIgnoreCase(analizado.getScheme());
        boolean local = "http".equalsIgnoreCase(analizado.getScheme())
                && ("localhost".equals(analizado.getHost()) || "127.0.0.1".equals(analizado.getHost()));
        if (!https && !local) {
            throw new IllegalArgumentException("El origen del sitio requiere HTTPS fuera del entorno local.");
        }
        destinoPublico = analizado;
    }

    URI evento(long id) {
        return destinoPublico.resolve("/eventos/" + id);
    }

    static URI canalSeguro(String enlace) {
        URI url = analizar(enlace);
        if (url == null || !"https".equalsIgnoreCase(url.getScheme())
                || (url.getPort() != -1 && url.getPort() != 443)) {
            return null;
        }
        String anfitrion = url.getHost().toLowerCase(Locale.ROOT);
        String ruta = url.getRawPath();
        String consulta = url.getRawQuery();
        boolean autorizado = switch (anfitrion) {
            case "www.twitch.tv", "twitch.tv" -> consulta == null && ruta.matches("/[A-Za-z0-9_]{1,32}/?");
            case "youtu.be" -> ruta.matches("/[A-Za-z0-9_-]{6,32}/?");
            case "www.youtube.com", "youtube.com" ->
                    ruta.matches("/live/[A-Za-z0-9_-]{6,32}/?")
                            || ("/watch".equals(ruta) && consulta != null
                            && consulta.matches("v=[A-Za-z0-9_-]{6,32}"));
            default -> false;
        };
        return autorizado ? url : null;
    }

    URI exigirCanal(String enlace) {
        URI canal = canalSeguro(enlace);
        if (canal == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El canal no está disponible.");
        }
        return canal;
    }

    private static URI analizar(String enlace) {
        if (enlace == null || enlace.isBlank()) {
            return null;
        }
        try {
            URI url = URI.create(enlace.strip());
            return url.getHost() != null && url.getUserInfo() == null ? url : null;
        } catch (IllegalArgumentException excepcion) {
            return null;
        }
    }
}
