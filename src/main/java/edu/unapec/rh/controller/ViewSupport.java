package edu.unapec.rh.controller;

import edu.unapec.rh.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;

public final class ViewSupport {
  private ViewSupport() {}

  public static void error(BindingResult result, RuntimeException e) {
    if (e instanceof BusinessException b && b.getField() != null)
      result.rejectValue(b.getField(), "business", b.getMessage());
    else
      result.reject(
          "business",
          e instanceof DataIntegrityViolationException
              ? "No se pudo guardar: hay un valor duplicado o una relación inválida. Revisa los"
                    + " datos."
              : e.getMessage());
  }
}
