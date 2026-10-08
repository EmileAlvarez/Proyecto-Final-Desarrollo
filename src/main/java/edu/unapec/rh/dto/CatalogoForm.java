package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class CatalogoForm {

  private Long version;

  @NotBlank(message = "Escribe un nombre o descripción.")
  @Size(max = 180)
  private String nombre;

  @NotNull private Estado estado = Estado.ACTIVO;
  private Riesgo riesgo;

  @DecimalMin("0.00")
  @Digits(integer = 12, fraction = 2)
  private BigDecimal salarioMinimo;

  @DecimalMin(value = "0.00", inclusive = false)
  @Digits(integer = 12, fraction = 2)
  private BigDecimal salarioMaximo;

  private Nivel nivel;
  private LocalDate fechaDesde;
  private LocalDate fechaHasta;

  @Size(max = 180)
  private String institucion;

  @AssertTrue(message = "La fecha final no puede ser anterior a la inicial.")
  public boolean isPeriodoValido() {
    return fechaDesde == null || fechaHasta == null || !fechaHasta.isBefore(fechaDesde);
  }

  @AssertTrue(message = "El salario máximo debe ser mayor o igual al mínimo.")
  public boolean isRangoValido() {
    return salarioMinimo == null
        || salarioMaximo == null
        || salarioMaximo.compareTo(salarioMinimo) >= 0;
  }
}
