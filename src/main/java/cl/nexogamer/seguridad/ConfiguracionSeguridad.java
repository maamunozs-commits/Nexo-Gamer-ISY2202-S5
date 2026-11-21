package cl.nexogamer.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
class ConfiguracionSeguridad {
    static final String EMISOR = "nexo-gamer";
    static final String AUDIENCIA = "api-eventos";
    private static final String POLITICA_CONTENIDO = "default-src 'none'; style-src 'self'; img-src 'self'; "
            + "script-src 'none'; media-src 'self'; object-src 'none'; base-uri 'none'; form-action 'self'; frame-ancestors 'none'";

    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http, JwtAuthenticationConverter conversor) throws Exception {
        return http.securityMatcher("/api/**")
                .headers(this::protegerRespuestas)
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/eventos", "/api/eventos/inicio").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/eventos", "/api/eventos/{id}/medios",
                                "/api/eventos/{id}/compartir").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.GET, "/api/eventos/{id}", "/api/eventos/{id}/foto",
                                "/api/eventos/{id}/participacion", "/api/eventos/{id}/medios/{medio}")
                        .hasAnyRole("JUGADOR", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.POST, "/api/eventos/{id}/comentarios")
                        .hasAnyRole("JUGADOR", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/eventos/{id}/valoracion")
                        .hasAnyRole("JUGADOR", "ORGANIZADOR")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(servidor -> servidor.jwt(jwt -> jwt.jwtAuthenticationConverter(conversor)))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain web(HttpSecurity http, AuthenticationManager autenticacion) throws Exception {
        return http.authenticationManager(autenticacion)
                .headers(this::protegerRespuestas)
                .authorizeHttpRequests(rutas -> rutas
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/eventos", "/eventos/buscar", "/eventos/acceso",
                                "/css/**", "/img/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/eventos/acceso").permitAll()
                        .requestMatchers(HttpMethod.GET, "/eventos/publicar").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.POST, "/eventos/publicar", "/eventos/{id}/medios",
                                "/eventos/{id}/compartir").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.GET, "/eventos/{id}", "/eventos/{id}/foto",
                                "/eventos/{id}/medios/{medio}", "/eventos/{id}/canal", "/eventos/comunidad")
                        .hasAnyRole("JUGADOR", "ORGANIZADOR")
                        .requestMatchers(HttpMethod.POST, "/eventos/{id}/comentarios", "/eventos/{id}/valoracion")
                        .hasAnyRole("JUGADOR", "ORGANIZADOR")
                        .anyRequest().denyAll())
                .csrf(Customizer.withDefaults())
                .formLogin(formulario -> formulario.loginPage("/eventos/acceso")
                        .usernameParameter("correo").passwordParameter("clave")
                        .defaultSuccessUrl("/eventos/buscar")
                        .failureUrl("/eventos/acceso?error"))
                .logout(salida -> salida.logoutUrl("/eventos/salir")
                        .logoutSuccessUrl("/eventos").deleteCookies("JSESSIONID"))
                .build();
    }

    private void protegerRespuestas(HeadersConfigurer<HttpSecurity> cabeceras) {
        cabeceras.contentSecurityPolicy(politica -> politica.policyDirectives(POLITICA_CONTENIDO))
                .referrerPolicy(politica -> politica.policy(ReferrerPolicy.NO_REFERRER))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=()"));
    }

    @Bean
    UserDetailsService usuarios(RepositorioCuentas cuentas) {
        return correo -> cuentas.findById(correo.strip().toLowerCase(Locale.ROOT))
                .map(cuenta -> User.withUsername(cuenta.correo()).password(cuenta.claveHash())
                        .roles(cuenta.rol()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
    }

    @Bean
    PasswordEncoder codificador() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationManager autenticacion(UserDetailsService usuarios, PasswordEncoder codificador) {
        var proveedor = new DaoAuthenticationProvider(usuarios);
        proveedor.setPasswordEncoder(codificador);
        return new ProviderManager(proveedor);
    }

    @Bean
    SecretKey llaveJwt(@Value("${nexo.clave-jwt}") String clave) {
        byte[] llave = Base64.getDecoder().decode(clave);
        if (llave.length < 32) {
            throw new IllegalArgumentException("CLAVE_JWT debe contener al menos 32 bytes en Base64");
        }
        return new SecretKeySpec(llave, "HmacSHA256");
    }

    @Bean
    JwtEncoder firmador(SecretKey llave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(llave));
    }

    @Bean
    JwtDecoder verificador(SecretKey llave) {
        var verificador = NimbusJwtDecoder.withSecretKey(llave).macAlgorithm(MacAlgorithm.HS256).build();
        JwtClaimValidator<java.util.List<String>> audiencia =
                new JwtClaimValidator<>("aud", valores -> valores != null && valores.contains(AUDIENCIA));
        verificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(EMISOR), audiencia));
        return verificador;
    }

    @Bean
    JwtAuthenticationConverter rolesJwt() {
        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        return conversor;
    }
}
