package com.banco.backend_banco.antifraude;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class DetectorFraudeService {

    // Límite de seguridad por transferencia sin segundo factor (2FA)
    private static final BigDecimal LIMITE_ALERTA_FRAUDE = new BigDecimal("500000.00");

 
    //Analiza patrones sospechosos antes de comprometer los fondos del cliente
    public void evaluarRiesgoTransferencia(String cbuOrigen, String cbuDestino, BigDecimal monto) {
        
        // Regla 1: Evitar auto-transferencias
        if (cbuOrigen.equals(cbuDestino)) {
            throw new SecurityException("BLOQUEO DE SEGURIDAD: No puedes transferir dinero a la misma cuenta de origen.");
        }

        // Regla 2: Prevención de vaciamiento de cuenta (Phishing / Robo de sesión)
        if (monto.compareTo(LIMITE_ALERTA_FRAUDE) > 0) {
            throw new SecurityException("ALERTA ANTIFRAUDE: El monto supera el límite seguro ($500.000). Operación retenida preventivamente por seguridad.");
        }
    }
}