package com.banco.backend_banco.cuenta.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 22)
    private String cbu;

    // Usamos BigDecimal para dinero (nunca Double) para no perder centavos.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    // CONCURRENCIA
    @Version
    private Long version;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Constructor vacío obligatorio para Hibernate
    protected Cuenta() {
    }

    // Constructor para cuando creamos una cuenta nueva desde nuestro código
    public Cuenta(String cbu, BigDecimal saldoInicial) {
        this.cbu = cbu;
        this.saldo = saldoInicial;
        this.fechaCreacion = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS SEGUROS ---
    public Long getId() {
        return id;
    }

    public String getCbu() {
        return cbu;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}