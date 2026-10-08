package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class CandidatoFiltro {

  @Size(max = 100)
  private String nombre;

  @Size(max = 11)
  @Pattern(regexp = "[0-9]*", message = "La cédula de búsqueda solo puede contener números.")
  private String cedula;

  private Long puestoId;

  @Size(max = 100)
  private String departamento;

  private EstadoCandidato estado;
  private Long competenciaId;
  private Long capacitacionId;
  private Long idiomaId;

  @DecimalMin("0.00")
  private BigDecimal salarioMin;

  @DecimalMin("0.00")
  private BigDecimal salarioMax;

  @AssertTrue(message = "El salario máximo del filtro debe ser mayor o igual al mínimo.")
  public boolean isRangoValido() {
    return salarioMin == null || salarioMax == null || salarioMax.compareTo(salarioMin) >= 0;
  }
}
