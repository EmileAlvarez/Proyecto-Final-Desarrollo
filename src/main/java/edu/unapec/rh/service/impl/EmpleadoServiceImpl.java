package edu.unapec.rh.service.impl;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.repository.*;
import edu.unapec.rh.service.EmpleadoService;
import edu.unapec.rh.util.Validacion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpleadoServiceImpl implements EmpleadoService {
  private final EmpleadoRepository empleados;
  private final PuestoRepository puestos;
  private final Validacion validacion;

  public Empleado obtener(Long id) {
    return empleados.findById(id).orElseThrow(NotFoundException::new);
  }

  public EmpleadoForm formulario(Long id) {
    Empleado e = obtener(id);
    EmpleadoForm f = new EmpleadoForm();
    BeanUtils.copyProperties(e, f);
    f.setPuestoId(e.getPuesto().getId());
    return f;
  }

  @Transactional
  public void actualizar(Long id, EmpleadoForm f) {
    validacion.validar(f);
    Empleado e = obtener(id);
    Validacion.version(f.getVersion(), e.getVersion());
    Puesto p =
        puestos
            .findById(f.getPuestoId())
            .orElseThrow(() -> new BusinessException("puestoId", "El puesto no existe."));
    if (p.getEstado() != Estado.ACTIVO && !p.getId().equals(e.getPuesto().getId()))
      throw new BusinessException("puestoId", "Solo puedes asignar puestos activos.");
    BeanUtils.copyProperties(f, e, "version");
    e.setPuesto(p);
    e.setDepartamento(f.getDepartamento().strip());
  }

  @Transactional
  public void cambiarEstado(Long id, long version) {
    Empleado e = obtener(id);
    Validacion.version(version, e.getVersion());
    e.setEstado(e.getEstado() == Estado.ACTIVO ? Estado.INACTIVO : Estado.ACTIVO);
  }

  public Page<Empleado> buscar(EmpleadoFiltro f, int page) {
    validacion.validar(f);
    return empleados.findAll(
        ConsultaSpecifications.empleados(f),
        PageRequest.of(
            Math.max(0, page),
            10,
            Sort.by("fechaIngreso").descending().and(Sort.by("id").descending())));
  }

  public List<Empleado> reporte(ReporteFiltro f) {
    validacion.validar(f);
    return empleados.findByFechaIngresoBetweenOrderByFechaIngresoDescIdDesc(
        f.getDesde(), f.getHasta());
  }

  private String celda(Object value) {
    String s = value == null ? "" : value.toString();
    if (s.matches("^[\\s]*[=+@-].*")) s = "'" + s;
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  public String csv(ReporteFiltro f) {
    StringBuilder b =
        new StringBuilder(
            "\uFEFFID,Cédula,Nombre,Fecha de ingreso,Departamento,Puesto,Salario"
                + " mensual,Estado\r\n");
    for (Empleado e : reporte(f)) {
      Object[] values = {
        e.getId(),
        e.getCedula(),
        e.getNombre(),
        e.getFechaIngreso(),
        e.getDepartamento(),
        e.getPuesto().getNombre(),
        e.getSalarioMensual().toPlainString(),
        e.getEstado()
      };
      for (int i = 0; i < values.length; i++) {
        if (i > 0) b.append(',');
        b.append(celda(values[i]));
      }
      b.append("\r\n");
    }
    return b.toString();
  }
}
