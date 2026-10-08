package edu.unapec.rh.service;

import edu.unapec.rh.entity.*;
import edu.unapec.rh.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final CandidatoRepository candidatos;
  private final EmpleadoRepository empleados;
  private final PuestoRepository puestos;
  private final CompetenciaRepository competencias;
  private final CapacitacionRepository capacitaciones;

  @Transactional(readOnly = true)
  public Map<String, Object> datos() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("totalCandidatos", candidatos.count());
    m.put(
        "activos",
        candidatos.countByEstadoIn(
            List.of(
                EstadoCandidato.REGISTRADO,
                EstadoCandidato.EN_EVALUACION,
                EstadoCandidato.PRESELECCIONADO)));
    m.put("totalEmpleados", empleados.count());
    m.put("totalPuestos", puestos.countByEstado(Estado.ACTIVO));
    m.put("totalCompetencias", competencias.count());
    m.put("totalCapacitaciones", capacitaciones.count());
    m.put("ultimosCandidatos", candidatos.findTop5ByOrderByFechaRegistroDescIdDesc());
    m.put("ultimosEmpleados", empleados.findTop5ByOrderByFechaIngresoDescIdDesc());
    return m;
  }
}
