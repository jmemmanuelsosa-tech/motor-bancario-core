package com.banco.backend_banco.transferencia.repositorio;
import com.banco.backend_banco.transferencia.dominio.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    // Revisa si ese código de operación ya fue procesado antes
    boolean existsByCodigoOperacion(String codigoOperacion);
}