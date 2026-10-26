package com.banco.backend_banco.cuenta.servicio;

import com.banco.backend_banco.cuenta.dominio.Cuenta;
import com.banco.backend_banco.cuenta.repositorio.CuentaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    // Inyección de dependencias por constructor
    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    // Método para crear una cuenta nueva con validaciones de negocio
    @Transactional
    public Cuenta crearCuenta(String cbu, BigDecimal saldoInicial) {
        if (cbu == null || cbu.length() != 22) {
            throw new IllegalArgumentException("El CBU debe tener exactamente 22 dígitos.");
        }
        if (cuentaRepository.existsByCbu(cbu)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese CBU.");
        }
        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Una cuenta no puede iniciar con saldo negativo.");
        }

        Cuenta nuevaCuenta = new Cuenta(cbu, saldoInicial);
        return cuentaRepository.save(nuevaCuenta);
    }

    // Método para listar todas las cuentas en la vista
    public List<Cuenta> listarCuentas() {
        return cuentaRepository.findAll();
    }
}