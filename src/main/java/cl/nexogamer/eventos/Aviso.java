package cl.nexogamer.eventos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "avisos")
class Aviso {
    @Id
    private Long id;
    private String marca;
    private String mensaje;

    protected Aviso() {
    }

    CatalogoEventos.Banner banner() {
        return new CatalogoEventos.Banner(marca, mensaje);
    }
}
