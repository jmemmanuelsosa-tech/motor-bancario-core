package com.banco.backend_banco.seguridad;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    // Inyectamos el BCryptPasswordEncoder oficial de Spring Security
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Map<String, String> datos) {
        String username = datos.get("username");
        String password = datos.get("password");

        if (username == null || username.trim().length() < 4) {
            throw new IllegalArgumentException("El nombre de usuario debe tener al menos 4 caracteres.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Por política de seguridad, la contraseña debe tener al menos 6 caracteres.");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya se encuentra registrado.");
        }

        // Encriptamos con BCrypt
        String hashBCrypt = passwordEncoder.encode(password);
        Usuario nuevo = new Usuario(username, hashBCrypt, "Administrador");
        usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Usuario registrado con encriptación BCrypt. Ya puede iniciar sesión."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Map<String, String> credenciales) {
        String username = credenciales.get("username");
        String password = credenciales.get("password");

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas. Verifique su usuario y clave."));
        }

        // Verificación de bloqueo por fuerza bruta
        if (usuario.isCuentaBloqueada()) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("error", "CUENTA BLOQUEADA POR SEGURIDAD: Se superó el límite de 3 intentos fallidos."));
        }

        // Verificamos la clave usando el método matches() de BCrypt
        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            usuario.registrarIntentoFallido();
            usuarioRepository.save(usuario);

            int restantes = 3 - usuario.getIntentosFallidos();
            if (restantes <= 0) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(Map.of("error", "Has fallado 3 veces. Tu usuario ha sido BLOQUEADO preventivamente."));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Contraseña incorrecta. Intentos restantes antes del bloqueo: " + restantes));
        }

        // Login correcto: reseteamos los intentos fallidos a 0
        usuario.resetearIntentos();
        usuarioRepository.save(usuario);

        String tokenSesion = "BEARER-" + UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of(
                "usuario", usuario.getUsername(),
                "rol", usuario.getRol(),
                "token", tokenSesion
        ));
    }
}