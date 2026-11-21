package cl.nexogamer.eventos;

import cl.nexogamer.eventos.DatosComunidad.Archivo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
class ValidacionMultimedia {
    private static final int MAXIMO_IMAGEN = 4 * 1024 * 1024;
    private static final int MAXIMO_VIDEO = 10 * 1024 * 1024;
    private static final Set<String> MARCAS_VIDEO = Set.of("isom", "iso2", "mp41", "mp42", "avc1", "M4V ");

    Archivo validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw error(HttpStatus.BAD_REQUEST, "Selecciona una foto o un video.");
        }
        String tipo = archivo.getContentType();
        if ("video/mp4".equals(tipo)) {
            byte[] datos = leer(archivo, MAXIMO_VIDEO);
            if (!esMp4(datos)) {
                throw error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El archivo no tiene una cabecera MP4 válida.");
            }
            return new Archivo(tipo, datos);
        }

        if ("image/jpeg".equals(tipo) || "image/png".equals(tipo)) {
            String formato = "image/jpeg".equals(tipo) ? "jpeg" : "png";
            return new Archivo(tipo, normalizarImagen(leer(archivo, MAXIMO_IMAGEN), formato));
        }
        throw error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Se aceptan fotos JPEG o PNG y videos MP4.");
    }

    private byte[] leer(MultipartFile archivo, int limite) {
        if (archivo.getSize() > limite) {
            throw error(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el límite permitido.");
        }
        try {
            return archivo.getBytes();
        } catch (IOException excepcion) {
            throw error(HttpStatus.BAD_REQUEST, "El archivo no se pudo leer.");
        }
    }

    private byte[] normalizarImagen(byte[] datos, String formato) {
        try (var entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(datos));
             var salida = new ByteArrayOutputStream()) {
            var lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "La foto no se pudo reconocer.");
            }
            var lector = lectores.next();
            try {
                lector.setInput(entrada, true, true);
                if (!formato.equalsIgnoreCase(lector.getFormatName())
                        || (long) lector.getWidth(0) * lector.getHeight(0) > 8_000_000) {
                    throw error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Revisa el formato y las dimensiones de la foto.");
                }
                ImageIO.write(lector.read(0), formato, salida);
                if (salida.size() == 0 || salida.size() > MAXIMO_IMAGEN) {
                    throw error(HttpStatus.PAYLOAD_TOO_LARGE, "El tamaño de la foto supera el límite.");
                }
                return salida.toByteArray();
            } finally {
                lector.dispose();
            }
        } catch (IOException excepcion) {
            throw error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "La foto no se pudo procesar.");
        }
    }

    private boolean esMp4(byte[] datos) {
        if (datos.length < 24) {
            return false;
        }
        int caja = ByteBuffer.wrap(datos).getInt();
        return caja >= 16 && caja <= datos.length
                && "ftyp".equals(new String(datos, 4, 4, StandardCharsets.US_ASCII))
                && MARCAS_VIDEO.contains(new String(datos, 8, 4, StandardCharsets.US_ASCII));
    }

    private ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
