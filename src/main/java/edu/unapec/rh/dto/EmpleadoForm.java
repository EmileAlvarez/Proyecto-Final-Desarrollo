package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class EmpleadoForm {

  @NotNull private Long version;
  @NotNull private LocalDate fechaIngreso;

  @NotBlank
  @Size(max = 100)
  private String departamento;

  @NotNull private Long puestoId;

  @NotNull
  @DecimalMin(value = "0.00", inclusive = false)
  @Digits(integer = 12, fraction = 2)
  private BigDecimal salarioMensual;

  @NotNull private Estado estado;
}
