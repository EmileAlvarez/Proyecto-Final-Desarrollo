package edu.unapec.rh.service.impl;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.repository.*;
import edu.unapec.rh.service.*;
import edu.unapec.rh.util.Validacion;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {
  private final CompetenciaRepository competencias;
  private final IdiomaRepository idiomas;
  private final CapacitacionRepository capacitaciones;
  private final PuestoRepository puestos;
  private final Validacion validacion;

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CatalogoRepository<CatalogoBase> repo(TipoCatalogo t) {
    return (CatalogoRepository)
        switch (t) {
          case COMPETENCIAS -> competencias;
          case IDIOMAS -> idiomas;
          case CAPACITACIONES -> capacitaciones;
          case PUESTOS -> puestos;
        };
  }

  public CatalogoBase obtener(TipoCatalogo t, Long id) {
    return repo(t).findById(id).orElseThrow(NotFoundException::new);
  }

  public List<? extends CatalogoBase> opciones(TipoCatalogo t) {
    return repo(t).findByEstadoOrderByNombreAsc(Estado.ACTIVO);
  }

  public List<? extends CatalogoBase> todos(TipoCatalogo t) {
    return repo(t).findAll(Sort.by("nombre"));
  }

  public Page<? extends CatalogoBase> buscar(
      TipoCatalogo t,
      String q,
      Estado estado,
      Riesgo riesgo,
      Nivel nivel,
      String institucion,
      int page) {
    return repo(t)
        .findAll(
            (r, query, cb) -> {
              List<Predicate> p = new ArrayList<>();
              ConsultaSpecifications.texto(p, cb, r, "nombre", q);
              if (estado != null) p.add(cb.equal(r.get("estado"), estado));
              if (t == TipoCatalogo.PUESTOS && riesgo != null)
                p.add(cb.equal(r.get("riesgo"), riesgo));
              if (t == TipoCatalogo.CAPACITACIONES) {
                if (nivel != null) p.add(cb.equal(r.get("nivel"), nivel));
                ConsultaSpecifications.texto(p, cb, r, "institucion", institucion);
              }
              return cb.and(p.toArray(Predicate[]::new));
            },
            PageRequest.of(Math.max(0, page), 10, Sort.by("nombre").and(Sort.by("id"))));
  }

  public CatalogoForm formulario(TipoCatalogo t, Long id) {
    CatalogoForm f = new CatalogoForm();
    BeanUtils.copyProperties(obtener(t, id), f);
    return f;
  }

  @Transactional
  public CatalogoBase guardar(TipoCatalogo t, Long id, CatalogoForm f) {
    validacion.validar(f);
    if (repo(t).existsByClaveAndIdNot(CatalogoBase.normalizar(f.getNombre()), id == null ? 0L : id))
      throw new BusinessException("nombre", "Ya existe un registro con este nombre o descripción.");
    if (t == TipoCatalogo.PUESTOS) {
      if (f.getRiesgo() == null)
        throw new BusinessException("riesgo", "Selecciona el nivel de riesgo.");
      if (f.getSalarioMinimo() == null)
        throw new BusinessException("salarioMinimo", "Indica el salario mínimo.");
      if (f.getSalarioMaximo() == null)
        throw new BusinessException("salarioMaximo", "Indica el salario máximo.");
    }
    if (t == TipoCatalogo.CAPACITACIONES) {
      if (f.getNivel() == null) throw new BusinessException("nivel", "Selecciona un nivel.");
      if (f.getFechaDesde() == null)
        throw new BusinessException("fechaDesde", "Selecciona la fecha inicial.");
      if (f.getInstitucion() == null || f.getInstitucion().isBlank())
        throw new BusinessException("institucion", "Escribe la institución.");
    }
    CatalogoBase e =
        id == null
            ? switch (t) {
              case COMPETENCIAS -> new Competencia();
              case IDIOMAS -> new Idioma();
              case PUESTOS -> new Puesto();
              case CAPACITACIONES -> new Capacitacion();
            }
            : obtener(t, id);
    if (id != null) Validacion.version(f.getVersion(), e.getVersion());
    BeanUtils.copyProperties(f, e, "version");
    e.normalizarNombre();
    return repo(t).saveAndFlush(e);
  }

  @Transactional
  public void cambiarEstado(TipoCatalogo t, Long id, long version) {
    CatalogoBase e = obtener(t, id);
    Validacion.version(version, e.getVersion());
    e.setEstado(e.getEstado() == Estado.ACTIVO ? Estado.INACTIVO : Estado.ACTIVO);
  }
}
