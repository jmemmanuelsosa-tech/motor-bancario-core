package com.banco.backend_banco.cuenta.controlador;

import com.banco.backend_banco.cuenta.dominio.Cuenta;
import com.banco.backend_banco.cuenta.servicio.CuentaService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "http://localhost:5173")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public List<Cuenta> obtenerTodas() {
        return cuentaService.listarCuentas();
    }

    @PostMapping
    public ResponseEntity<?> crearCuenta(@RequestBody Map<String, String> datos) {
        try {
            String cbu = datos.get("cbu");
            BigDecimal saldo = new BigDecimal(datos.get("saldoInicial"));
            Cuenta creada = cuentaService.crearCuenta(cbu, saldo);
            return ResponseEntity.ok(creada);
        } catch (IllegalArgumentException e) {
            // Si falla una validación, devolvemos un error 400 claro al frontend
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}