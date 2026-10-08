package edu.unapec.rh.repository;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import jakarta.persistence.criteria.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;

public final class ConsultaSpecifications {
  private ConsultaSpecifications() {}

  private static String like(String s) {
    return "%"
        + s.strip()
            .toLowerCase(Locale.ROOT)
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        + "%";
  }

  public static <T> void texto(
      List<Predicate> p, CriteriaBuilder cb, Root<T> root, String campo, String valor) {
    if (valor != null && !valor.isBlank())
      p.add(cb.like(cb.lower(root.get(campo)), like(valor), '\\'));
  }

  public static Specification<Candidato> candidatos(CandidatoFiltro f) {
    return (r, q, cb) -> {
      List<Predicate> p = new ArrayList<>();
      if (f.getNombre() != null && !f.getNombre().isBlank())
        p.add(
            cb.like(
                cb.lower(cb.concat(cb.concat(r.get("nombre"), " "), r.get("apellidos"))),
                like(f.getNombre()),
                '\\'));
      texto(p, cb, r, "cedula", f.getCedula());
      texto(p, cb, r, "departamento", f.getDepartamento());
      if (f.getPuestoId() != null) p.add(cb.equal(r.get("puesto").get("id"), f.getPuestoId()));
      if (f.getEstado() != null) p.add(cb.equal(r.get("estado"), f.getEstado()));
      if (f.getCompetenciaId() != null)
        p.add(cb.equal(r.join("competencias").get("id"), f.getCompetenciaId()));
      if (f.getCapacitacionId() != null)
        p.add(cb.equal(r.join("capacitaciones").get("id"), f.getCapacitacionId()));
      if (f.getIdiomaId() != null)
        p.add(cb.equal(r.join("idiomas").get("idioma").get("id"), f.getIdiomaId()));
      if (f.getSalarioMin() != null)
        p.add(cb.greaterThanOrEqualTo(r.get("salarioAspirado"), f.getSalarioMin()));
      if (f.getSalarioMax() != null)
        p.add(cb.lessThanOrEqualTo(r.get("salarioAspirado"), f.getSalarioMax()));
      q.distinct(true);
      return cb.and(p.toArray(Predicate[]::new));
    };
  }

  public static Specification<Empleado> empleados(EmpleadoFiltro f) {
    return (r, q, cb) -> {
      List<Predicate> p = new ArrayList<>();
      texto(p, cb, r, "nombre", f.getNombre());
      texto(p, cb, r, "cedula", f.getCedula());
      texto(p, cb, r, "departamento", f.getDepartamento());
      if (f.getEstado() != null) p.add(cb.equal(r.get("estado"), f.getEstado()));
      if (f.getPuestoId() != null) p.add(cb.equal(r.get("puesto").get("id"), f.getPuestoId()));
      return cb.and(p.toArray(Predicate[]::new));
    };
  }
}
