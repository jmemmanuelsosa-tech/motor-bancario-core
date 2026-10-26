package com.banco.backend_banco.cuenta.repositorio;
import com.banco.backend_banco.cuenta.dominio.Cuenta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByCbu(String cbu);

    // Verifica si ya existe una cuenta con ese CBU
    boolean existsByCbu(String cbu);
}
