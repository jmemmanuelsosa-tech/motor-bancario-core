package com.banco.backend_banco;
import com.banco.backend_banco.cuenta.repositorio.CuentaRepository;
import com.banco.backend_banco.cuenta.servicio.CuentaService;
import com.banco.backend_banco.seguridad.Usuario;
import com.banco.backend_banco.seguridad.UsuarioRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CargadorDatosIniciales implements CommandLineRunner {

    private final CuentaService cuentaService;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CargadorDatosIniciales(CuentaService cuentaService,
                                  CuentaRepository cuentaRepository,
                                  UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder) {
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (cuentaRepository.count() == 0) {
            cuentaService.crearCuenta("0110000000000000000001", new BigDecimal("1000000.00"));
            cuentaService.crearCuenta("0070000000000000000002", new BigDecimal("250000.00"));
        }

        if (!usuarioRepository.existsByUsername("admin")) {
            String hashBCrypt = passwordEncoder.encode("Banco2026*");
            usuarioRepository.save(new Usuario("admin", hashBCrypt, "Administrador"));
        }
    }
}