package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class ReporteFiltro {

  @NotNull(message = "Selecciona la fecha inicial.")
  private LocalDate desde = LocalDate.now().withDayOfMonth(1);

  @NotNull(message = "Selecciona la fecha final.")
  private LocalDate hasta = LocalDate.now();

  @AssertTrue(message = "La fecha final no puede ser anterior a la inicial.")
  public boolean isPeriodoValido() {
    return desde == null || hasta == null || !hasta.isBefore(desde);
  }
}
