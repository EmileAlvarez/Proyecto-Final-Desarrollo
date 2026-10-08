package edu.unapec.rh.service;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import org.springframework.data.domain.Page;

public interface CandidatoService {
  Candidato obtener(Long id);

  CandidatoForm formulario(Long id);

  Candidato guardar(Long id, CandidatoForm form);

  Page<Candidato> buscar(CandidatoFiltro filtro, int page, String orden);

  void cambiarEstado(Long id, EstadoCandidato estado, long version);

  void eliminar(Long id, long version);
}
