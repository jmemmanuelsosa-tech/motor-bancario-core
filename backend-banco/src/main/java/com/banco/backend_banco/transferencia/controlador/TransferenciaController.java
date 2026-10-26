package com.banco.backend_banco.transferencia.controlador;
import com.banco.backend_banco.transferencia.dominio.Transferencia;
import com.banco.backend_banco.transferencia.servicio.TransferenciaService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transferencias")
@CrossOrigin(origins = "http://localhost:5173")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @GetMapping
    public List<Transferencia> obtenerHistorial() {
        return transferenciaService.listarHistorial();
    }

    @PostMapping
    public ResponseEntity<?> transferir(@RequestBody Map<String, String> solicitud) {
        try {
            String cbuOrigen = solicitud.get("cbuOrigen");
            String cbuDestino = solicitud.get("cbuDestino");
            BigDecimal monto = new BigDecimal(solicitud.get("monto"));
            String codigoOperacion = solicitud.get("codigoOperacion");

            Transferencia realizada = transferenciaService.realizarTransferencia(
                    cbuOrigen, cbuDestino, monto, codigoOperacion
            );
            return ResponseEntity.ok(realizada);

        } catch (SecurityException e) {
            // Si el módulo Antifraude detecta peligro, devolvemos un código HTTP 403 (Prohibido)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // Si falta saldo o el CBU no existe, devolvemos un código HTTP 400 (Error de datos)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Error de concurrencia: Otra operación modificó el saldo al mismo tiempo. Intente nuevamente."));
        }
    }
}