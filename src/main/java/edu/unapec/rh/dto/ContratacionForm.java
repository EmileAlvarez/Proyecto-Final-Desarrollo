package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class ContratacionForm {

  @NotNull(message = "Selecciona la fecha de ingreso.")
  private LocalDate fechaIngreso = LocalDate.now();

  @NotBlank
  @Size(max = 100)
  private String departamento;

  @NotNull(message = "Selecciona el puesto definitivo.")
  private Long puestoId;

  @NotNull
  @DecimalMin(value = "0.00", inclusive = false, message = "El salario debe ser mayor que cero.")
  @Digits(integer = 12, fraction = 2)
  private BigDecimal salarioMensual;
}
