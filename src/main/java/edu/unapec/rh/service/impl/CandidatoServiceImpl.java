package edu.unapec.rh.service.impl;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.repository.*;
import edu.unapec.rh.service.*;
import edu.unapec.rh.util.Validacion;
import java.util.*;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CandidatoServiceImpl implements CandidatoService {
  private final CandidatoRepository candidatos;
  private final PuestoRepository puestos;
  private final CompetenciaRepository competencias;
  private final CapacitacionRepository capacitaciones;
  private final IdiomaRepository idiomas;
  private final EmpleadoRepository empleados;
  private final Validacion validacion;

  public CandidatoServiceImpl(
      CandidatoRepository candidatos,
      PuestoRepository puestos,
      CompetenciaRepository competencias,
      CapacitacionRepository capacitaciones,
      IdiomaRepository idiomas,
      EmpleadoRepository empleados,
      Validacion validacion) {
    this.candidatos = candidatos;
    this.puestos = puestos;
    this.competencias = competencias;
    this.capacitaciones = capacitaciones;
    this.idiomas = idiomas;
    this.empleados = empleados;
    this.validacion = validacion;
  }

  private Candidato cargar(Long id) {
    return candidatos.findById(id).orElseThrow(NotFoundException::new);
  }

  private Candidato preparar(Candidato c) {
    Hibernate.initialize(c.getCompetencias());
    Hibernate.initialize(c.getCapacitaciones());
    c.getIdiomas().forEach(i -> Hibernate.initialize(i.getIdioma()));
    Hibernate.initialize(c.getExperiencias());
    return c;
  }

  public Candidato obtener(Long id) {
    return preparar(cargar(id));
  }

  public Page<Candidato> buscar(CandidatoFiltro filtro, int page, String orden) {
    validacion.validar(filtro);
    Sort sort =
        switch (orden) {
          case "nombre" -> Sort.by("nombre", "apellidos").ascending();
          case "salario" -> Sort.by("salarioAspirado").ascending();
          default -> Sort.by("fechaRegistro").descending();
        };
    Page<Candidato> result =
        candidatos.findAll(
            ConsultaSpecifications.candidatos(filtro),
            PageRequest.of(Math.max(0, page), 10, sort.and(Sort.by("id").descending())));
    result.forEach(c -> Hibernate.initialize(c.getCompetencias()));
    return result;
  }

  public CandidatoForm formulario(Long id) {
    Candidato c = obtener(id);
    CandidatoForm f = new CandidatoForm();
    BeanUtils.copyProperties(c, f);
    f.setPuestoId(c.getPuesto().getId());
    f.setCompetenciaIds(c.getCompetencias().stream().map(BaseEntity::getId).toList());
    f.setCapacitacionIds(c.getCapacitaciones().stream().map(BaseEntity::getId).toList());
    c.getIdiomas()
        .forEach(
            i -> {
              IdiomaSeleccion s = new IdiomaSeleccion();
              s.setIdiomaId(i.getIdioma().getId());
              s.setSeleccionado(true);
              s.setNivel(i.getNivel());
              f.getIdiomas().add(s);
            });
    c.getExperiencias()
        .forEach(
            e -> {
              ExperienciaForm x = new ExperienciaForm();
              BeanUtils.copyProperties(e, x);
              f.getExperiencias().add(x);
            });
    return f;
  }

  private <T extends CatalogoBase> Set<T> seleccion(
      CatalogoRepository<T> repo, List<Long> ids, Set<T> actuales, String field) {
    Set<Long> anteriores = actuales.stream().map(BaseEntity::getId).collect(Collectors.toSet());
    Set<T> seleccionados = new LinkedHashSet<>();
    for (Long id : new LinkedHashSet<>(ids)) {
      T e =
          repo.findById(id)
              .orElseThrow(
                  () -> new BusinessException(field, "Una opción seleccionada ya no existe."));
      if (e.getEstado() != Estado.ACTIVO && !anteriores.contains(id))
        throw new BusinessException(field, "Solo puedes agregar opciones activas.");
      seleccionados.add(e);
    }
    return seleccionados;
  }

  @Transactional
  public Candidato guardar(Long id, CandidatoForm f) {
    validacion.validar(f);
    String cedula = Validacion.cedula(f.getCedula());
    if (!Validacion.validaCedula(cedula))
      throw new BusinessException("cedula", "La cédula ingresada no es válida.");
    if (candidatos.existsByCedulaAndIdNot(cedula, id == null ? 0L : id))
      throw new BusinessException("cedula", "Ya existe un candidato con esta cédula.");
    Candidato c = id == null ? new Candidato() : cargar(id);
    if (id != null) {
      Validacion.version(f.getVersion(), c.getVersion());
      if (c.getEstado() == EstadoCandidato.CONTRATADO)
        throw new BusinessException(
            "El candidato ya fue contratado. Edita sus datos laborales desde Empleados.");
    }
    Puesto puesto =
        puestos
            .findById(f.getPuestoId())
            .orElseThrow(() -> new BusinessException("puestoId", "El puesto no existe."));
    if (puesto.getEstado() != Estado.ACTIVO
        && (c.getPuesto() == null || !puesto.getId().equals(c.getPuesto().getId())))
      throw new BusinessException("puestoId", "Selecciona un puesto activo.");
    Set<Competencia> cs =
        seleccion(competencias, f.getCompetenciaIds(), c.getCompetencias(), "competenciaIds");
    Set<Capacitacion> caps =
        seleccion(capacitaciones, f.getCapacitacionIds(), c.getCapacitaciones(), "capacitacionIds");
    // Reutilizar las filas de idiomas evita violar la clave única durante una edición.
    Map<Long, CandidatoIdioma> previos =
        c.getIdiomas().stream().collect(Collectors.toMap(i -> i.getIdioma().getId(), i -> i));
    Set<Long> seleccionados = new HashSet<>();
    for (int index = 0; index < f.getIdiomas().size(); index++) {
      IdiomaSeleccion s = f.getIdiomas().get(index);
      if (!s.isSeleccionado()) continue;
      if (!seleccionados.add(s.getIdiomaId()))
        throw new BusinessException("idiomas", "No repitas el mismo idioma.");
      Idioma idioma =
          idiomas
              .findById(s.getIdiomaId())
              .orElseThrow(() -> new BusinessException("idiomas", "El idioma no existe."));
      if (idioma.getEstado() != Estado.ACTIVO && !previos.containsKey(idioma.getId()))
        throw new BusinessException("idiomas", "Solo puedes agregar idiomas activos.");
      CandidatoIdioma ci = previos.get(idioma.getId());
      if (ci == null) {
        ci = new CandidatoIdioma();
        ci.setCandidato(c);
        ci.setIdioma(idioma);
        c.getIdiomas().add(ci);
      }
      ci.setNivel(s.getNivel());
    }
    c.getIdiomas().removeIf(i -> !seleccionados.contains(i.getIdioma().getId()));
    BeanUtils.copyProperties(
        f, c, "version", "experiencias", "idiomas", "competencias", "capacitaciones");
    c.setCedula(cedula);
    c.setNombre(f.getNombre().strip());
    c.setApellidos(f.getApellidos().strip());
    c.setEmail(f.getEmail().strip().toLowerCase(Locale.ROOT));
    c.setTelefono(
        f.getTelefono() == null || f.getTelefono().isBlank() ? null : f.getTelefono().strip());
    c.setDepartamento(f.getDepartamento().strip());
    c.setRecomendadoPor(
        f.getRecomendadoPor() == null || f.getRecomendadoPor().isBlank()
            ? null
            : f.getRecomendadoPor().strip());
    c.setPuesto(puesto);
    c.getCompetencias().clear();
    c.getCompetencias().addAll(cs);
    c.getCapacitaciones().clear();
    c.getCapacitaciones().addAll(caps);
    c.getExperiencias().clear();
    for (ExperienciaForm x : f.getExperiencias()) {
      ExperienciaLaboral e = new ExperienciaLaboral();
      BeanUtils.copyProperties(x, e);
      e.setCandidato(c);
      c.getExperiencias().add(e);
    }
    return candidatos.saveAndFlush(c);
  }

  @Transactional
  public void cambiarEstado(Long id, EstadoCandidato estado, long version) {
    Candidato c = candidatos.findForUpdate(id).orElseThrow(NotFoundException::new);
    Validacion.version(version, c.getVersion());
    if (estado == null || estado == EstadoCandidato.CONTRATADO)
      throw new BusinessException("Para contratar utiliza el proceso de selección.");
    if (c.getEstado() == EstadoCandidato.CONTRATADO)
      throw new BusinessException("Un candidato contratado conserva su historial de contratación.");
    c.setEstado(estado);
  }

  @Transactional
  public void eliminar(Long id, long version) {
    Candidato c = candidatos.findForUpdate(id).orElseThrow(NotFoundException::new);
    Validacion.version(version, c.getVersion());
    if (c.getEstado() == EstadoCandidato.CONTRATADO || empleados.existsByCandidatoId(id))
      throw new BusinessException(
          "No se puede eliminar un candidato contratado porque está vinculado a un empleado.");
    candidatos.delete(c);
    candidatos.flush();
  }
}
