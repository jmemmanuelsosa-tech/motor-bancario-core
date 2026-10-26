package com.banco.backend_banco.transferencia.servicio;
import com.banco.backend_banco.antifraude.DetectorFraudeService;
import com.banco.backend_banco.cuenta.dominio.Cuenta;
import com.banco.backend_banco.cuenta.repositorio.CuentaRepository;
import com.banco.backend_banco.transferencia.dominio.Transferencia;
import com.banco.backend_banco.transferencia.repositorio.TransferenciaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferenciaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final DetectorFraudeService detectorFraudeService;

    public TransferenciaService(CuentaRepository cuentaRepository,
                                TransferenciaRepository transferenciaRepository,
                                DetectorFraudeService detectorFraudeService) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.detectorFraudeService = detectorFraudeService;
    }

    //TRANSACCIONALIDAD ACID (@Transactional): 
    @Transactional
    public Transferencia realizarTransferencia(String cbuOrigen, String cbuDestino, BigDecimal monto, String codigoOperacion) {

        // Chequeo de Idempotencia
        if (transferenciaRepository.existsByCodigoOperacion(codigoOperacion)) {
            throw new IllegalArgumentException("Operación duplicada: Esta transferencia ya fue procesada.");
        }

        // Validación básica del monto
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser mayor a $0.");
        }

        // CIBERSEGURIDAD
        detectorFraudeService.evaluarRiesgoTransferencia(cbuOrigen, cbuDestino, monto);

        // Buscamos ambas cuentas en la base de datos
        Cuenta origen = cuentaRepository.findByCbu(cbuOrigen)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta de origen no existe."));

        Cuenta destino = cuentaRepository.findByCbu(cbuDestino)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta de destino (CBU) no existe."));

        // Verificamos que tenga saldo suficiente
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar la transferencia.");
        }

        // Movimiento de dinero seguro con BigDecimal
        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        // Registramos el comprobante histórico
        Transferencia comprobante = new Transferencia(cbuOrigen, cbuDestino, monto, codigoOperacion);
        return transferenciaRepository.save(comprobante);
    }

    public List<Transferencia> listarHistorial() {
        return transferenciaRepository.findAll();
    }
}