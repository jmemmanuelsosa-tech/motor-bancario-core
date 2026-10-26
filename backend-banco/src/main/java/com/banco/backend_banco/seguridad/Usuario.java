package com.banco.backend_banco.seguridad;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    // SEGURIDAD
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String rol;

    // PREVENCIÓN DE FUERZA BRUTA
    @Column(nullable = false)
    private int intentosFallidos = 0;

    @Column(nullable = false)
    private boolean cuentaBloqueada = false;

    private LocalDateTime fechaCreacion;

    protected Usuario() {
    }

    public Usuario(String username, String passwordHash, String rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.intentosFallidos = 0;
        this.cuentaBloqueada = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS CONTROLADOS ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public boolean isCuentaBloqueada() { return cuentaBloqueada; }

    public void registrarIntentoFallido() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 3) {
            this.cuentaBloqueada = true;
        }
    }

    public void resetearIntentos() {
        this.intentosFallidos = 0;
    }
}