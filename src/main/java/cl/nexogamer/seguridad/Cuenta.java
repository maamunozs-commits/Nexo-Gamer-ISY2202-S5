package cl.nexogamer.seguridad;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas")
class Cuenta {
    @Id
    private String correo;
    private String claveHash;
    private String rol;

    protected Cuenta() {
    }

    String correo() { return correo; }
    String claveHash() { return claveHash; }
    String rol() { return rol; }
}
