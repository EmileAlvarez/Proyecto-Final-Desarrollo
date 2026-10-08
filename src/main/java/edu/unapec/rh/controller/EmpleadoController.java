package edu.unapec.rh.controller;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.BusinessException;
import edu.unapec.rh.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/empleados")
public class EmpleadoController {
  private final EmpleadoService empleados;
  private final CatalogoService catalogos;

  @GetMapping
  public String list(
      @Valid @ModelAttribute("filtro") EmpleadoFiltro f,
      BindingResult errors,
      @RequestParam(defaultValue = "0") int page,
      Model m) {
    m.addAttribute("title", "Empleados");
    m.addAttribute(
        "page",
        errors.hasErrors()
            ? org.springframework.data.domain.Page.empty()
            : empleados.buscar(f, page));
    m.addAttribute("puestos", catalogos.todos(TipoCatalogo.PUESTOS));
    return "empleados/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model m) {
    m.addAttribute("title", "Perfil del empleado");
    m.addAttribute("e", empleados.obtener(id));
    return "empleados/detail";
  }

  private void options(Long id, EmpleadoForm f, Model m) {
    m.addAttribute("title", "Editar empleado");
    m.addAttribute("e", empleados.obtener(id));
    m.addAttribute(
        "puestos",
        catalogos.todos(TipoCatalogo.PUESTOS).stream()
            .filter(p -> p.getEstado() == Estado.ACTIVO || p.getId().equals(f.getPuestoId()))
            .toList());
  }

  @GetMapping("/{id}/editar")
  public String edit(@PathVariable Long id, Model m) {
    EmpleadoForm f = empleados.formulario(id);
    m.addAttribute("form", f);
    options(id, f, m);
    return "empleados/form";
  }

  @PostMapping("/{id}")
  public String save(
      @PathVariable Long id,
      @Valid @ModelAttribute("form") EmpleadoForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        empleados.actualizar(id, f);
        flash.addFlashAttribute("success", "Empleado actualizado.");
        return "redirect:/empleados/" + id;
      } catch (BusinessException e) {
        ViewSupport.error(errors, e);
      }
    options(id, f, m);
    return "empleados/form";
  }

  @PostMapping("/{id}/estado")
  public String state(@PathVariable Long id, @RequestParam long version, RedirectAttributes flash) {
    empleados.cambiarEstado(id, version);
    flash.addFlashAttribute("success", "Estado actualizado.");
    return "redirect:/empleados/" + id;
  }
}
