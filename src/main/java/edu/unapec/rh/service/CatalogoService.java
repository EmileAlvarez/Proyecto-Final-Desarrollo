package edu.unapec.rh.service;

import edu.unapec.rh.dto.CatalogoForm;
import edu.unapec.rh.entity.*;
import java.util.List;
import org.springframework.data.domain.Page;

public interface CatalogoService {
  Page<? extends CatalogoBase> buscar(
      TipoCatalogo tipo,
      String q,
      Estado estado,
      Riesgo riesgo,
      Nivel nivel,
      String institucion,
      int page);

  CatalogoBase obtener(TipoCatalogo tipo, Long id);

  CatalogoForm formulario(TipoCatalogo tipo, Long id);

  CatalogoBase guardar(TipoCatalogo tipo, Long id, CatalogoForm form);

  void cambiarEstado(TipoCatalogo tipo, Long id, long version);

  List<? extends CatalogoBase> opciones(TipoCatalogo tipo);

  List<? extends CatalogoBase> todos(TipoCatalogo tipo);
}
