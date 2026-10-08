package edu.unapec.rh.util;

import edu.unapec.rh.exception.BusinessException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Validacion {
  private final Validator validator;

  public void validar(Object form) {
    validator.validate(form).stream()
        .findFirst()
        .ifPresent(
            v -> {
              throw new BusinessException(v.getPropertyPath().toString(), v.getMessage());
            });
  }

  public static void version(Long submitted, long actual) {
    if (submitted == null || submitted != actual)
      throw new BusinessException(
          "Otro usuario modificó este registro. Recarga la página antes de guardar.");
  }

  public static String cedula(String value) {
    return value.strip();
  }

  /**
   * Valida una cédula dominicana usando el algoritmo de comprobación provisto para el proyecto.
   * Se aceptan guiones para efectos del cálculo, aunque los formularios actuales exigen 11 dígitos.
   */
  public static boolean validaCedula(String pCedula) {
    if (pCedula == null) return false;

    String vcCedula = pCedula.replace("-", "").trim();
    if (vcCedula.length() != 11 || !vcCedula.chars().allMatch(Character::isDigit)) return false;

    int vnTotal = 0;
    int[] digitoMult = {1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1};

    for (int vDig = 0; vDig < vcCedula.length(); vDig++) {
      int vCalculo = Character.digit(vcCedula.charAt(vDig), 10) * digitoMult[vDig];
      if (vCalculo < 10) {
        vnTotal += vCalculo;
      } else {
        vnTotal += (vCalculo / 10) + (vCalculo % 10);
      }
    }

    return vnTotal % 10 == 0;
  }
}
