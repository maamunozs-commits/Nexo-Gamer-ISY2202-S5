package cl.nexogamer.eventos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
class CatalogoEventos {
    private final RepositorioEventos eventos;
    private final RepositorioAvisos avisos;

    CatalogoEventos(RepositorioEventos eventos, RepositorioAvisos avisos) {
        this.eventos = eventos;
        this.avisos = avisos;
    }

    Inicio inicio() {
        return new Inicio(eventos.findTop3ByOrderByPublicadoEnDescIdDesc().stream().map(Evento::resumen).toList(),
                eventos.findTop3ByOrderByPopularidadDescIdAsc().stream().map(Evento::resumen).toList(),
                avisos.findAllByOrderByIdAsc().stream().map(Aviso::banner).toList());
    }

    Resultados buscar(FiltrosEventos filtros) {
        var pagina = PageRequest.of(filtros.indice(), 12, Sort.by("fecha", "hora", "id"));
        var resultados = eventos.buscar(patron(filtros.nombre()), patron(filtros.videojuego()),
                patron(filtros.categoria()), patron(filtros.pais()), patron(filtros.ciudad()), filtros.fecha(),
                patron(filtros.modalidad()), pagina);
        return new Resultados(resultados.map(Evento::resumen).getContent(), resultados.getTotalElements(),
                resultados.getNumber(), resultados.hasPrevious(), resultados.hasNext());
    }

    Detalle detalle(long id) {
        return porId(id).detalle();
    }

    Resource foto(long id) {
        String nombre = porId(id).fotoDetalle();
        if (!nombre.matches("[a-z0-9-]+\\.jpg")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var foto = new ClassPathResource("galeria/" + nombre);
        if (!foto.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return foto;
    }

    private Evento porId(long id) {
        return eventos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String patron(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        return "%" + texto.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    public record Resumen(long id, String nombre, String videojuego, String categoria, String pais, String ciudad,
                          LocalDate fecha, String modalidad, String resumen, String imagen) {
    }

    public record Detalle(Resumen evento, LocalTime hora, String zonaHoraria, String descripcion, String ubicacion,
                          String enlaceAcceso, String organizador, String requisitos, String inscripcion, String foto) {
    }

    public record Banner(String marca, String mensaje) {
    }

    public record Inicio(List<Resumen> recientes, List<Resumen> populares, List<Banner> avisos) {
    }

    public record Resultados(List<Resumen> eventos, long total, int pagina, boolean anterior, boolean siguiente) {
    }
}
