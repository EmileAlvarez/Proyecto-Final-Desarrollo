package edu.unapec.rh.controller;

import edu.unapec.rh.dto.ReporteFiltro;
import edu.unapec.rh.entity.Empleado;
import edu.unapec.rh.service.EmpleadoService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reportes/nuevos-ingresos")
public class ReporteController {
  private final EmpleadoService empleados;

  @GetMapping
  public String report(
      @Valid @ModelAttribute("filtro") ReporteFiltro f, BindingResult errors, Model m) {
    List<Empleado> list = errors.hasErrors() ? List.of() : empleados.reporte(f);
    m.addAttribute("title", "Empleados de nuevo ingreso");
    m.addAttribute("empleados", list);
    m.addAttribute(
        "total",
        list.stream().map(Empleado::getSalarioMensual).reduce(BigDecimal.ZERO, BigDecimal::add));
    return "reportes/nuevos-ingresos";
  }

  @GetMapping("/csv")
  public ResponseEntity<byte[]> csv(@ModelAttribute ReporteFiltro f) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nuevos-ingresos.csv\"")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(empleados.csv(f).getBytes(StandardCharsets.UTF_8));
  }
}
