package cl.nexogamer.eventos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

final class DatosComunidad {
    private DatosComunidad() {
    }

    record Publicacion(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 100) String videojuego,
            @NotBlank @Size(max = 60) String categoria,
            @NotBlank @Size(max = 60) String pais,
            @NotBlank @Size(max = 80) String ciudad,
            @NotNull LocalDate fecha,
            @NotNull LocalTime hora,
            @NotBlank @Pattern(regexp = "Presencial|En línea") String modalidad,
            @NotBlank @Size(max = 255) String resumen,
            @NotBlank @Size(max = 1000) String descripcion,
            @Size(max = 255) String ubicacion,
            @Size(max = 255) String enlace,
            @NotBlank @Size(max = 120) String organizador,
            @NotBlank @Size(max = 400) String requisitos,
            @NotBlank @Size(max = 400) String inscripcion) {
    }

    record NuevoComentario(@NotBlank @Size(max = 600) String contenido) {
    }

    record NuevaValoracion(@NotNull @Min(1) @Max(5) Integer puntuacion) {
    }

    record Medio(long id, String tipo, String titulo) {
        public boolean video() {
            return "video/mp4".equals(tipo);
        }
    }

    record Archivo(String tipo, byte[] datos) {
    }

    record Comentario(String autor, String contenido, LocalDateTime fecha) {
    }

    record Valoracion(double promedio, int cantidad, Integer propia) {
    }

    record Participacion(List<Medio> medios, List<Comentario> comentarios, Valoracion valoracion) {
    }

    record Difusion(String enlace, boolean compartido, String fecha) {
    }
}
