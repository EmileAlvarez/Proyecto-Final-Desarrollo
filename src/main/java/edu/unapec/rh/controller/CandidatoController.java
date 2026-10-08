package edu.unapec.rh.controller;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.service.*;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CandidatoController {
  private final CandidatoService candidatos;
  private final CatalogoService catalogos;
  private final ContratacionService contratacion;

  public CandidatoController(
      CandidatoService candidatos, CatalogoService catalogos, ContratacionService contratacion) {
    this.candidatos = candidatos;
    this.catalogos = catalogos;
    this.contratacion = contratacion;
  }

  private void filters(Model m) {
    m.addAttribute("puestos", catalogos.todos(TipoCatalogo.PUESTOS));
    m.addAttribute("competencias", catalogos.todos(TipoCatalogo.COMPETENCIAS));
    m.addAttribute("capacitaciones", catalogos.todos(TipoCatalogo.CAPACITACIONES));
    m.addAttribute("idiomas", catalogos.todos(TipoCatalogo.IDIOMAS));
  }

  private void opciones(CandidatoForm f, Model m) {
    m.addAttribute("title", "Candidato");
    m.addAttribute(
        "puestos",
        catalogos.todos(TipoCatalogo.PUESTOS).stream()
            .filter(x -> x.getEstado() == Estado.ACTIVO || x.getId().equals(f.getPuestoId()))
            .toList());
    m.addAttribute(
        "competencias",
        catalogos.todos(TipoCatalogo.COMPETENCIAS).stream()
            .filter(
                x -> x.getEstado() == Estado.ACTIVO || f.getCompetenciaIds().contains(x.getId()))
            .toList());
    m.addAttribute(
        "capacitaciones",
        catalogos.todos(TipoCatalogo.CAPACITACIONES).stream()
            .filter(
                x -> x.getEstado() == Estado.ACTIVO || f.getCapacitacionIds().contains(x.getId()))
            .toList());
    var idiomas = catalogos.todos(TipoCatalogo.IDIOMAS);
    Set<Long> present =
        f.getIdiomas().stream().map(IdiomaSeleccion::getIdiomaId).collect(Collectors.toSet());
    idiomas.stream()
        .filter(i -> i.getEstado() == Estado.ACTIVO && !present.contains(i.getId()))
        .forEach(
            i -> {
              IdiomaSeleccion s = new IdiomaSeleccion();
              s.setIdiomaId(i.getId());
              f.getIdiomas().add(s);
            });
    m.addAttribute(
        "idiomaNombres",
        idiomas.stream().collect(Collectors.toMap(BaseEntity::getId, CatalogoBase::getNombre)));
  }

  @GetMapping({"/candidatos", "/consultas"})
  public String list(
      @Valid @ModelAttribute("filtro") CandidatoFiltro f,
      BindingResult errors,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "recientes") String orden,
      Model m,
      jakarta.servlet.http.HttpServletRequest req) {
    filters(m);
    m.addAttribute(
        "title",
        req.getRequestURI().equals("/consultas") ? "Consulta de candidatos" : "Candidatos");
    m.addAttribute("orden", orden);
    m.addAttribute("page", errors.hasErrors() ? Page.empty() : candidatos.buscar(f, page, orden));
    return "candidatos/list";
  }

  @GetMapping("/candidatos/nuevo")
  public String create(Model m) {
    CandidatoForm f = new CandidatoForm();
    m.addAttribute("form", f);
    opciones(f, m);
    return "candidatos/form";
  }

  @GetMapping("/candidatos/{id}/editar")
  public String edit(@PathVariable Long id, Model m) {
    if (candidatos.obtener(id).getEstado() == EstadoCandidato.CONTRATADO)
      throw new BusinessException(
          "El perfil contratado es histórico. Actualiza sus datos laborales desde Empleados.");
    CandidatoForm f = candidatos.formulario(id);
    m.addAttribute("form", f);
    m.addAttribute("id", id);
    opciones(f, m);
    return "candidatos/form";
  }

  @PostMapping({"/candidatos", "/candidatos/{id}"})
  public String save(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") CandidatoForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        Candidato c = candidatos.guardar(id, f);
        flash.addFlashAttribute("success", "Perfil guardado correctamente.");
        return "redirect:/candidatos/" + c.getId();
      } catch (BusinessException | DataIntegrityViolationException e) {
        ViewSupport.error(errors, e);
      }
    m.addAttribute("id", id);
    opciones(f, m);
    return "candidatos/form";
  }

  @GetMapping("/candidatos/{id}")
  public String profile(@PathVariable Long id, Model m) {
    m.addAttribute("c", candidatos.obtener(id));
    m.addAttribute("title", "Perfil del candidato");
    return "candidatos/detail";
  }

  @PostMapping("/candidatos/{id}/estado")
  public String state(
      @PathVariable Long id,
      @RequestParam EstadoCandidato estado,
      @RequestParam long version,
      RedirectAttributes flash) {
    candidatos.cambiarEstado(id, estado, version);
    flash.addFlashAttribute("success", "Estado del candidato actualizado.");
    return "redirect:/candidatos/" + id;
  }

  @PostMapping("/candidatos/{id}/eliminar")
  public String delete(
      @PathVariable Long id, @RequestParam long version, RedirectAttributes flash) {
    try {
      candidatos.eliminar(id, version);
      flash.addFlashAttribute("success", "Candidato eliminado correctamente.");
      return "redirect:/candidatos";
    } catch (BusinessException | DataIntegrityViolationException e) {
      flash.addFlashAttribute(
          "error",
          e instanceof BusinessException
              ? e.getMessage()
              : "No se puede eliminar el candidato porque tiene información relacionada.");
      return "redirect:/candidatos/" + id;
    }
  }

  private void hiring(Long id, Model m) {
    Candidato c = candidatos.obtener(id);
    if (!c.isElegible())
      throw new BusinessException("El candidato no está disponible para contratación.");
    m.addAttribute("c", c);
    m.addAttribute("puestos", catalogos.opciones(TipoCatalogo.PUESTOS));
    m.addAttribute("title", "Contratar candidato");
  }

  @GetMapping("/candidatos/{id}/contratar")
  public String hire(@PathVariable Long id, Model m) {
    hiring(id, m);
    Candidato c = candidatos.obtener(id);
    ContratacionForm f = new ContratacionForm();
    f.setDepartamento(c.getDepartamento());
    f.setPuestoId(c.getPuesto().getId());
    f.setSalarioMensual(c.getSalarioAspirado());
    m.addAttribute("form", f);
    return "candidatos/contratar";
  }

  @PostMapping("/candidatos/{id}/contratar")
  public String hireSave(
      @PathVariable Long id,
      @Valid @ModelAttribute("form") ContratacionForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        Empleado e = contratacion.contratar(id, f);
        flash.addFlashAttribute(
            "success",
            "Contratación completada. El empleado fue creado y el candidato quedó CONTRATADO.");
        return "redirect:/empleados/" + e.getId();
      } catch (BusinessException | DataIntegrityViolationException e) {
        ViewSupport.error(errors, e);
      }
    m.addAttribute("c", candidatos.obtener(id));
    m.addAttribute("puestos", catalogos.opciones(TipoCatalogo.PUESTOS));
    m.addAttribute("title", "Contratar candidato");
    return "candidatos/contratar";
  }
}
