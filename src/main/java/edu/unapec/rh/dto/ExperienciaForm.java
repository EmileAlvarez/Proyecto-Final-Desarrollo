package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class ExperienciaForm {

  @NotBlank(message = "Escribe la empresa.")
  @Size(max = 180)
  private String empresa;

  @NotBlank(message = "Escribe el puesto ocupado.")
  @Size(max = 180)
  private String puestoOcupado;

  @NotNull(message = "Selecciona la fecha inicial.")
  private LocalDate fechaDesde;

  private LocalDate fechaHasta;

  @NotNull
  @DecimalMin("0.00")
  @Digits(integer = 12, fraction = 2)
  private BigDecimal salario;

  @AssertTrue(message = "La fecha final no puede ser anterior a la inicial.")
  public boolean isPeriodoValido() {
    return fechaDesde == null || fechaHasta == null || !fechaHasta.isBefore(fechaDesde);
  }
}
