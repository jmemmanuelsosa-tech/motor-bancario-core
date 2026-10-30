package com.banco.backend_banco.transferencia.servicio;

import com.banco.backend_banco.antifraude.DetectorFraudeService;
import com.banco.backend_banco.cuenta.dominio.Cuenta;
import com.banco.backend_banco.cuenta.repositorio.CuentaRepository;
import com.banco.backend_banco.transferencia.repositorio.TransferenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    // Falsificamos el motor antifraude para que no lance errores en la prueba
    @Mock
    private DetectorFraudeService detectorFraudeService;

    @InjectMocks
    private TransferenciaService transferenciaService;

    @Test
    void cuandoHaySaldoSuficiente_entoncesTransferenciaExitosa() {
        // Arrange (Preparar datos)
        Cuenta origen = new Cuenta("CBU-ORIGEN", new BigDecimal("1000.00"));
        Cuenta destino = new Cuenta("CBU-DESTINO", new BigDecimal("500.00"));

        when(cuentaRepository.findByCbu("CBU-ORIGEN")).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByCbu("CBU-DESTINO")).thenReturn(Optional.of(destino));

        // Act (Ejecutar método)
        transferenciaService.realizarTransferencia("CBU-ORIGEN", "CBU-DESTINO", new BigDecimal("200.00"), "TXN-TEST-1");

        // Assert (Verificar resultados matemáticos)
        assertEquals(new BigDecimal("800.00"), origen.getSaldo());
        assertEquals(new BigDecimal("700.00"), destino.getSaldo());
        
        verify(transferenciaRepository, times(1)).save(any());
    }

    @Test
    void cuandoSaldoInsuficiente_entoncesLanzaExcepcion() {
        // Arrange
        Cuenta origen = new Cuenta("CBU-ORIGEN", new BigDecimal("100.00")); // Saldo bajo
        Cuenta destino = new Cuenta("CBU-DESTINO", new BigDecimal("500.00"));

        when(cuentaRepository.findByCbu("CBU-ORIGEN")).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByCbu("CBU-DESTINO")).thenReturn(Optional.of(destino));

        // Act & Assert 
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            transferenciaService.realizarTransferencia("CBU-ORIGEN", "CBU-DESTINO", new BigDecimal("500.00"), "TXN-TEST-2");
        });

        assertEquals("Saldo insuficiente para realizar la transferencia.", excepcion.getMessage());
        
        verify(transferenciaRepository, never()).save(any());
    }
}