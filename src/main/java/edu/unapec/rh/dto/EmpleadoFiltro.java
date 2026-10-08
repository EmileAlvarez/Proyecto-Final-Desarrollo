package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class EmpleadoFiltro {

  @Size(max = 100)
  private String nombre;

  @Size(max = 11)
  @Pattern(regexp = "[0-9]*", message = "La cédula de búsqueda solo puede contener números.")
  private String cedula;

  @Size(max = 100)
  private String departamento;

  private Long puestoId;
  private Estado estado;
}
