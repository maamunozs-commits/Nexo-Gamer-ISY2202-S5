package cl.nexogamer.eventos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RepositorioAvisos extends JpaRepository<Aviso, Long> {
    List<Aviso> findAllByOrderByIdAsc();
}
