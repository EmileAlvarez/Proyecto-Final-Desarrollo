package edu.unapec.rh.repository;

import edu.unapec.rh.entity.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface EmpleadoRepository
    extends JpaRepository<Empleado, Long>, JpaSpecificationExecutor<Empleado> {
  boolean existsByCedula(String cedula);

  boolean existsByCandidatoId(Long id);

  List<Empleado> findByFechaIngresoBetweenOrderByFechaIngresoDescIdDesc(
      LocalDate desde, LocalDate hasta);

  List<Empleado> findTop5ByOrderByFechaIngresoDescIdDesc();
}
