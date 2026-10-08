package edu.unapec.rh.controller;

import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.UriComponentsBuilder;

@ControllerAdvice
@lombok.extern.slf4j.Slf4j
public class GlobalAdvice {
  @Value("${app.seed.enabled}")
  private boolean demo;

  @InitBinder
  public void trim(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @ModelAttribute
  public void common(Model m, HttpServletRequest req) {
    m.addAttribute("currentPath", req.getRequestURI());
    m.addAttribute("demo", demo);
    m.addAttribute("estados", Estado.values());
    m.addAttribute("estadosCandidato", EstadoCandidato.values());
    m.addAttribute("riesgos", Riesgo.values());
    m.addAttribute("niveles", Nivel.values());
    m.addAttribute("nivelesIdioma", NivelIdioma.values());
    String base =
        UriComponentsBuilder.fromUriString(
                req.getRequestURI()
                    + (req.getQueryString() == null ? "" : "?" + req.getQueryString()))
            .replaceQueryParam("page")
            .build(true)
            .toUriString();
    m.addAttribute("pageLink", base + (base.contains("?") ? "&" : "?") + "page=");
  }

  @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String notFound(Model m) {
    m.addAttribute("title", "Página no encontrada");
    return "error/404";
  }

  @ExceptionHandler({
    BusinessException.class,
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class,
    CannotAcquireLockException.class,
    MethodArgumentTypeMismatchException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String business(Exception e, Model m) {
    m.addAttribute("title", "No se pudo completar la operación");
    m.addAttribute(
        "message",
        e instanceof BusinessException
            ? e.getMessage()
            : "Los datos son inválidos o el registro cambió. Vuelve a abrir el formulario e"
                + " inténtalo otra vez.");
    return "error/business";
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public String database(DataAccessException e, Model m) {
    log.error("No fue posible completar el acceso a la base de datos", e);
    m.addAttribute("title", "Base de datos no disponible");
    m.addAttribute(
        "message",
        "No pudimos acceder a la base de datos. Comprueba la conexión e inténtalo nuevamente.");
    return "error/business";
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String unexpected(Exception e, Model m) {
    log.error("Error inesperado al procesar la solicitud", e);
    m.addAttribute("title", "Error inesperado");
    return "error/500";
  }
}
