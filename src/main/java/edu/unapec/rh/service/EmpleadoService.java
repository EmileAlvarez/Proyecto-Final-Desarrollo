package edu.unapec.rh.service;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.Empleado;
import java.util.List;
import org.springframework.data.domain.Page;

public interface EmpleadoService {
  Empleado obtener(Long id);

  EmpleadoForm formulario(Long id);

  void actualizar(Long id, EmpleadoForm form);

  void cambiarEstado(Long id, long version);

  Page<Empleado> buscar(EmpleadoFiltro filtro, int page);

  List<Empleado> reporte(ReporteFiltro filtro);

  String csv(ReporteFiltro filtro);
}
