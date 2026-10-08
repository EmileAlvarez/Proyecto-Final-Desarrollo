package edu.unapec.rh.repository;

import edu.unapec.rh.entity.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CandidatoRepository
    extends JpaRepository<Candidato, Long>, JpaSpecificationExecutor<Candidato> {
  boolean existsByCedulaAndIdNot(String cedula, Long id);

  long countByEstadoIn(Collection<EstadoCandidato> estados);

  List<Candidato> findTop5ByOrderByFechaRegistroDescIdDesc();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Candidato c where c.id = :id")
  Optional<Candidato> findForUpdate(@Param("id") Long id);
}
