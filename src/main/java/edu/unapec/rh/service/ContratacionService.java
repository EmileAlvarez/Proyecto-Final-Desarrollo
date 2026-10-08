package edu.unapec.rh.service;

import edu.unapec.rh.dto.ContratacionForm;
import edu.unapec.rh.entity.Empleado;

public interface ContratacionService {
  Empleado contratar(Long candidatoId, ContratacionForm form);
}
