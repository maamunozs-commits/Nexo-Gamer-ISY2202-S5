package cl.nexogamer.eventos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.net.URI;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "eventos")
class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String videojuego;
    private String categoria;
    private String pais;
    private String ciudad;
    private LocalDate fecha;
    private LocalTime hora;
    private String zonaHoraria;
    private String modalidad;
    private String resumen;
    private String descripcion;
    private String ubicacion;
    private String enlaceAcceso;
    private String organizador;
    private String requisitos;
    private String inscripcion;
    private String imagen;
    private String fotoDetalle;
    private LocalDate publicadoEn;
    private int popularidad;
    @Column(name = "autor_correo")
    private String autorCorreo;

    protected Evento() {
    }

    Evento(String cuenta, DatosComunidad.Publicacion datos, URI canal) {
        autorCorreo = cuenta;
        nombre = datos.nombre().strip();
        videojuego = datos.videojuego().strip();
        categoria = datos.categoria().strip();
        pais = datos.pais().strip();
        ciudad = datos.ciudad().strip();
        fecha = datos.fecha();
        hora = datos.hora();
        zonaHoraria = "Hora local del evento";
        modalidad = datos.modalidad();
        resumen = datos.resumen().strip();
        descripcion = datos.descripcion().strip();
        ubicacion = "Presencial".equals(modalidad) ? datos.ubicacion().strip() : null;
        enlaceAcceso = "En línea".equals(modalidad) ? canal.toASCIIString() : null;
        organizador = datos.organizador().strip();
        requisitos = datos.requisitos().strip();
        inscripcion = datos.inscripcion().strip();
        imagen = "arena.jpg";
        fotoDetalle = "control-detalle.jpg";
        publicadoEn = LocalDate.now(java.time.ZoneId.of("America/Santiago"));
    }

    long identificador() {
        return id;
    }

    boolean esAutor(String cuenta) {
        return cuenta.equals(autorCorreo);
    }

    CatalogoEventos.Resumen resumen() {
        return new CatalogoEventos.Resumen(id, nombre, videojuego, categoria, pais, ciudad, fecha, modalidad,
                resumen, imagen);
    }

    CatalogoEventos.Detalle detalle() {
        URI canal = EnlacesEventos.canalSeguro(enlaceAcceso);
        return new CatalogoEventos.Detalle(resumen(), hora, zonaHoraria, descripcion, ubicacion,
                canal == null ? null : canal.toASCIIString(),
                organizador, requisitos, inscripcion, "/api/eventos/" + id + "/foto");
    }

    String fotoDetalle() {
        return fotoDetalle;
    }
}
