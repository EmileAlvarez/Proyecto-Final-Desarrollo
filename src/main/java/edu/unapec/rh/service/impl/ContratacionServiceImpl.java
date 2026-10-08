package edu.unapec.rh.service.impl;

import edu.unapec.rh.dto.ContratacionForm;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.repository.*;
import edu.unapec.rh.service.ContratacionService;
import edu.unapec.rh.util.Validacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContratacionServiceImpl implements ContratacionService {
  private final CandidatoRepository candidatos;
  private final EmpleadoRepository empleados;
  private final PuestoRepository puestos;
  private final Validacion validacion;

  @Transactional
  public Empleado contratar(Long candidatoId, ContratacionForm f) {
    validacion.validar(f);
    // Serializa contrataciones concurrentes del mismo candidato; UNIQUE refuerza la garantía.
    Candidato c = candidatos.findForUpdate(candidatoId).orElseThrow(NotFoundException::new);
    if (c.getEstado() == EstadoCandidato.CONTRATADO || empleados.existsByCandidatoId(candidatoId))
      throw new BusinessException("Este candidato ya fue contratado.");
    if (!c.isElegible())
      throw new BusinessException("Reabre la evaluación del candidato antes de contratarlo.");
    if (empleados.existsByCedula(c.getCedula()))
      throw new BusinessException("Ya existe un empleado con esta cédula.");
    Puesto p =
        puestos
            .findById(f.getPuestoId())
            .orElseThrow(() -> new BusinessException("puestoId", "El puesto no existe."));
    if (p.getEstado() != Estado.ACTIVO)
      throw new BusinessException("puestoId", "El puesto definitivo debe estar activo.");
    Empleado e = new Empleado();
    e.setCandidato(c);
    e.setCedula(c.getCedula());
    e.setNombre(c.getNombreCompleto());
    e.setFechaIngreso(f.getFechaIngreso());
    e.setDepartamento(f.getDepartamento().strip());
    e.setPuesto(p);
    e.setSalarioMensual(f.getSalarioMensual());
    e.setEstado(Estado.ACTIVO);
    c.setEstado(EstadoCandidato.CONTRATADO);
    empleados.saveAndFlush(e);
    candidatos.saveAndFlush(c);
    return e;
  }
}
