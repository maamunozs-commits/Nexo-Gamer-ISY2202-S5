package cl.nexogamer.eventos;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface RepositorioEventos extends JpaRepository<Evento, Long> {
    List<Evento> findTop3ByOrderByPublicadoEnDescIdDesc();

    List<Evento> findTop3ByOrderByPopularidadDescIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> bloquear(long id);

    @Query(value = """
            SELECT e.* FROM eventos e JOIN difusion_evento d ON d.evento_id = e.id
            ORDER BY d.fecha DESC LIMIT 24
            """, nativeQuery = true)
    List<Evento> compartidos();

    @Query("""
            select e from Evento e
            where e.nombre like :nombre escape '!'
              and e.videojuego like :videojuego escape '!'
              and e.categoria like :categoria escape '!'
              and e.pais like :pais escape '!'
              and e.ciudad like :ciudad escape '!'
              and (:fecha is null or e.fecha = :fecha)
              and e.modalidad like :modalidad escape '!'
            """)
    Page<Evento> buscar(String nombre, String videojuego, String categoria, String pais, String ciudad,
                        LocalDate fecha, String modalidad, Pageable pagina);
}
