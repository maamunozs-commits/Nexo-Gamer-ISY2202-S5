package cl.nexogamer.eventos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

record FiltrosEventos(
        @Size(max = 120) String nombre,
        @Size(max = 100) String videojuego,
        @Size(max = 60) String categoria,
        @Size(max = 60) String pais,
        @Size(max = 80) String ciudad,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
        @Pattern(regexp = "|Presencial|En línea") String modalidad,
        @Min(0) @Max(1000) Integer pagina) {

    int indice() {
        return pagina == null ? 0 : pagina;
    }
}
