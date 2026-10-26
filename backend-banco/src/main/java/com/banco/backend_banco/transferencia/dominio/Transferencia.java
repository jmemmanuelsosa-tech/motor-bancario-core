package com.banco.backend_banco.transferencia.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencias")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 22)
    private String cbuOrigen;

    @Column(nullable = false, length = 22)
    private String cbuDestino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    // IDEMPOTENCIA
    @Column(nullable = false, unique = true)
    private String codigoOperacion;

    private LocalDateTime fecha;

    protected Transferencia() {
    }

    public Transferencia(String cbuOrigen, String cbuDestino, BigDecimal monto, String codigoOperacion) {
        this.cbuOrigen = cbuOrigen;
        this.cbuDestino = cbuDestino;
        this.monto = monto;
        this.codigoOperacion = codigoOperacion;
        this.fecha = LocalDateTime.now();
    }

    // --- GETTERS (Inmutabilidad: Una transferencia ya hecha jamás se edita) ---

    public Long getId() {
        return id;
    }

    public String getCbuOrigen() {
        return cbuOrigen;
    }

    public String getCbuDestino() {
        return cbuDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}