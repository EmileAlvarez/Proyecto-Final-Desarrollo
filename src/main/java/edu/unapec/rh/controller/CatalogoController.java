package edu.unapec.rh.controller;

import edu.unapec.rh.dto.CatalogoForm;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/{ruta:competencias|idiomas|capacitaciones|puestos}")
public class CatalogoController {
  private final CatalogoService service;

  private TipoCatalogo meta(String ruta, Model m) {
    TipoCatalogo t = TipoCatalogo.ruta(ruta);
    m.addAttribute("tipo", t);
    m.addAttribute("title", t.getTitulo());
    return t;
  }

  @GetMapping
  public String list(
      @PathVariable String ruta,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Estado estado,
      @RequestParam(required = false) Riesgo riesgo,
      @RequestParam(required = false) Nivel nivel,
      @RequestParam(defaultValue = "") String institucion,
      @RequestParam(defaultValue = "0") int page,
      Model m) {
    TipoCatalogo t = meta(ruta, m);
    m.addAttribute("page", service.buscar(t, q, estado, riesgo, nivel, institucion, page));
    m.addAttribute("q", q);
    m.addAttribute("estado", estado);
    m.addAttribute("riesgo", riesgo);
    m.addAttribute("nivel", nivel);
    m.addAttribute("institucion", institucion);
    return "catalogos/list";
  }

  @GetMapping("/nuevo")
  public String create(@PathVariable String ruta, Model m) {
    meta(ruta, m);
    m.addAttribute("form", new CatalogoForm());
    return "catalogos/form";
  }

  @GetMapping("/{id}/editar")
  public String edit(@PathVariable String ruta, @PathVariable Long id, Model m) {
    TipoCatalogo t = meta(ruta, m);
    m.addAttribute("form", service.formulario(t, id));
    m.addAttribute("id", id);
    return "catalogos/form";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable String ruta, @PathVariable Long id, Model m) {
    TipoCatalogo t = meta(ruta, m);
    m.addAttribute("item", service.obtener(t, id));
    return "catalogos/detail";
  }

  @PostMapping({"", "/{id}"})
  public String save(
      @PathVariable String ruta,
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") CatalogoForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    TipoCatalogo t = meta(ruta, m);
    m.addAttribute("id", id);
    if (!errors.hasErrors())
      try {
        CatalogoBase e = service.guardar(t, id, f);
        flash.addFlashAttribute("success", "Registro guardado correctamente.");
        return "redirect:/" + ruta + "/" + e.getId();
      } catch (BusinessException | DataIntegrityViolationException e) {
        ViewSupport.error(errors, e);
      }
    return "catalogos/form";
  }

  @PostMapping("/{id}/estado")
  public String state(
      @PathVariable String ruta,
      @PathVariable Long id,
      @RequestParam long version,
      RedirectAttributes flash) {
    service.cambiarEstado(TipoCatalogo.ruta(ruta), id, version);
    flash.addFlashAttribute("success", "Estado actualizado.");
    return "redirect:/" + ruta;
  }
}
