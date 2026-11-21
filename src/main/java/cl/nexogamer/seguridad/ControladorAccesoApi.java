package cl.nexogamer.seguridad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class ControladorAccesoApi {
    private static final long DURACION_SEGUNDOS = 900;

    private final AuthenticationManager autenticacion;
    private final JwtEncoder firmador;

    ControladorAccesoApi(AuthenticationManager autenticacion, JwtEncoder firmador) {
        this.autenticacion = autenticacion;
        this.firmador = firmador;
    }

    @PostMapping("/api/login")
    Token iniciarSesion(@Valid @RequestBody Credenciales credenciales) {
        if (credenciales.clave().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            var cuenta = autenticacion.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(credenciales.correo(), credenciales.clave()));
            var roles = cuenta.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .map(rol -> rol.substring("ROLE_".length())).toList();
            var ahora = Instant.now();
            var datos = JwtClaimsSet.builder()
                    .issuer(ConfiguracionSeguridad.EMISOR)
                    .audience(List.of(ConfiguracionSeguridad.AUDIENCIA))
                    .subject(cuenta.getName())
                    .issuedAt(ahora)
                    .notBefore(ahora)
                    .expiresAt(ahora.plusSeconds(DURACION_SEGUNDOS))
                    .claim("roles", roles)
                    .build();
            var firma = JwsHeader.with(MacAlgorithm.HS256).build();
            var token = firmador.encode(JwtEncoderParameters.from(firma, datos));
            return new Token(token.getTokenValue(), "Bearer", DURACION_SEGUNDOS, roles);
        } catch (AuthenticationException excepcion) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
    }

    record Credenciales(@NotBlank @Email @Size(max = 160) String correo,
                        @NotBlank @Size(max = 72) String clave) {
    }

    record Token(String token, String tipo, long expiraEn, List<String> roles) {
    }
}
