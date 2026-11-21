package cl.nexogamer.seguridad;

import org.springframework.data.jpa.repository.JpaRepository;

interface RepositorioCuentas extends JpaRepository<Cuenta, String> {
}
